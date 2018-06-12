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