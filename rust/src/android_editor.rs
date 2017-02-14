use rust_keylock::{ Entry, Editor, UserSelection, Menu };
use super::{StringCallback, ShowEntryCallback, ShowEntriesSetCallback, LogCallback, logger, JavaEntriesSet, JavaEntry};
use std::sync::mpsc::Receiver;

pub struct AndroidImpl {
	show_menu_cb: StringCallback,
	show_entry_cb: ShowEntryCallback,
	show_entries_set_cb: ShowEntriesSetCallback,
	show_message_cb: StringCallback,
	rx: Receiver<UserSelection>,
}

pub fn new(show_menu_cb: StringCallback,
	show_entry_cb: ShowEntryCallback,
	show_entries_set_cb: ShowEntriesSetCallback,
	show_message_cb: StringCallback,
	log_cb: LogCallback,
	rx: Receiver<UserSelection>) -> AndroidImpl {

	// Initialize the Android logger
	logger::init(log_cb);
	// Return the Editor
	AndroidImpl {
		show_menu_cb: show_menu_cb,
		show_entry_cb: show_entry_cb,
		show_entries_set_cb: show_entries_set_cb,
		show_message_cb: show_message_cb,
		rx: rx,
	}
}

impl Editor for AndroidImpl {
	fn show_password_enter(&self) -> UserSelection {
		debug!("Opening the password fragment");
		let try_pass_menu_name = Menu::TryPass.get_name();
		(self.show_menu_cb)(super::to_java_string(try_pass_menu_name));
		debug!("Waiting for password...");
		let user_selection = self.rx.recv().unwrap();
		user_selection
	}

	fn show_change_password(&self) -> UserSelection {
		debug!("Opening the change password fragment");
		let change_pass_menu_name = Menu::ChangePass.get_name();
		(self.show_menu_cb)(super::to_java_string(change_pass_menu_name));
		debug!("Waiting for password...");
		let user_selection = self.rx.recv().unwrap();
		user_selection
	}

	fn show_menu(&self, menu: &Menu, entries: &[Entry]) -> UserSelection {
		debug!("Opening menu '{:?}' with entries size {}", menu, entries.len());

		match menu {
			&Menu::Main => (self.show_menu_cb)(super::to_java_string(Menu::Main.get_name())),
			&Menu::EntriesList => {
					let java_entries_set = if entries.len() == 0 {
					JavaEntriesSet::with_nulls()
				} else {
					JavaEntriesSet::from(entries)
				};

				(self.show_entries_set_cb)(java_entries_set);
			},
			&Menu::ShowEntry(index) => {
				let ref entry = entries[index];
				(self.show_entry_cb)(Box::new(JavaEntry::new(entry)), index as i32, false, false);
			},
			&Menu::DeleteEntry(index) => {
				let ref entry = entries[index];
				(self.show_entry_cb)(Box::new(JavaEntry::new(entry)), index as i32, false, true);
			},
			&Menu::NewEntry => {
				let empty_entry = JavaEntry::empty();
				// In order to denote that this is a new entry, put -1 as index
				(self.show_entry_cb)(Box::new(empty_entry), -1, true, false);
			},
			&Menu::EditEntry(index) => {
				let ref selected_entry = entries[index];
				(self.show_entry_cb)(Box::new(JavaEntry::new(selected_entry)), index as i32, true, false);
			},
			&Menu::ExportEntries => {
				(self.show_menu_cb)(super::to_java_string(Menu::ExportEntries.get_name()))
			},
			&Menu::ImportEntries => {
				(self.show_menu_cb)(super::to_java_string(Menu::ImportEntries.get_name()))
			},
			other => panic!("Menu '{:?}' cannot be used with Entries. Please, consider opening a bug to the developers.", other),
		};

		debug!("Waiting for User Input from {:?}", menu);
		let usin = match self.rx.recv() {
			Ok(u) => u,
			Err(error) => {
				error!("Error while receiving User Input: {:?}", error);
				UserSelection::GoTo(Menu::Main)
			},
		};
		debug!("Proceeding after receiving User Input from {:?}", menu);
		usin
	}

	fn exit(&self, contents_changed: bool) -> UserSelection {
		debug!("Exiting rust-keylock...");
		if contents_changed {
			let menu_name = Menu::Exit.get_name();
			(self.show_menu_cb)(super::to_java_string(menu_name));
			let user_selection = self.rx.recv().unwrap();
			user_selection
		}
		else {
			UserSelection::GoTo(Menu::ForceExit)
		}
	}

	fn show_message(&self, message: &'static str) -> UserSelection {
		debug!("Showing Message '{}'", message);
		(self.show_message_cb)(super::to_java_string(message.to_string()));
		let user_selection = self.rx.recv().unwrap();
		user_selection
	}
}
