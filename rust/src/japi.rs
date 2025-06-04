use std::future::Future;

use j4rs::errors::J4RsError;
use j4rs::{Instance, Jvm};
use log::*;
use rust_keylock::dropbox::DropboxConfiguration;
use rust_keylock::nextcloud::NextcloudConfiguration;
use rust_keylock::{
    AllConfigurations, Entry, EntryMeta, GeneralConfiguration, Menu, UserOption, UserSelection,
};
use serde::{Deserialize, Serialize};
use zeroize::Zeroize;

pub async fn handle_instance_receiver_result(
    instance_res_future: impl Future<Output = Result<Instance, J4RsError>>,
) -> crate::errors::Result<UserSelection> {
    let received_instance_res = instance_res_future.await;
    instance_to_gui_response(received_instance_res?)
}

fn instance_to_gui_response(instance: Instance) -> crate::errors::Result<UserSelection> {
    let jvm = Jvm::attach_thread()?;
    let gui_response = jvm.to_rust(instance)?;

    let user_selection = match gui_response {
        GuiResponse::ProvidedPassword { password, number } => {
            UserSelection::new_provided_password(password, number)
        }
        GuiResponse::GoToMenu { menu } => {
            debug!("go_to_menu");
            UserSelection::GoTo(menu.to_menu())
        }
        GuiResponse::AddEntry { entry } => {
            debug!("add_entry");
            let entry = Entry::new(
                entry.name.to_owned(),
                entry.url.to_owned(),
                entry.user.to_owned(),
                entry.pass.to_owned(),
                entry.desc.to_owned(),
                EntryMeta::new(entry.meta.leakedpassword),
            );

            UserSelection::NewEntry(entry)
        }
        GuiResponse::ReplaceEntry { entry, index } => {
            debug!("replace_entry");
            let entry = Entry::new(
                entry.name.to_owned(),
                entry.url.to_owned(),
                entry.user.to_owned(),
                entry.pass.to_owned(),
                entry.desc.to_owned(),
                EntryMeta::new(entry.meta.leakedpassword),
            );

            UserSelection::ReplaceEntry(index as usize, entry)
        }
        GuiResponse::DeleteEntry { index } => {
            debug!("delete_entry");
            UserSelection::DeleteEntry(index)
        }
        GuiResponse::GeneratePassphrase { entry, index } => {
            debug!("generate_passphrase");
            let entry = Entry::new(
                entry.name.to_owned(),
                entry.url.to_owned(),
                entry.user.to_owned(),
                entry.pass.to_owned(),
                entry.desc.to_owned(),
                EntryMeta::new(entry.meta.leakedpassword),
            );
            let index_opt = if index < 0 {
                None
            } else {
                Some(index as usize)
            };
            UserSelection::GeneratePassphrase(index_opt, entry)
        }
        GuiResponse::SetConfiguration { strings } => {
            debug!("set_configuration with {} elements", strings.len());

            let ncc = if strings.len() == 6 {
                let b = match strings[3].as_ref() {
                    "true" => true,
                    _ => false,
                };
                NextcloudConfiguration::new(
                    strings[0].clone(),
                    strings[1].clone(),
                    strings[2].clone(),
                    b,
                )
            } else {
                NextcloudConfiguration::new(
                    "Wrong Java Data".to_string().to_string(),
                    "Wrong Java Data".to_string(),
                    "Wrong Java Data".to_string(),
                    false,
                )
            };
            let dbxc = if strings.len() == 6 && strings[4] != "" {
                DropboxConfiguration::new(strings[4].clone())
            } else {
                Ok(DropboxConfiguration::default())
            };
            let genc = if strings.len() == 6 {
                GeneralConfiguration::new(Some(strings[5].clone()))
            } else {
                GeneralConfiguration::default()
            };

            UserSelection::UpdateConfiguration(AllConfigurations::new(
                ncc.unwrap(),
                dbxc.unwrap(),
                genc,
            ))
        }
        GuiResponse::UserOptionSelected { user_option } => {
            debug!("user_option_selected");

            UserSelection::UserOption(UserOption::from((
                user_option.label,
                user_option.value,
                user_option.short_label,
            )))
        }
        GuiResponse::ExportImport {
            path,
            mode,
            password,
            number,
        } => {
            debug!("export_import");

            if mode > 0 {
                debug!("Followed exporting path");
                UserSelection::ExportTo(path)
            } else {
                debug!("Followed importing path");
                UserSelection::new_import_from(path, password, number as usize)
            }
        }
        GuiResponse::Copy { data } => {
            debug!("copy");
            UserSelection::AddToClipboard(data)
        }
        GuiResponse::CheckPasswords => {
            debug!("check passwords");
            UserSelection::CheckPasswords
        }
    };

    Ok(user_selection)
}

#[derive(Deserialize, Debug)]
enum GuiResponse {
    ProvidedPassword {
        password: String,
        number: usize,
    },
    GoToMenu {
        menu: JavaMenu,
    },
    AddEntry {
        entry: JavaEntry,
    },
    ReplaceEntry {
        entry: JavaEntry,
        index: usize,
    },
    DeleteEntry {
        index: usize,
    },
    SetConfiguration {
        strings: Vec<String>,
    },
    UserOptionSelected {
        user_option: JavaUserOption,
    },
    ExportImport {
        path: String,
        mode: usize,
        password: String,
        number: isize,
    },
    Copy {
        data: String,
    },
    GeneratePassphrase {
        entry: JavaEntry,
        index: isize,
    },
    CheckPasswords,
}

#[derive(Serialize, Deserialize, Debug, Zeroize)]
#[zeroize(drop)]
pub(crate) struct JavaEntryMeta {
    pub leakedpassword: bool,
}

impl JavaEntryMeta {
    fn new(entry_meta: &EntryMeta) -> JavaEntryMeta {
        JavaEntryMeta {
            leakedpassword: entry_meta.leaked_password,
        }
    }
}

#[derive(Serialize, Deserialize, Debug, Zeroize)]
#[zeroize(drop)]
pub struct JavaEntry {
    name: String,
    url: String,
    user: String,
    pass: String,
    desc: String,
    meta: JavaEntryMeta,
}

impl JavaEntry {
    pub(crate) fn new(entry: &Entry) -> JavaEntry {
        JavaEntry {
            name: entry.name.clone(),
            url: entry.url.clone(),
            user: entry.user.clone(),
            pass: entry.pass.clone(),
            desc: entry.desc.clone(),
            meta: JavaEntryMeta::new(&entry.meta),
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
    SetDbxToken { token: String },
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
            JavaMenu::NewEntry => Menu::NewEntry(None),
            JavaMenu::WaitForDbxTokenCallback { s } => Menu::WaitForDbxTokenCallback(s),
            JavaMenu::ShowEntry { idx } => Menu::ShowEntry(idx),
            JavaMenu::EditEntry { idx } => Menu::EditEntry(idx),
            JavaMenu::DeleteEntry { idx } => Menu::DeleteEntry(idx),
            JavaMenu::TryPass { b } => Menu::TryPass(b),
            _ => Menu::Current,
        }
    }
}
