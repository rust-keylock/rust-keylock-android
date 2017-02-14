#[macro_use]
extern crate log;
#[macro_use]
extern crate lazy_static;
extern crate rust_keylock;
extern crate libc;

use libc::c_char;
use std::ffi::{CStr, CString};
use std::mem;
use std::str;
use std::sync::Mutex;
use std::sync::mpsc::{self, Sender, Receiver};
use rust_keylock::{UserSelection, Menu, Entry};

mod android_editor;
mod logger;

type LogCallback =  extern "C" fn(*const c_char, *const c_char, *const c_char, i32, *const c_char);
type StringCallback =  extern "C" fn(*const c_char);
type ShowEntryCallback = extern "C" fn(Box<JavaEntry>, i32, bool, bool);
type ShowEntriesSetCallback = extern "C" fn(JavaEntriesSet);

lazy_static! {
    static ref TX: Mutex<Option<Sender<UserSelection>>> = Mutex::new(None);
}

#[repr(C)]
pub struct JavaEntry {
	name: *const c_char,
    user: *const c_char,
    pass: *const c_char,
    desc: *const c_char,
}

impl JavaEntry {
    fn new(entry: &Entry) -> JavaEntry {
        JavaEntry {
        	name: to_java_string(entry.name.clone()),
		    user: to_java_string(entry.user.clone()),
		    pass: to_java_string(entry.pass.clone()),
		    desc: to_java_string(entry.desc.clone()),
        }
    }

	fn empty() -> JavaEntry {
		JavaEntry {
	    	name: to_java_string("".to_string()),
		    user: to_java_string("".to_string()),
		    pass: to_java_string("".to_string()),
		    desc: to_java_string("".to_string()),
	    }
	}

	fn with_nulls() -> JavaEntry {
		JavaEntry {
	    	name: to_java_string("null".to_string()),
		    user: to_java_string("null".to_string()),
		    pass: to_java_string("null".to_string()),
		    desc: to_java_string("null".to_string()),
	    }
	}
}

#[repr(C)]
pub struct JavaEntriesSet {
    entries: Box<[JavaEntry]>,
    number_of_entries: i32,
}

impl JavaEntriesSet {
	fn from(entries: &[Entry]) -> JavaEntriesSet {
		// Create JavaEntries from Entries
		let java_entries: Vec<JavaEntry> = entries.iter().clone().map(|entry| {
				JavaEntry::new(entry)
		}).collect();
		// Get the length of the entries
		let num_entries = java_entries.len();
		
		let java_entries_set = JavaEntriesSet {
			entries: java_entries.into_boxed_slice(),
			number_of_entries: num_entries as i32,
		};
		java_entries_set
	}

	fn with_nulls() -> JavaEntriesSet {
		let empty_entry = JavaEntry::with_nulls();
		let dummy = vec![empty_entry];
		let java_entries_set = JavaEntriesSet {
			entries: dummy.into_boxed_slice(),
			number_of_entries: 1,
		};
		java_entries_set
	}
}

#[no_mangle]
pub extern fn execute(show_menu_cb: StringCallback, show_entry_cb: ShowEntryCallback, show_entries_set_cb: ShowEntriesSetCallback, show_message_cb: StringCallback, log_cb: LogCallback) {
	let (tx, rx): (Sender<UserSelection>, Receiver<UserSelection>) = mpsc::channel();
	// Release the lock before calling the execute.
	// Execute will not return for the whole lifetime of the app, so the lock would be for ever acquired if was not explicitly released using the brackets.
	{
		let mut tx_opt = TX.lock().unwrap();
		*tx_opt = Some(tx);
	}
	let editor = android_editor::new(show_menu_cb, show_entry_cb, show_entries_set_cb, show_message_cb, log_cb, rx);
	debug!("TX Mutex initialized. Executing native rust_keylock!");
	rust_keylock::execute(&editor)
}

#[no_mangle]
pub extern fn set_password(password: *const c_char, number: i32) {
	debug!("set_password called");

	match TX.try_lock() {
		Ok(tx_opt) => {
			debug!("Lock acquired");
			let tx = tx_opt.as_ref().unwrap();
			let user_selection = UserSelection::ProvidedPassword(to_rust_string(password), number as usize);
			tx.send(user_selection).unwrap();
			debug!("set_password sent to the TX");
		},
		Err(error) => {
			error!("Could not acquire lock for tx: {:?}", error);
		},
	};
}

#[no_mangle]
pub extern fn go_to_menu(menu_name: *const c_char) {
	debug!("go_to_menu called");
	let tx = {
		TX.lock().unwrap().as_ref().unwrap().clone()
	};
	let rust_string_menu_name = to_rust_string(menu_name);
	debug!("go_to_menu '{}'", rust_string_menu_name);
	let menu = Menu::from(rust_string_menu_name, None);
	let user_selection = UserSelection::GoTo(menu);
	tx.send(user_selection).unwrap();
	debug!("go_to_menu sent UserSelection to the TX");
}

#[no_mangle]
pub extern fn go_to_menu_plus_arg(menu_name: *const c_char, arg: i32) {
	debug!("go_to_menu_plus_arg called");
	let tx = {
		TX.lock().unwrap().as_ref().unwrap().clone()
	};
	let rust_string_menu_name = to_rust_string(menu_name);
	debug!("go_to_menu_plus_arg '{}'", rust_string_menu_name);
	let menu = Menu::from(rust_string_menu_name, Some(arg as usize));
	let user_selection = UserSelection::GoTo(menu);
	tx.send(user_selection).unwrap();
	debug!("go_to_menu_plus_arg sent UserSelection to the TX");
}

#[no_mangle]
pub extern fn add_entry(java_entry: &JavaEntry) {
	debug!("add_entry");
	let tx = {
		TX.lock().unwrap().as_ref().unwrap().clone()
	};
	let entry = Entry::new(
		to_rust_string(java_entry.name),
		to_rust_string(java_entry.user),
		to_rust_string(java_entry.pass),
		to_rust_string(java_entry.desc));

	let user_selection = UserSelection::NewEntry(entry);
	tx.send(user_selection).unwrap();
	debug!("add_entry sent UserSelection to the TX");
}

#[no_mangle]
pub extern fn replace_entry(java_entry: &JavaEntry, index: i32) {
	debug!("replace_entry");
	let tx = {
		TX.lock().unwrap().as_ref().unwrap().clone()
	};
	let entry = Entry::new(
		to_rust_string(java_entry.name),
		to_rust_string(java_entry.user),
		to_rust_string(java_entry.pass),
		to_rust_string(java_entry.desc));

	let user_selection = UserSelection::ReplaceEntry(index as usize, entry);
	tx.send(user_selection).unwrap();
	debug!("replace_entry sent UserSelection to the TX");
}

#[no_mangle]
pub extern fn delete_entry(index: i32) {
	debug!("delete_entry");
	let tx = {
		TX.lock().unwrap().as_ref().unwrap().clone()
	};

	let user_selection = UserSelection::DeleteEntry(index as usize);
	tx.send(user_selection).unwrap();
	debug!("delete_entry sent UserSelection to the TX");
}

#[no_mangle]
pub extern fn export_import(path: *const c_char, export: u32, password: *const c_char, number: u32) {
	debug!("export_import");
	let tx = {
		TX.lock().unwrap().as_ref().unwrap().clone()
	};

	let user_selection = if export > 0 {
		debug!("Followed exporting path");
		UserSelection::ExportTo(to_rust_string(path))
	} else {
		debug!("Followed importing path");
		UserSelection::ImportFrom(to_rust_string(path), to_rust_string(password), number as usize)
	};
	tx.send(user_selection).unwrap();
	debug!("export_import sent UserSelection to the TX");
}

#[no_mangle]
pub extern fn drop_java_entry(_: Box<JavaEntry>) {
    // Do nothing here. Because we own the JavaEntry here (we're using a Box) and we're not
    // returning it, Rust will assume we don't want it anymore and clean it up.
}

#[no_mangle]
pub extern fn drop_java_entries_set(_: Box<JavaEntriesSet>) {
    // Do nothing here. Because we own the JavaEntriesSet here (we're using a Box) and we're not
    // returning it, Rust will assume we don't want it anymore and clean it up.
}

fn to_rust_string(pointer: *const c_char) -> String {
    let slice = unsafe { CStr::from_ptr(pointer).to_bytes() };
    str::from_utf8(slice).unwrap().to_string()
}

fn to_java_string(string: String) -> *const c_char {
    let cs = CString::new(string.as_bytes()).unwrap();
    let ptr = cs.as_ptr();
    // Tell Rust not to clean up the string while we still have a pointer to it.
    // Otherwise, we'll get a segfault.
    mem::forget(cs);
    ptr
}
