package org.astonbitecode.rustkeylock.callbacks;

import org.astonbitecode.rustkeylock.api.InterfaceWithRust;

import android.util.Log;

public class LogCb implements InterfaceWithRust.LoggingCallback {
	private final String INFO = "INFO";
	private final String WARN = "WARN";
	private final String ERROR = "ERROR";

	@Override
	public void apply(String level, String path, String file, int line, String message) {
		String TAG = "astonbitecode NATIVE: " + path + "-" + file + "(line " + line + ")";

		if (level.equals(INFO)) {
			Log.i(TAG, message);
		} else if (level.equals(WARN)) {
			Log.w(TAG, message);
		} else if (level.equals(ERROR)) {
			Log.e(TAG, message);
		} else {
			// Everything else is assumed to be DEBUG
			Log.d(TAG, message);
		}
	}
}