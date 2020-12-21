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

use std::convert::TryFrom;
use std::sync::mpsc::{self, Receiver};

use j4rs::{Instance, InvocationArg, Jvm};
use log::*;
use rust_keylock::{AsyncEditor, Entry, EntryPresentationType, Menu, MessageSeverity, UserOption, UserSelection};
use rust_keylock::dropbox::DropboxConfiguration;
use rust_keylock::nextcloud::NextcloudConfiguration;

use crate::{japi, errors};
use crate::errors::RklAndroidError;

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
        show_password_enter(&self).unwrap_or_else(|error| handle_error(&error))
    }

    fn show_change_password(&self) -> Receiver<UserSelection> {
        show_change_password(&self).unwrap_or_else(|error| handle_error(&error))
    }

    fn show_menu(&self, menu: &Menu) -> Receiver<UserSelection> {
        show_menu(&self, menu).unwrap_or_else(|error| handle_error(&error))
    }

    fn show_entries(&self, entries: Vec<Entry>, filter: String) -> Receiver<UserSelection> {
        show_entries(&self, entries, filter).unwrap_or_else(|error| handle_error(&error))
    }

    fn show_entry(&self, entry: Entry, index: usize, presentation_type: EntryPresentationType) -> Receiver<UserSelection> {
        show_entry(&self, entry, index, presentation_type).unwrap_or_else(|error| handle_error(&error))
    }

    fn show_configuration(&self, nextcloud: NextcloudConfiguration, dropbox: DropboxConfiguration) -> Receiver<UserSelection> {
        show_configuration(&self, nextcloud, dropbox).unwrap_or_else(|error| handle_error(&error))
    }

    fn exit(&self, contents_changed: bool) -> Receiver<UserSelection> {
        exit(&self, contents_changed).unwrap_or_else(|error| handle_error(&error))
    }

    fn show_message(&self,
                    message: &str,
                    options: Vec<UserOption>,
                    severity: MessageSeverity)
                    -> Receiver<UserSelection> {
        show_message(&self, message, options, severity).unwrap_or_else(|error| handle_error(&error))
    }
}

fn show_password_enter(editor: &AndroidImpl) -> errors::Result<Receiver<UserSelection>> {
    debug!("Opening the password fragment");
    let instance_receiver = editor.jvm.invoke_to_channel(
        &editor.show_menu_cb,
        "apply",
        &[InvocationArg::try_from("TryPass")?]);
    debug!("Waiting for password...");
    japi::handle_instance_receiver_result(instance_receiver)
}

fn show_change_password(editor: &AndroidImpl) -> errors::Result<Receiver<UserSelection>> {
    debug!("Opening the change password fragment");
    let instance_receiver = editor.jvm.invoke_to_channel(
        &editor.show_menu_cb,
        "apply",
        &[InvocationArg::try_from("ChangePass")?]);
    debug!("Waiting for password...");
    japi::handle_instance_receiver_result(instance_receiver)
}

fn show_menu(editor: &AndroidImpl, menu: &Menu) -> errors::Result<Receiver<UserSelection>> {
    let instance_receiver_res = match menu {
        &Menu::Main => {
            editor.jvm.invoke_to_channel(
                &editor.show_menu_cb,
                "apply",
                &[InvocationArg::try_from("Main")?])
        }
        &Menu::NewEntry(ref entry_opt) => {
            let entry = entry_opt.clone().unwrap_or_else(|| Entry::empty());
            let empty_entry = japi::JavaEntry::new(&entry);
            // In order to denote that this is a new entry, put -1 as index
            editor.jvm.invoke_to_channel(
                &editor.show_entry_cb,
                "apply",
                &[
                    InvocationArg::new(&empty_entry, "org.astonbitecode.rustkeylock.api.JavaEntry"),
                    InvocationArg::try_from(-1)?,
                    InvocationArg::try_from(true)?,
                    InvocationArg::try_from(false)?
                ])
        }
        &Menu::ExportEntries => {
            editor.jvm.invoke_to_channel(
                &editor.show_menu_cb,
                "apply",
                &[InvocationArg::try_from("ExportEntries")?])
        }
        &Menu::ImportEntries => {
            editor.jvm.invoke_to_channel(
                &editor.show_menu_cb,
                "apply",
                &[InvocationArg::try_from("ImportEntries")?])
        }
        &Menu::Current => {
            editor.jvm.invoke_to_channel(
                &editor.show_menu_cb,
                "apply",
                &[InvocationArg::try_from("Current")?])
        }
        other => {
            panic!("Menu '{:?}' cannot be used with Entries. Please, consider opening a bug \
                        to the developers.",
                   other)
        }
    };

    japi::handle_instance_receiver_result(instance_receiver_res)
}

fn show_entries(editor: &AndroidImpl, entries: Vec<Entry>, filter: String) -> errors::Result<Receiver<UserSelection>> {
    let java_entries: Vec<japi::JavaEntry> = entries.iter()
        .map(|entry| japi::JavaEntry::new(entry))
        .collect();
    let filter = if filter.is_empty() {
        "null".to_string()
    } else {
        filter
    };

    let instance_receiver_res = editor.jvm.invoke_to_channel(
        &editor.show_entries_set_cb,
        "apply",
        &[
            InvocationArg::try_from((
                java_entries.as_slice(),
                "org.astonbitecode.rustkeylock.api.JavaEntry"))?,
            InvocationArg::try_from(filter)?]);
    japi::handle_instance_receiver_result(instance_receiver_res)
}

fn show_entry(editor: &AndroidImpl, entry: Entry, index: usize, presentation_type: EntryPresentationType) -> errors::Result<Receiver<UserSelection>> {
    let instance_receiver_res = match presentation_type {
        EntryPresentationType::View => {
            editor.jvm.invoke_to_channel(
                &editor.show_entry_cb,
                "apply",
                &[
                    InvocationArg::new(&japi::JavaEntry::new(&entry), "org.astonbitecode.rustkeylock.api.JavaEntry"),
                    InvocationArg::try_from(index as i32)?,
                    InvocationArg::try_from(false)?,
                    InvocationArg::try_from(false)?
                ])
        }
        EntryPresentationType::Delete => {
            editor.jvm.invoke_to_channel(
                &editor.show_entry_cb,
                "apply",
                &[
                    InvocationArg::new(&japi::JavaEntry::new(&entry), "org.astonbitecode.rustkeylock.api.JavaEntry"),
                    InvocationArg::try_from(index as i32)?,
                    InvocationArg::try_from(false)?,
                    InvocationArg::try_from(true)?
                ])
        }
        EntryPresentationType::Edit => {
            editor.jvm.invoke_to_channel(
                &editor.show_entry_cb,
                "apply",
                &[
                    InvocationArg::new(&japi::JavaEntry::new(&entry), "org.astonbitecode.rustkeylock.api.JavaEntry"),
                    InvocationArg::try_from(index as i32)?,
                    InvocationArg::try_from(true)?,
                    InvocationArg::try_from(false)?
                ])
        }
    };

    japi::handle_instance_receiver_result(instance_receiver_res)
}

fn show_configuration(editor: &AndroidImpl, nextcloud: NextcloudConfiguration, dropbox: DropboxConfiguration) -> errors::Result<Receiver<UserSelection>> {
    let conf_strings = vec![
        nextcloud.server_url.clone(),
        nextcloud.username.clone(),
        nextcloud.decrypted_password().unwrap().to_string(),
        nextcloud.use_self_signed_certificate.to_string(),
        DropboxConfiguration::dropbox_url(),
        dropbox.decrypted_token().unwrap().to_string()];
    let instance_receiver_res = editor.jvm.invoke_to_channel(
        &editor.edit_configuration_cb,
        "apply",
        &[InvocationArg::try_from(conf_strings.as_slice())?]);
    japi::handle_instance_receiver_result(instance_receiver_res)
}

fn exit(editor: &AndroidImpl, contents_changed: bool) -> errors::Result<Receiver<UserSelection>> {
    debug!("Exiting rust-keylock...");
    if contents_changed {
        let instance_receiver = editor.jvm.invoke_to_channel(
            &editor.show_menu_cb,
            "apply",
            &[InvocationArg::try_from("Exit")?]);

        japi::handle_instance_receiver_result(instance_receiver)
    } else {
        let (tx, rx) = mpsc::channel();
        let _ = tx.send(UserSelection::GoTo(Menu::ForceExit));
        Ok(rx)
    }
}

fn show_message(editor: &AndroidImpl,
                message: &str,
                options: Vec<UserOption>,
                severity: MessageSeverity)
                -> errors::Result<Receiver<UserSelection>> {
    debug!("Showing Message '{}'", message);
    let java_user_options: Vec<japi::JavaUserOption> = options.iter()
        .clone()
        .map(|user_option| japi::JavaUserOption::new(user_option))
        .collect();
    let instance_receiver = editor.jvm.invoke_to_channel(
        &editor.show_message_cb,
        "apply",
        &[
            InvocationArg::try_from((
                java_user_options.as_slice(),
                "org.astonbitecode.rustkeylock.api.JavaUserOption"))?,
            InvocationArg::try_from(message)?,
            InvocationArg::try_from(severity.to_string())?]);

    japi::handle_instance_receiver_result(instance_receiver)
}

fn handle_error(error: &RklAndroidError) -> Receiver<UserSelection> {
    error!("An error occured: {}", error);
    error!("{:?}", error);
    let (tx, rx) = mpsc::channel();
    let _ = tx.send(UserSelection::GoTo(Menu::Main));
    rx
}