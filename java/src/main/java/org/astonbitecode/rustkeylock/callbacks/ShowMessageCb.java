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

import java.util.List;

import org.astonbitecode.rustkeylock.MainActivity;
import org.astonbitecode.rustkeylock.R;
import org.astonbitecode.rustkeylock.api.InterfaceWithRust;
import org.astonbitecode.rustkeylock.api.JavaUserOption;
import org.astonbitecode.rustkeylock.api.JavaUserOptionsSet;
import org.astonbitecode.rustkeylock.fragments.ShowMessage;

import android.util.Log;

public class ShowMessageCb implements InterfaceWithRust.ShowMessageCallback {
    private final String TAG = getClass().getName();

    @Override
    public void apply(JavaUserOptionsSet.ByReference options, String message, String severity) {
        Log.d(TAG, "Callback for showing message " + message + " of severity " + severity);

        List<JavaUserOption> optionsList = options.getOptions();

        MainActivity mainActivity = MainActivity.getActiveActivity();
        Runnable uiRunnable = new UiThreadRunnable(optionsList, message, severity, mainActivity);
        mainActivity.runOnUiThread(uiRunnable);
    }

    private class UiThreadRunnable implements Runnable {
        private MainActivity mainActivity = null;
        private List<JavaUserOption> optionsList = null;
        private String message = null;
        private String severity = null;

        public UiThreadRunnable(List<JavaUserOption> optionsList, String message, String severity,
                                MainActivity mainActivity) {
            this.optionsList = optionsList;
            this.message = message;
            this.severity = severity;
            this.mainActivity = mainActivity;
        }

        @Override
        public void run() {
            ShowMessage sm = new ShowMessage(severity, message, optionsList);
            mainActivity.setBackButtonHandler(sm);
            mainActivity.getFragmentManager().beginTransaction().replace(R.id.container, sm).commitAllowingStateLoss();
        }
    }

}
