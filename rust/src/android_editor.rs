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
use std::sync::Mutex;

use async_trait::async_trait;
use j4rs::{Instance, InvocationArg, Jvm};
use log::*;
use rust_keylock::dropbox::DropboxConfiguration;
use rust_keylock::nextcloud::NextcloudConfiguration;
use rust_keylock::{
    AsyncEditor, Entry, EntryPresentationType, GeneralConfiguration, Menu, MessageSeverity,
    UserOption, UserSelection,
};

use crate::{errors, japi};

pub struct AndroidImpl {
    show_menu_cb: Mutex<Instance>,
    show_entry_cb: Mutex<Instance>,
    show_entries_set_cb: Mutex<Instance>,
    show_message_cb: Mutex<Instance>,
    edit_configuration_cb: Mutex<Instance>,
}

impl AndroidImpl {
    fn clone_show_menu_cb(&self) -> errors::Result<Instance> {
        let jvm = Jvm::attach_thread()?;
        let i = self.show_menu_cb.try_lock()?;
        Ok(jvm.clone_instance(&i)?)
    }

    fn clone_show_entries_set_cb(&self) -> errors::Result<Instance> {
        let jvm = Jvm::attach_thread()?;
        let i = self.show_entries_set_cb.try_lock()?;
        Ok(jvm.clone_instance(&i)?)
    }

    fn clone_show_entry_cb(&self) -> errors::Result<Instance> {
        let jvm = Jvm::attach_thread()?;
        let i = self.show_entry_cb.try_lock()?;
        Ok(jvm.clone_instance(&i)?)
    }

    fn clone_edit_configuration_cb(&self) -> errors::Result<Instance> {
        let jvm = Jvm::attach_thread()?;
        let i = self.edit_configuration_cb.try_lock()?;
        Ok(jvm.clone_instance(&i)?)
    }

    fn clone_show_message_cb(&self) -> errors::Result<Instance> {
        let jvm = Jvm::attach_thread()?;
        let i = self.show_message_cb.try_lock()?;
        Ok(jvm.clone_instance(&i)?)
    }
}

pub fn new(
    show_menu_cb: Instance,
    show_entry_cb: Instance,
    show_entries_set_cb: Instance,
    show_message_cb: Instance,
    edit_configuration_cb: Instance,
) -> AndroidImpl {
    AndroidImpl {
        show_menu_cb: Mutex::new(show_menu_cb),
        show_entry_cb: Mutex::new(show_entry_cb),
        show_entries_set_cb: Mutex::new(show_entries_set_cb),
        show_message_cb: Mutex::new(show_message_cb),
        edit_configuration_cb: Mutex::new(edit_configuration_cb),
    }
}

#[async_trait]
impl AsyncEditor for AndroidImpl {
    async fn show_password_enter(&self) -> UserSelection {
        show_password_enter(&self)
            .await
            .unwrap_or_else(|error| handle_error(&error))
    }

    async fn show_change_password(&self) -> UserSelection {
        show_change_password(&self)
            .await
            .unwrap_or_else(|error| handle_error(&error))
    }

    async fn show_menu(&self, menu: Menu) -> UserSelection {
        show_menu(&self, menu)
            .await
            .unwrap_or_else(|error| handle_error(&error))
    }

    async fn show_entries(&self, entries: Vec<Entry>, filter: String) -> UserSelection {
        show_entries(&self, entries, filter)
            .await
            .unwrap_or_else(|error| handle_error(&error))
    }

    async fn show_entry(
        &self,
        entry: Entry,
        index: usize,
        presentation_type: EntryPresentationType,
    ) -> UserSelection {
        show_entry(&self, entry, index, presentation_type)
            .await
            .unwrap_or_else(|error| handle_error(&error))
    }

    async fn show_configuration(
        &self,
        nextcloud: NextcloudConfiguration,
        dropbox: DropboxConfiguration,
        general: GeneralConfiguration,
    ) -> UserSelection {
        show_configuration(&self, nextcloud, dropbox, general)
            .await
            .unwrap_or_else(|error| handle_error(&error))
    }

    async fn exit(&self, contents_changed: bool) -> UserSelection {
        exit(&self, contents_changed)
            .await
            .unwrap_or_else(|error| handle_error(&error))
    }

    async fn show_message(
        &self,
        message: &str,
        options: Vec<UserOption>,
        severity: MessageSeverity,
    ) -> UserSelection {
        show_message(&self, message, options, severity)
            .await
            .unwrap_or_else(|error| handle_error(&error))
    }

    fn start_rest_server(&self) -> bool {
        true
    }
}

async fn show_password_enter(editor: &AndroidImpl) -> errors::Result<UserSelection> {
    debug!("Opening the password fragment");
    let instance_res_future = Jvm::invoke_into_sendable_async(
        editor.clone_show_menu_cb()?,
        "apply".to_string(),
        vec![InvocationArg::try_from("TryPass")?],
    );
    debug!("Waiting for password...");
    japi::handle_instance_receiver_result(instance_res_future).await
}

async fn show_change_password(editor: &AndroidImpl) -> errors::Result<UserSelection> {
    debug!("Opening the change password fragment");
    let instance_res_future = Jvm::invoke_into_sendable_async(
        editor.clone_show_menu_cb()?,
        "apply".to_string(),
        vec![InvocationArg::try_from("ChangePass")?],
    );

    debug!("Waiting for password...");
    japi::handle_instance_receiver_result(instance_res_future).await
}

async fn show_menu(editor: &AndroidImpl, menu: Menu) -> errors::Result<UserSelection> {
    let instance_res_future = match menu {
        Menu::Main => Jvm::invoke_into_sendable_async(
            editor.clone_show_menu_cb()?,
            "apply".to_string(),
            vec![InvocationArg::try_from("Main")?],
        ),
        Menu::NewEntry(ref entry_opt) => {
            let entry = entry_opt.clone().unwrap_or_else(|| Entry::empty());
            let empty_entry = japi::JavaEntry::new(&entry);
            // In order to denote that this is a new entry, put -1 as index
            Jvm::invoke_into_sendable_async(
                editor.clone_show_entry_cb()?,
                "apply".to_string(),
                vec![
                    InvocationArg::new(&empty_entry, "org.astonbitecode.rustkeylock.api.JavaEntry"),
                    InvocationArg::try_from(-1)?,
                    InvocationArg::try_from(true)?,
                    InvocationArg::try_from(false)?,
                ],
            )
        }
        Menu::ExportEntries => Jvm::invoke_into_sendable_async(
            editor.clone_show_menu_cb()?,
            "apply".to_string(),
            vec![InvocationArg::try_from("ExportEntries")?],
        ),
        Menu::ImportEntries => Jvm::invoke_into_sendable_async(
            editor.clone_show_menu_cb()?,
            "apply".to_string(),
            vec![InvocationArg::try_from("ImportEntries")?],
        ),
        Menu::Current => Jvm::invoke_into_sendable_async(
            editor.clone_show_menu_cb()?,
            "apply".to_string(),
            vec![InvocationArg::try_from("Current")?],
        ),
        other => {
            panic!(
                "Menu '{:?}' cannot be used with Entries. Please, consider opening a bug \
                        to the developers.",
                other
            )
        }
    };

    japi::handle_instance_receiver_result(instance_res_future).await
}

async fn show_entries(
    editor: &AndroidImpl,
    entries: Vec<Entry>,
    filter: String,
) -> errors::Result<UserSelection> {
    let java_entries: Vec<japi::JavaEntry> = entries
        .iter()
        .map(|entry| japi::JavaEntry::new(entry))
        .collect();
    let filter = if filter.is_empty() {
        "null".to_string()
    } else {
        filter
    };

    let instance_res_future = Jvm::invoke_into_sendable_async(
        editor.clone_show_entries_set_cb()?,
        "apply".to_string(),
        vec![
            InvocationArg::try_from((
                java_entries.as_slice(),
                "org.astonbitecode.rustkeylock.api.JavaEntry",
            ))?,
            InvocationArg::try_from(filter)?,
        ],
    );

    japi::handle_instance_receiver_result(instance_res_future).await
}

async fn show_entry(
    editor: &AndroidImpl,
    entry: Entry,
    index: usize,
    presentation_type: EntryPresentationType,
) -> errors::Result<UserSelection> {
    let instance_res_future = match presentation_type {
        EntryPresentationType::View => Jvm::invoke_into_sendable_async(
            editor.clone_show_entry_cb()?,
            "apply".to_string(),
            vec![
                InvocationArg::new(
                    &japi::JavaEntry::new(&entry),
                    "org.astonbitecode.rustkeylock.api.JavaEntry",
                ),
                InvocationArg::try_from(index as i32)?,
                InvocationArg::try_from(false)?,
                InvocationArg::try_from(false)?,
            ],
        ),
        EntryPresentationType::Delete => Jvm::invoke_into_sendable_async(
            editor.clone_show_entry_cb()?,
            "apply".to_string(),
            vec![
                InvocationArg::new(
                    &japi::JavaEntry::new(&entry),
                    "org.astonbitecode.rustkeylock.api.JavaEntry",
                ),
                InvocationArg::try_from(index as i32)?,
                InvocationArg::try_from(false)?,
                InvocationArg::try_from(true)?,
            ],
        ),
        EntryPresentationType::Edit => Jvm::invoke_into_sendable_async(
            editor.clone_show_entry_cb()?,
            "apply".to_string(),
            vec![
                InvocationArg::new(
                    &japi::JavaEntry::new(&entry),
                    "org.astonbitecode.rustkeylock.api.JavaEntry",
                ),
                InvocationArg::try_from(index as i32)?,
                InvocationArg::try_from(true)?,
                InvocationArg::try_from(false)?,
            ],
        ),
    };

    japi::handle_instance_receiver_result(instance_res_future).await
}

async fn show_configuration(
    editor: &AndroidImpl,
    nextcloud: NextcloudConfiguration,
    dropbox: DropboxConfiguration,
    general: GeneralConfiguration,
) -> errors::Result<UserSelection> {
    let conf_strings = vec![
        nextcloud.server_url.clone(),
        nextcloud.username.clone(),
        nextcloud.decrypted_password().unwrap().to_string(),
        nextcloud.use_self_signed_certificate.to_string(),
        DropboxConfiguration::dropbox_url(),
        dropbox.decrypted_token().unwrap().to_string(),
        general.browser_extension_token.unwrap_or_default(),
    ];

    let instance_res_future = Jvm::invoke_into_sendable_async(
        editor.clone_edit_configuration_cb()?,
        "apply".to_string(),
        vec![InvocationArg::try_from(conf_strings.as_slice())?],
    );
    japi::handle_instance_receiver_result(instance_res_future).await
}

async fn exit(editor: &AndroidImpl, contents_changed: bool) -> errors::Result<UserSelection> {
    debug!("Exiting rust-keylock...");
    if contents_changed {
        let instance_res_future = Jvm::invoke_into_sendable_async(
            editor.clone_show_menu_cb()?,
            "apply".to_string(),
            vec![InvocationArg::try_from("Exit")?],
        );

        japi::handle_instance_receiver_result(instance_res_future).await
    } else {
        Ok(UserSelection::GoTo(Menu::ForceExit))
    }
}

async fn show_message(
    editor: &AndroidImpl,
    message: &str,
    options: Vec<UserOption>,
    severity: MessageSeverity,
) -> errors::Result<UserSelection> {
    debug!("Showing Message '{}'", message);
    let java_user_options: Vec<japi::JavaUserOption> = options
        .iter()
        .clone()
        .map(|user_option| japi::JavaUserOption::new(user_option))
        .collect();

    let instance_res_future = Jvm::invoke_into_sendable_async(
        editor.clone_show_message_cb()?,
        "apply".to_string(),
        vec![
            InvocationArg::try_from((
                java_user_options.as_slice(),
                "org.astonbitecode.rustkeylock.api.JavaUserOption",
            ))?,
            InvocationArg::try_from(message)?,
            InvocationArg::try_from(severity.to_string())?,
        ],
    );

    japi::handle_instance_receiver_result(instance_res_future).await
}

fn handle_error(error: &errors::RklAndroidError) -> UserSelection {
    error!("An error occured: {:?}", error);
    UserSelection::GoTo(Menu::Main)
}
