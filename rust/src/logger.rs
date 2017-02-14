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
	let android_logger = AndroidLogger {log_cb: log_cb};

	let _ = log::set_logger(|max_log_level| {
        max_log_level.set(LogLevelFilter::Debug);
        Box::new(android_logger)
    });
}