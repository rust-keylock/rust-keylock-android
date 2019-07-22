use std::sync::mpsc::{self, Receiver};
use std::thread;

use j4rs::{errors, Instance, InstanceReceiver};
use log::*;
use rust_keylock::{AllConfigurations, Entry, Menu, UserOption, UserSelection};
use rust_keylock::dropbox::DropboxConfiguration;
use rust_keylock::nextcloud::NextcloudConfiguration;
use serde_derive::{Deserialize, Serialize};

pub fn handle_instance_receiver_result(instance_receiver_res: errors::Result<InstanceReceiver>) -> Receiver<UserSelection> {
    let (tx, rx) = mpsc::channel();
    let _ = thread::spawn(move || {
        let sel = retrieve_user_selection(instance_receiver_res);
        let _ = tx.send(sel);
    });

    rx
}

fn retrieve_user_selection(instance_receiver_res: errors::Result<InstanceReceiver>) -> UserSelection {
    match instance_receiver_res {
        Ok(instance_receiver) => {
            match instance_receiver.rx().recv() {
                Ok(instance) => {
                    instance_to_gui_response(instance)
                }
                Err(error) => {
                    error!("Error while retrieving Instance: {:?}", error);
                    UserSelection::GoTo(Menu::Main)
                }
            }
        }
        Err(error) => {
            error!("Error while invoking invoke_to_channel: {:?}", error);
            UserSelection::GoTo(Menu::Main)
        }
    }
}

#[derive(Deserialize, Debug)]
enum GuiResponse {
    ProvidedPassword { password: String, number: usize },
    GoToMenu { menu: JavaMenu },
    AddEntry { entry: JavaEntry },
    ReplaceEntry { entry: JavaEntry, index: usize },
    DeleteEntry { index: usize },
    SetConfiguration { strings: Vec<String> },
    UserOptionSelected { user_option: JavaUserOption },
    ExportImport { path: String, mode: usize, password: String, number: usize },
    Copy { data: String },
}

fn instance_to_gui_response(instance: Instance) -> UserSelection {
    let jvm = j4rs::Jvm::attach_thread().unwrap();
    let res = jvm.to_rust(instance);
    if let Ok(gr) = res {
        match gr {
            GuiResponse::ProvidedPassword { password, number } => {
                UserSelection::ProvidedPassword(password, number)
            }
            GuiResponse::GoToMenu { menu } => {
                debug!("go_to_menu");
                UserSelection::GoTo(menu.to_menu())
            }
            GuiResponse::AddEntry { entry } => {
                debug!("add_entry");
                let entry = Entry::new(entry.name,
                                       entry.url,
                                       entry.user,
                                       entry.pass,
                                       entry.desc);

                UserSelection::NewEntry(entry)
            }
            GuiResponse::ReplaceEntry { entry, index } => {
                debug!("replace_entry");
                let entry = Entry::new(entry.name,
                                       entry.url,
                                       entry.user,
                                       entry.pass,
                                       entry.desc);

                UserSelection::ReplaceEntry(index as usize, entry)
            }
            GuiResponse::DeleteEntry { index } => {
                debug!("delete_entry");
                UserSelection::DeleteEntry(index)
            }
            GuiResponse::SetConfiguration { strings } => {
                debug!("set_configuration with {} elements", strings.len());

                let ncc = if strings.len() == 4 {
                    let b = match strings[3].as_ref() {
                        "true" => true,
                        _ => false,
                    };
                    NextcloudConfiguration::new(strings[0].clone(),
                                                strings[1].clone(),
                                                strings[2].clone(),
                                                b)
                } else {
                    NextcloudConfiguration::new("Wrong Java Data".to_string().to_string(),
                                                "Wrong Java Data".to_string(),
                                                "Wrong Java Data".to_string(),
                                                false)
                };

                let dbxc = if strings.len() == 4 {
                    DropboxConfiguration::new(strings[3].clone())
                } else {
                    Ok(DropboxConfiguration::default())
                };

                UserSelection::UpdateConfiguration(AllConfigurations::new(ncc.unwrap(), dbxc.unwrap()))
            }
            GuiResponse::UserOptionSelected { user_option } => {
                debug!("user_option_selected");

                UserSelection::UserOption(
                    UserOption::from((
                        user_option.label,
                        user_option.value,
                        user_option.short_label)
                    )
                )
            }
            GuiResponse::ExportImport { path, mode, password, number } => {
                debug!("export_import");

                if mode > 0 {
                    debug!("Followed exporting path");
                    UserSelection::ExportTo(path)
                } else {
                    debug!("Followed importing path");
                    UserSelection::ImportFrom(path, password, number as usize)
                }
            }
            GuiResponse::Copy { data } => {
                debug!("copy");
                UserSelection::AddToClipboard(data)
            }
        }
    } else {
        error!("Error while creating Rust representation of a Java Instance: {:?}", res.err());
        UserSelection::GoTo(Menu::Main)
    }
}


#[derive(Serialize, Deserialize, Debug)]
pub struct JavaEntry {
    name: String,
    url: String,
    user: String,
    pass: String,
    desc: String,
}

impl JavaEntry {
    pub(crate) fn new(entry: &Entry) -> JavaEntry {
        JavaEntry {
            name: entry.name.clone(),
            url: entry.url.clone(),
            user: entry.user.clone(),
            pass: entry.pass.clone(),
            desc: entry.desc.clone(),
        }
    }

    pub(crate) fn empty() -> JavaEntry {
        JavaEntry {
            name: "".to_string(),
            url: "".to_string(),
            user: "".to_string(),
            pass: "".to_string(),
            desc: "".to_string(),
        }
    }
}

#[derive(Serialize, Deserialize, Debug)]
pub struct JavaUserOption {
    label: String,
    value: String,
    short_label: String,
}

impl JavaUserOption {
    pub(crate) fn new(user_option: &UserOption) -> JavaUserOption {
        JavaUserOption {
            label: user_option.label.clone(),
            value: user_option.value.to_string(),
            short_label: user_option.short_label.clone(),
        }
    }
}

#[derive(Deserialize, Serialize, Debug)]
pub(crate) enum JavaMenu {
    TryPass { b: bool },
    ChangePass,
    Main,
    EntriesList { filter: String },
    NewEntry,
    ShowEntry { idx: usize },
    EditEntry { idx: usize },
    DeleteEntry { idx: usize },
    Save { b: bool },
    Exit,
    ForceExit,
    TryFileRecovery,
    ImportEntries,
    ExportEntries,
    ShowConfiguration,
    WaitForDbxTokenCallback { s: String },
    SetDbxToken { s: String },
    Current,
}

impl JavaMenu {
    pub(crate) fn to_menu(self) -> Menu {
        match self {
            JavaMenu::Main => Menu::Main,
            JavaMenu::Exit => Menu::Exit,
            JavaMenu::EntriesList { filter } => Menu::EntriesList(filter),
            JavaMenu::Save { b } => Menu::Save(b),
            JavaMenu::ChangePass => Menu::ChangePass,
            JavaMenu::ExportEntries => Menu::ExportEntries,
            JavaMenu::ImportEntries => Menu::ImportEntries,
            JavaMenu::ShowConfiguration => Menu::ShowConfiguration,
            JavaMenu::ForceExit => Menu::ForceExit,
            JavaMenu::NewEntry => Menu::NewEntry,
            JavaMenu::WaitForDbxTokenCallback { s } => Menu::WaitForDbxTokenCallback(s),
            JavaMenu::ShowEntry { idx } => Menu::ShowEntry(idx),
            JavaMenu::EditEntry { idx } => Menu::EditEntry(idx),
            JavaMenu::DeleteEntry { idx } => Menu::DeleteEntry(idx),
            JavaMenu::TryPass { b } => Menu::TryPass(b),
            _ => {
                Menu::Current
            }
        }
    }
}