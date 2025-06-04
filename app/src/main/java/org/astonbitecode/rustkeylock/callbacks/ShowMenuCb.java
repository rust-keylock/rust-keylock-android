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

import android.app.Fragment;
import android.util.Log;
import org.astonbitecode.rustkeylock.MainActivity;
import org.astonbitecode.rustkeylock.R;
import org.astonbitecode.rustkeylock.api.InterfaceWithRust;
import org.astonbitecode.rustkeylock.fragments.*;
import org.astonbitecode.rustkeylock.handlers.back.BackButtonHandler;
import org.astonbitecode.rustkeylock.utils.Defs;
import java.util.concurrent.CompletableFuture;

public class ShowMenuCb {
    private final String TAG = getClass().getName();

    public CompletableFuture<Object> apply(String menu) {
        Log.d(TAG, "Callback for showing menu " + menu);
        CompletableFuture<Object> f = new CompletableFuture<>();
        InterfaceWithRust.INSTANCE.setCallbackFuture(f);
        MainActivity mainActivity = MainActivity.getActiveActivity();
        Runnable uiRunnable = new UiThreadRunnable(menu, MainActivity.getActiveActivity());
        mainActivity.runOnUiThread(uiRunnable);
        return f;
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
            Fragment fragment = null;
            if (menu.equals(Defs.MENU_TRY_PASS)) {
                fragment = new EnterPassword();
            } else if (menu.equals(Defs.MENU_CHANGE_PASS)) {
                fragment = new ChangePassword();
            } else if (menu.equals(Defs.MENU_MAIN)) {
                fragment = new MainMenu();
            } else if (menu.equals(Defs.MENU_EXIT)) {
                fragment = new ExitMenu();
            } else if (menu.equals(Defs.MENU_EXPORT_ENTRIES)) {
                fragment = new SelectPath(true);
            } else if (menu.equals(Defs.MENU_IMPORT_ENTRIES)) {
                fragment = new SelectPath(false);
            } else if (menu.equals(Defs.MENU_CURRENT)) {
                fragment = InterfaceWithRust.INSTANCE.getPreviousFragment();
            } else {
                throw new RuntimeException("Cannot Show Menu with name '" + menu + "' and no arguments");
            }

            if (fragment instanceof BackButtonHandler) {
                mainActivity.setBackButtonHandler((BackButtonHandler) fragment);
            }
            mainActivity.getFragmentManager().beginTransaction().replace(R.id.container, fragment)
                    .commitAllowingStateLoss();

            InterfaceWithRust.INSTANCE.updateState(fragment);
        }
    }

}
