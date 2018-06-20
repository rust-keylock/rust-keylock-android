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
use rust_keylock::{Editor, UserSelection, Menu, Safe, UserOption, MessageSeverity,
                   RklConfiguration};
use super::{StringCallback, StringListCallback, ShowEntryCallback, ShowEntriesSetCallback,
            LogCallback, logger, JavaEntriesSet, JavaEntry, ShowMessageCallback,
            JavaUserOptionsSet, StringList};
use std::sync::mpsc::{Receiver, Sender};
use std::sync::Mutex;

pub struct AndroidImpl {
    show_menu_cb: StringCallback,
    show_entry_cb: ShowEntryCallback,
    show_entries_set_cb: ShowEntriesSetCallback,
    show_message_cb: ShowMessageCallback,
    edit_configuration_cb: StringListCallback,
    rx: Receiver<UserSelection>,
    tx: Sender<UserSelection>,
    previous_menu: Mutex<Option<Menu>>,
}

pub fn new(show_menu_cb: StringCallback,
           show_entry_cb: ShowEntryCallback,
           show_entries_set_cb: ShowEntriesSetCallback,
           show_message_cb: ShowMessageCallback,
           edit_configuration_cb: StringListCallback,
           log_cb: LogCallback,
           rx: Receiver<UserSelection>,
           tx: Sender<UserSelection>)
           -> AndroidImpl {

    // Initialize the Android logger
    logger::init(log_cb);
    // Return the Editor
    AndroidImpl {
        show_menu_cb: show_menu_cb,
        show_entry_cb: show_entry_cb,
        show_entries_set_cb: show_entries_set_cb,
        show_message_cb: show_message_cb,
        edit_configuration_cb: edit_configuration_cb,
        rx: rx,
        tx: tx,
        previous_menu: Mutex::new(None),
    }
}

impl AndroidImpl {
    fn update_internal_state(&self, menu: &UserSelection) {
        match menu {
            &UserSelection::GoTo(ref menu) => { self.update_menu(menu.clone()) }
            _ => {
                // ignore
            }
        }
    }

    fn update_menu(&self, menu: Menu) {
        match self.previous_menu.lock() {
            Ok(mut previous_menu_mut) => {
                *previous_menu_mut = Some(menu);
            }
            Err(error) => {
                warn!("Warning! Could not update the internal state. Reason: {:?}", error);
            }
        };
    }

    fn previous_menu(&self) -> Option<Menu> {
        match self.previous_menu.lock() {
            Ok(previous_menu_mut) => {
                previous_menu_mut.clone()
            }
            Err(error) => {
                warn!("Warning! Could not retrieve the internal state. Reason: {:?}", error);
                Some(Menu::Main)
            }
        }
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

    fn show_menu(&self,
                 menu: &Menu,
                 safe: &Safe,
                 configuration: &RklConfiguration)
                 -> UserSelection {
        debug!("Opening menu '{:?}' with entries size {}",
               menu,
               safe.get_entries().len());

        match menu {
            &Menu::Main => (self.show_menu_cb)(super::to_java_string(Menu::Main.get_name())),
            &Menu::EntriesList(_) => {
                let java_entries_set = if safe.get_entries().len() == 0 {
                    JavaEntriesSet::with_nulls()
                } else {
                    JavaEntriesSet::from(safe.get_entries())
                };

                let filter_ptr = if safe.get_filter().len() == 0 {
                    super::to_java_string("null".to_string())
                } else {
                    super::to_java_string(safe.get_filter().clone())
                };

                (self.show_entries_set_cb)(Box::new(java_entries_set), filter_ptr);
            }
            &Menu::ShowEntry(index) => {
                let entry = safe.get_entry_decrypted(index);
                (self.show_entry_cb)(Box::new(JavaEntry::new(&entry)), index as i32, false, false);
            }
            &Menu::DeleteEntry(index) => {
                let ref entry = safe.get_entry(index);
                (self.show_entry_cb)(Box::new(JavaEntry::new(&entry)), index as i32, false, true);
            }
            &Menu::NewEntry => {
                let empty_entry = JavaEntry::empty();
                // In order to denote that this is a new entry, put -1 as index
                (self.show_entry_cb)(Box::new(empty_entry), -1, true, false);
            }
            &Menu::EditEntry(index) => {
                let ref selected_entry = safe.get_entry_decrypted(index);
                (self.show_entry_cb)(Box::new(JavaEntry::new(selected_entry)),
                                     index as i32,
                                     true,
                                     false);
            }
            &Menu::ExportEntries => {
                (self.show_menu_cb)(super::to_java_string(Menu::ExportEntries.get_name()))
            }
            &Menu::ImportEntries => {
                (self.show_menu_cb)(super::to_java_string(Menu::ImportEntries.get_name()))
            }
            &Menu::ShowConfiguration => {
                let conf_strings =
                    vec![configuration.nextcloud.server_url.clone(),
                         configuration.nextcloud.username.clone(),
                         configuration.nextcloud.decrypted_password().unwrap(),
                         configuration.nextcloud.use_self_signed_certificate.to_string()];
                (self.edit_configuration_cb)(Box::new(StringList::from(conf_strings)))
            }
            &Menu::Current => {
                let _ = self.tx.send(UserSelection::GoTo(self.previous_menu().unwrap_or(Menu::Main)));
            }
            other => {
                panic!("Menu '{:?}' cannot be used with Entries. Please, consider opening a bug \
                        to the developers.",
                       other)
            }
        };

        debug!("Waiting for User Input from {:?}", menu);
        let usin = match self.rx.recv() {
            Ok(u) => u,
            Err(error) => {
                error!("Error while receiving User Input: {:?}", error);
                UserSelection::GoTo(Menu::Main)
            }
        };
        self.update_internal_state(&usin);
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
        } else {
            UserSelection::GoTo(Menu::ForceExit)
        }
    }

    fn show_message(&self,
                    message: &str,
                    options: Vec<UserOption>,
                    severity: MessageSeverity)
                    -> UserSelection {
        debug!("Showing Message '{}'", message);
        let java_options_set = if options.len() == 0 {
            JavaUserOptionsSet::with_nulls()
        } else {
            JavaUserOptionsSet::from(&options[..])
        };
        (self.show_message_cb)(Box::new(java_options_set),
                               super::to_java_string(message.to_string()),
                               super::to_java_string(severity.to_string()));
        let user_selection = self.rx.recv().unwrap();
        user_selection
    }
}
