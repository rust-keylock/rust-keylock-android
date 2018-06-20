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

import org.astonbitecode.rustkeylock.MainActivity;
import org.astonbitecode.rustkeylock.R;
import org.astonbitecode.rustkeylock.api.InterfaceWithRust;
import org.astonbitecode.rustkeylock.api.JavaEntry;
import org.astonbitecode.rustkeylock.fragments.ShowEntry;

import android.util.Log;

public class ShowEntryCb implements InterfaceWithRust.EntryCallback {
    private final String TAG = getClass().getName();

    @Override
    public void apply(JavaEntry.ByReference anEntry, int entryIndex, boolean edit, boolean delete) {
        Log.d(TAG, "Callback with JavaEntry name " + anEntry.getName());
        MainActivity mainActivity = MainActivity.getActiveActivity();
        Runnable uiRunnable = new UiThreadRunnable(anEntry, entryIndex, edit, delete, mainActivity);
        mainActivity.runOnUiThread(uiRunnable);
    }

    private class UiThreadRunnable implements Runnable {
        private MainActivity mainActivity = null;
        private JavaEntry entry = null;
        private int entryIndex;
        private boolean edit = false;
        private boolean delete = false;

        public UiThreadRunnable(JavaEntry entry, int entryIndex, boolean edit, boolean delete, MainActivity mainActivity) {
            this.entry = entry;
            this.entryIndex = entryIndex;
            this.edit = edit;
            this.delete = delete;
            this.mainActivity = mainActivity;
        }

        @Override
        public void run() {
            ShowEntry se = new ShowEntry(entry, entryIndex, edit, delete);
            mainActivity.setBackButtonHandler(se);
            mainActivity.getFragmentManager().beginTransaction().replace(R.id.container, se).commitAllowingStateLoss();
        }
    }

}
