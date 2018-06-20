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
import org.astonbitecode.rustkeylock.api.InterfaceWithRust.StringListCallback;
import org.astonbitecode.rustkeylock.api.StringList.ByReference;
import org.astonbitecode.rustkeylock.fragments.EditConfiguration;

import android.util.Log;

public class EditConfigurationCb implements StringListCallback {
    private final String TAG = getClass().getName();

    @Override
    public void apply(ByReference stringList) {
        Log.d(TAG, "Callback for editing configuration");
        MainActivity mainActivity = MainActivity.getActiveActivity();
        Runnable uiRunnable = new UiThreadRunnable(stringList.getStrings(), mainActivity);
        mainActivity.runOnUiThread(uiRunnable);
    }

    private class UiThreadRunnable implements Runnable {
        private List<String> strings = null;
        private MainActivity mainActivity = null;

        public UiThreadRunnable(List<String> strings, MainActivity mainActivity) {
            this.strings = strings;
            this.mainActivity = mainActivity;
        }

        @Override
        public void run() {
            EditConfiguration ec = new EditConfiguration(strings);
            mainActivity.setBackButtonHandler(ec);
            mainActivity.getFragmentManager().beginTransaction().replace(R.id.container, ec).commitAllowingStateLoss();
        }
    }
}
