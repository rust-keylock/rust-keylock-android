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
import org.astonbitecode.rustkeylock.fragments.*;
import org.astonbitecode.rustkeylock.utils.Defs;

public class ShowMenuCb extends NativeCallbackToRustChannelSupport {
    private final String TAG = getClass().getName();

    public void apply(String menu) {
        Log.d(TAG, "Callback for showing menu " + menu);
        InterfaceWithRust.INSTANCE.setCallback(this);
        MainActivity mainActivity = MainActivity.getActiveActivity();
        Runnable uiRunnable = new UiThreadRunnable(menu, MainActivity.getActiveActivity());
        mainActivity.runOnUiThread(uiRunnable);
    }

    private class UiThreadRunnable implements Runnable {
        private String menu = null;
        private MainActivity mainActivity = null;

        public UiThreadRunnable(String menu, MainActivity mainActivity) {
            this.menu = menu;
            this.mainActivity = mainActivity;
        }

        @Override
        public void run() {
            if (menu.equals(Defs.MENU_TRY_PASS)) {
                EnterPassword ep = new EnterPassword();
                mainActivity.setBackButtonHandler(ep);
                mainActivity.getFragmentManager().beginTransaction().replace(R.id.container, ep)
                        .commitAllowingStateLoss();
            } else if (menu.equals(Defs.MENU_CHANGE_PASS)) {
                ChangePassword cp = new ChangePassword();
                mainActivity.setBackButtonHandler(cp);
                mainActivity.getFragmentManager().beginTransaction().replace(R.id.container, cp)
                        .commitAllowingStateLoss();
            } else if (menu.equals(Defs.MENU_MAIN)) {
                MainMenu mm = new MainMenu();
                mainActivity.setBackButtonHandler(mm);
                mainActivity.getFragmentManager().beginTransaction().replace(R.id.container, mm)
                        .commitAllowingStateLoss();
            } else if (menu.equals(Defs.MENU_EXIT)) {
                ExitMenu em = new ExitMenu();
                mainActivity.setBackButtonHandler(em);
                mainActivity.getFragmentManager().beginTransaction().replace(R.id.container, em)
                        .commitAllowingStateLoss();
            } else if (menu.equals(Defs.MENU_EXPORT_ENTRIES)) {
                SelectPath sp = new SelectPath(true);
                mainActivity.setBackButtonHandler(sp);
                mainActivity.getFragmentManager().beginTransaction().replace(R.id.container, sp)
                        .commitAllowingStateLoss();
            } else if (menu.equals(Defs.MENU_IMPORT_ENTRIES)) {
                SelectPath sp = new SelectPath(false);
                mainActivity.setBackButtonHandler(sp);
                mainActivity.getFragmentManager().beginTransaction().replace(R.id.container, sp)
                        .commitAllowingStateLoss();
            } else {
                throw new RuntimeException("Cannot Show Menu with name '" + menu + "' and no arguments");
            }
        }
    }

}
