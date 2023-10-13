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

import android.util.Log;
import org.astonbitecode.j4rs.api.invocation.NativeCallbackToRustChannelSupport;
import org.astonbitecode.rustkeylock.MainActivity;
import org.astonbitecode.rustkeylock.R;
import org.astonbitecode.rustkeylock.api.InterfaceWithRust;
import org.astonbitecode.rustkeylock.api.JavaEntry;
import org.astonbitecode.rustkeylock.fragments.ListEntries;
import org.astonbitecode.rustkeylock.utils.Defs;

import java.util.List;

public class ShowEntriesSetCb extends NativeCallbackToRustChannelSupport {
    private final String TAG = getClass().getName();

    public void apply(List<JavaEntry> entries, String filter) {
        Log.d(TAG, "ShowEntriesSetCb with filter " + filter);
        InterfaceWithRust.INSTANCE.setCallback(this);

        MainActivity mainActivity = MainActivity.getActiveActivity();
        Runnable uiRunnable = new UiThreadRunnable(entries, filter, mainActivity);
        mainActivity.runOnUiThread(uiRunnable);
    }

    private class UiThreadRunnable implements Runnable {
        private List<JavaEntry> entries = null;
        private MainActivity mainActivity = null;
        private String filter = null;

        public UiThreadRunnable(List<JavaEntry> entries, String filter, MainActivity mainActivity) {
            this.entries = entries;
            this.mainActivity = mainActivity;
            this.filter = filter.equals(Defs.EMPTY_ARG) ? "" : filter;
        }

        @Override
        public void run() {
            ListEntries le = new ListEntries(entries, filter);
            mainActivity.setBackButtonHandler(le);
            mainActivity.getFragmentManager().beginTransaction().replace(R.id.container, le).commitAllowingStateLoss();
            InterfaceWithRust.INSTANCE.updateState(le);
        }
    }

}