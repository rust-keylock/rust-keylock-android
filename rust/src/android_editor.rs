use std::sync::mpsc::{self, Receiver};

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
use j4rs::{Instance, InvocationArg, Jvm};
use log::*;

use rust_keylock::{AsyncEditor, Entry, EntryPresentationType, Menu, MessageSeverity, UserOption, UserSelection};
use rust_keylock::dropbox::DropboxConfiguration;
use rust_keylock::nextcloud::NextcloudConfiguration;

use super::japi;

pub struct AndroidImpl {
    jvm: Jvm,
    show_menu_cb: Instance,
    show_entry_cb: Instance,
    show_entries_set_cb: Instance,
    show_message_cb: Instance,
    edit_configuration_cb: Instance,
}

pub fn new(jvm: Jvm,
           show_menu_cb: Instance,
           show_entry_cb: Instance,
           show_entries_set_cb: Instance,
           show_message_cb: Instance,
           edit_configuration_cb: Instance)
           -> AndroidImpl {
    AndroidImpl {
        jvm,
        show_menu_cb,
        show_entry_cb,
        show_entries_set_cb,
        show_message_cb,
        edit_configuration_cb,
    }
}

impl AsyncEditor for AndroidImpl {
    fn show_password_enter(&self) -> Receiver<UserSelection> {
        debug!("Opening the password fragment");
        let instance_receiver = self.jvm.invoke_to_channel(
            &self.show_menu_cb,
            "apply",
            &[InvocationArg::from("TryPass")]);
        debug!("Waiting for password...");
        japi::handle_instance_receiver_result(instance_receiver)
    }

    fn show_change_password(&self) -> Receiver<UserSelection> {
        debug!("Opening the change password fragment");
        let instance_receiver = self.jvm.invoke_to_channel(
            &self.show_menu_cb,
            "apply",
            &[InvocationArg::from("ChangePass")]);
        debug!("Waiting for password...");
        japi::handle_instance_receiver_result(instance_receiver)
    }

    fn show_menu(&self, menu: &Menu) -> Receiver<UserSelection> {
        debug!("Opening menu '{:?}'", menu);

        let instance_receiver_res = match menu {
            &Menu::Main => {
                self.jvm.invoke_to_channel(
                    &self.show_menu_cb,
                    "apply",
                    &[InvocationArg::from("Main")])
            }
            &Menu::NewEntry => {
                let empty_entry = japi::JavaEntry::empty();
                // In order to denote that this is a new entry, put -1 as index
                self.jvm.invoke_to_channel(
                    &self.show_entry_cb,
                    "apply",
                    &[
                        InvocationArg::new(&empty_entry, "org.astonbitecode.rustkeylock.api.JavaEntry"),
                        InvocationArg::from(-1),
                        InvocationArg::from(true),
                        InvocationArg::from(false)
                    ])
            }
            &Menu::ExportEntries => {
                self.jvm.invoke_to_channel(
                    &self.show_menu_cb,
                    "apply",
                    &[InvocationArg::from("ExportEntries")])
            }
            &Menu::ImportEntries => {
                self.jvm.invoke_to_channel(
                    &self.show_menu_cb,
                    "apply",
                    &[InvocationArg::from("ImportEntries")])
            }
            &Menu::Current => {
                self.jvm.invoke_to_channel(
                    &self.show_menu_cb,
                    "apply",
                    &[InvocationArg::from("Current")])
            }
            other => {
                panic!("Menu '{:?}' cannot be used with Entries. Please, consider opening a bug \
                        to the developers.",
                       other)
            }
        };

        japi::handle_instance_receiver_result(instance_receiver_res)
    }

    fn show_entries(&self, entries: Vec<Entry>, filter: String) -> Receiver<UserSelection> {
        let java_entries: Vec<japi::JavaEntry> = entries.iter()
            .map(|entry| japi::JavaEntry::new(entry))
            .collect();
        let filter = if filter.is_empty() {
            "null".to_string()
        } else {
            filter
        };

        let instance_receiver_res = self.jvm.invoke_to_channel(
            &self.show_entries_set_cb,
            "apply",
            &[
                InvocationArg::from((
                    java_entries.as_slice(),
                    "org.astonbitecode.rustkeylock.api.JavaEntry",
                    &self.jvm)),
                InvocationArg::from(filter)]);
        japi::handle_instance_receiver_result(instance_receiver_res)
    }

    fn show_entry(&self, entry: Entry, index: usize, presentation_type: EntryPresentationType) -> Receiver<UserSelection> {
        let instance_receiver_res = match presentation_type {
            EntryPresentationType::View => {
                self.jvm.invoke_to_channel(
                    &self.show_entry_cb,
                    "apply",
                    &[
                        InvocationArg::new(&japi::JavaEntry::new(&entry), "org.astonbitecode.rustkeylock.api.JavaEntry"),
                        InvocationArg::from(index as i32),
                        InvocationArg::from(false),
                        InvocationArg::from(false)
                    ])
            }
            EntryPresentationType::Delete => {
                self.jvm.invoke_to_channel(
                    &self.show_entry_cb,
                    "apply",
                    &[
                        InvocationArg::new(&japi::JavaEntry::new(&entry), "org.astonbitecode.rustkeylock.api.JavaEntry"),
                        InvocationArg::from(index as i32),
                        InvocationArg::from(false),
                        InvocationArg::from(true)
                    ])
            }
            EntryPresentationType::Edit => {
                self.jvm.invoke_to_channel(
                    &self.show_entry_cb,
                    "apply",
                    &[
                        InvocationArg::new(&japi::JavaEntry::new(&entry), "org.astonbitecode.rustkeylock.api.JavaEntry"),
                        InvocationArg::from(index as i32),
                        InvocationArg::from(true),
                        InvocationArg::from(false)
                    ])
            }
        };

        japi::handle_instance_receiver_result(instance_receiver_res)
    }

    fn show_configuration(&self, nextcloud: NextcloudConfiguration, dropbox: DropboxConfiguration) -> Receiver<UserSelection> {
        let conf_strings = vec![
            nextcloud.server_url.clone(),
            nextcloud.username.clone(),
            nextcloud.decrypted_password().unwrap(),
            nextcloud.use_self_signed_certificate.to_string(),
            DropboxConfiguration::dropbox_url(),
            dropbox.decrypted_token().unwrap()];
        let instance_receiver_res = self.jvm.invoke_to_channel(
            &self.edit_configuration_cb,
            "apply",
            &[InvocationArg::from((conf_strings.as_slice(), &self.jvm))]);
        japi::handle_instance_receiver_result(instance_receiver_res)
    }

    fn exit(&self, contents_changed: bool) -> Receiver<UserSelection> {
        debug!("Exiting rust-keylock...");
        if contents_changed {
            let instance_receiver = self.jvm.invoke_to_channel(
                &self.show_menu_cb,
                "apply",
                &[InvocationArg::from("Exit")]);

            japi::handle_instance_receiver_result(instance_receiver)
        } else {
            let (tx, rx) = mpsc::channel();
            let _ = tx.send(UserSelection::GoTo(Menu::ForceExit));
            rx
        }
    }

    fn show_message(&self,
                    message: &str,
                    options: Vec<UserOption>,
                    severity: MessageSeverity)
                    -> Receiver<UserSelection> {
        debug!("Showing Message '{}'", message);
        let java_user_options: Vec<japi::JavaUserOption> = options.iter()
            .clone()
            .map(|user_option| japi::JavaUserOption::new(user_option))
            .collect();
        let instance_receiver = self.jvm.invoke_to_channel(
            &self.show_message_cb,
            "apply",
            &[
                InvocationArg::from((
                    java_user_options.as_slice(),
                    "org.astonbitecode.rustkeylock.api.JavaUserOption",
                    &self.jvm)),
                InvocationArg::from(message),
                InvocationArg::from(severity.to_string())]);

        japi::handle_instance_receiver_result(instance_receiver)
    }
}
