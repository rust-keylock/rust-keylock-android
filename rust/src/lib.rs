// Copyright 2017 astonbitecode
// This file is part of rust-keylock password manager.
//
// rust-keylock is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// rust-keylock is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with rust-keylock.  If not, see <http://www.gnu.org/licenses/>.

extern crate android_logger;
extern crate j4rs;
extern crate jni_sys;
extern crate lazy_static;
extern crate libc;
extern crate log;
extern crate rust_keylock;
extern crate serde;
extern crate serde_json;

use std::ffi::CString;
use std::str;

use jni_sys::{JavaVM, jint, JNI_VERSION_1_6, JNIEnv, jobject};
use j4rs::prelude::*;
use j4rs_derive::*;
use libc::c_char;
use log::*;

mod android_editor;
mod logger;
mod japi;
mod errors;

#[allow(non_snake_case)]
#[no_mangle]
pub extern fn JNI_OnLoad(env: *mut JavaVM, _reserved: jobject) -> jint {
    logger::init();
    j4rs::set_java_vm(env);
    debug!("JNI_OnLoad completed!");
    JNI_VERSION_1_6
}

#[allow(non_snake_case)]
#[call_from_java("org.astonbitecode.rustkeylock.api.InterfaceWithRust.execute")]
pub fn execute(cert_file_path_java_string: Instance) {
    debug!("Executing rust-keylock native");
    match j4rs::JvmBuilder::new()
        .detach_thread_on_drop(false)
        .with_no_implicit_classpath()
        .with_native_lib_name("rustkeylockandroid")
        .build() {
        Ok(jvm) => {
            debug!("JVM is created ");
            let cert_file_path_java_string:j4rs::errors::Result<String> = jvm.to_rust(cert_file_path_java_string);
            if let Ok(cert_file_path) = cert_file_path_java_string {
                ::std::env::set_var("SSL_CERT_FILE", cert_file_path);
            }

            match (jvm.create_instance("org.astonbitecode.rustkeylock.callbacks.ShowMenuCb", &Vec::new()),
                   jvm.create_instance("org.astonbitecode.rustkeylock.callbacks.ShowEntryCb", &Vec::new()),
                   jvm.create_instance("org.astonbitecode.rustkeylock.callbacks.ShowEntriesSetCb", &Vec::new()),
                   jvm.create_instance("org.astonbitecode.rustkeylock.callbacks.ShowMessageCb", &Vec::new()),
                   jvm.create_instance("org.astonbitecode.rustkeylock.callbacks.EditConfigurationCb", &Vec::new())) {
                (Ok(show_menu_cb), Ok(show_entry_cb), Ok(show_entries_set_cb), Ok(show_message_cb), Ok(edit_configuration_cb)) => {
                    let editor = android_editor::new(jvm,
                                                     show_menu_cb,
                                                     show_entry_cb,
                                                     show_entries_set_cb,
                                                     show_message_cb,
                                                     edit_configuration_cb);
                    debug!("Executing native rust_keylock!");
                    rust_keylock::execute_async(Box::new(editor))
                }
                (_, _, _, _, _) => {
                    error!("Could not instantiate the Java World callbacks")
                }
            }
        }
        Err(error) => error!("Could not execute native rust_keylock: {:?}", error)
    }
}

pub fn to_java_string(string: &str) -> *mut c_char {
    let cs = CString::new(string.as_bytes()).unwrap();
    cs.into_raw()
}
