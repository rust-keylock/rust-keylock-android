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

use log::{self, LogRecord, LogLevel, LogMetadata, LogLevelFilter};
use super::LogCallback;

pub struct AndroidLogger {
    log_cb: LogCallback,
}

impl log::Log for AndroidLogger {
    fn enabled(&self, metadata: &LogMetadata) -> bool {
        metadata.level() <= LogLevel::Debug
    }

    fn log(&self, record: &LogRecord) {
        if self.enabled(record.metadata()) {
            let message = format!("{:?}", record.args());
            (self.log_cb)(super::to_java_string(record.level().to_string()),
                          super::to_java_string(record.location().module_path().to_string()),
                          super::to_java_string(record.location().file().to_string()),
                          record.location().line() as i32,
                          super::to_java_string(message))
        }
    }
}

pub fn init(log_cb: LogCallback) {
    let android_logger = AndroidLogger { log_cb: log_cb };

    let _ = log::set_logger(|max_log_level| {
        max_log_level.set(LogLevelFilter::Debug);
        Box::new(android_logger)
    });
}
