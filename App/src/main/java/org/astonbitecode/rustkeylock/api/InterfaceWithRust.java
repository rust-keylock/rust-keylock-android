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
package org.astonbitecode.rustkeylock.api;

import androidx.fragment.app.Fragment;
import android.util.Log;
import org.astonbitecode.rustkeylock.api.stubs.GuiResponse;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

public class InterfaceWithRust {
    private final String TAG = getClass().getName();
    public static final InterfaceWithRust INSTANCE = new InterfaceWithRust();
    private AtomicReference<CompletableFuture> callbackFuture = new AtomicReference<>(null);
    private AtomicReference<Fragment> previousFragment = new AtomicReference<>(null);

    private InterfaceWithRust() {
        Log.i(TAG, "Initializing the native interface with Rust...");
        System.loadLibrary("rustkeylockandroid");
        Log.i(TAG, "The native interface with Rust is initialized!");
    }

    public native void execute();

    private void call(Object obj) {
        callbackFuture.get().complete(obj);
    }

    public void set_password(String password, int number) {
        Map<String, Object> m = GuiResponse.ChangePassword(password, number);
        call(m);
    }

    public void go_to_menu(Map<String, Object> menu) {
        Map<String, Object> m = GuiResponse.GoToMenu(menu);
        call(m);
    }

    public void go_to_menu(String menu) {
        Map<String, Object> m = GuiResponse.GoToMenu(menu);
        call(m);
    }

    public void add_entry(JavaEntry javaEntry) {
        Map<String, Object> m = GuiResponse.AddEntry(javaEntry);
        call(m);
    }

    public void replace_entry(JavaEntry javaEntry, int index) {
        Map<String, Object> m = GuiResponse.ReplaceEntry(javaEntry, index);
        call(m);
    }

    public void delete_entry(int index) {
        Map<String, Object> m = GuiResponse.DeleteEntry(index);
        call(m);
    }

    public void generate_passphrase(JavaEntry javaEntry, int index) {
        Map<String, Object> m = GuiResponse.GeneratePassphrase(javaEntry, index);
        call(m);
    }

    public void export_import(String path, int export, String password, int number) {
        Map<String, Object> m = GuiResponse.ExportImport(path, export, password, number);
        call(m);
    }

    public void user_option_selected(String label, String value, String short_label) {
        JavaUserOption juo = new JavaUserOption(label, value, short_label);
        Map<String, Object> m = GuiResponse.UserOptionSelected(juo);
        call(m);
    }

    public void set_configuration(List<String> stringList) {
        Map<String, Object> m = GuiResponse.SetConfiguration(stringList);
        call(m);
    }

    public void copy(String data) {
        Map<String, Object> m = GuiResponse.Copy(data);
        call(m);
    }

    public void check_passwords() {
        call(GuiResponse.CheckPasswords());
    }

    public void setCallbackFuture(CompletableFuture newCallbackFuture) {
        callbackFuture.set(newCallbackFuture);
    }

    public void updateState(Fragment newFragment) {
        previousFragment.set(newFragment);
    }

    public Fragment getPreviousFragment() {
        return previousFragment.get();
    }
}