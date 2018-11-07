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

use std::sync::Mutex;
use log::{self, Level, LevelFilter, Metadata, Record};

use super::LogCallback;

static ANDROID_LOGGER: AndroidLogger = AndroidLogger {};

lazy_static! {
    static ref LOG_CB: Mutex<Option<LogCallback>> = Mutex::new(None);
}

pub struct AndroidLogger {}

impl log::Log for AndroidLogger {
    fn enabled(&self, metadata: &Metadata) -> bool {
        metadata.level() <= Level::Debug
    }

    fn log(&self, record: &Record) {
        if self.enabled(record.metadata()) {
            let message = format!("{:?}", record.args());
            (LOG_CB.lock().unwrap().unwrap())(super::to_java_string(record.level().to_string()),
                                     super::to_java_string(record.module_path().unwrap_or("").to_string()),
                                     super::to_java_string(record.file().unwrap_or("").to_string()),
                                     record.line().unwrap_or(0) as i32,
                                     super::to_java_string(message))
        }
    }

    fn flush(&self) {}
}

pub fn init(log_cb: LogCallback) {
    {
        let mut cb = LOG_CB.lock().unwrap();
        *cb = Some(log_cb);
    }
    let _ = log::set_logger(&ANDROID_LOGGER)
        .map(|()| log::set_max_level(LevelFilter::Debug));
}
