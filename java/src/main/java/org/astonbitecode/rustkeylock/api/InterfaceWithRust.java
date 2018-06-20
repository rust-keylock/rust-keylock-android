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

import org.astonbitecode.rustkeylock.callbacks.ShowMessageCb;

import com.sun.jna.Callback;
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;

public interface InterfaceWithRust extends Library {
    String JNA_LIBRARY_NAME = "rustkeylockandroid";
    NativeLibrary JNA_NATIVE_LIB = NativeLibrary.getInstance(JNA_LIBRARY_NAME);

    InterfaceWithRust INSTANCE = (InterfaceWithRust) Native.loadLibrary(JNA_LIBRARY_NAME, InterfaceWithRust.class);

    /**
     * Simple callback with String as argument
     */
    interface RustCallback extends Callback {
        void apply(String aString);
    }

    /**
     * Callback with a List of Strings
     */
    interface StringListCallback extends Callback {

        void apply(StringList.ByReference stringList);
    }

    /**
     * Callback containing an Entry
     */
    interface EntryCallback extends Callback {
        // TODO: Use an enum in the arguments instead
        void apply(JavaEntry.ByReference anEntry, int entryIndex, boolean edit, boolean delete);
    }

    /**
     * Callback containing a Set of Entries
     */
    interface EntriesSetCallback extends Callback {
        void apply(JavaEntriesSet.ByReference entriesSet, String filter);
    }

    /**
     * Callback for showing messages
     */
    interface ShowMessageCallback extends Callback {
        void apply(JavaUserOptionsSet.ByReference options, String message, String severity);
    }

    /**
     * Callback to be used for logging
     */
    interface LoggingCallback extends Callback {
        void apply(String level, String path, String file, int line, String message);
    }

    void execute(RustCallback showMenuCb, EntryCallback showEntryCb, EntriesSetCallback showEntriesSetCb,
                 ShowMessageCb sowMessageCb, StringListCallback editConfigurationCb, LoggingCallback logCb, String certFilePath);

    /**
     * Passes the Username and Password to Rust
     *
     * @param password
     * @param number
     */
    void set_password(String password, int number);

    /**
     * Passes a selected Entry to Rust
     *
     * @param anEntry
     */
    void entry_selected(JavaEntry anEntry);

    /**
     * Passes a Menu name to Rust. Rust instructs the callback to go there
     *
     * @param menuName
     */
    void go_to_menu(String menuName);

    /**
     * Passes a Menu name to Rust plus an int argument and a String argument. Rust
     * instructs the callback to go to this menu and use the passed arguments. An
     * argNum or argStr that is a String null means that the argument is not used.
     *
     * @param menuName
     * @param argNum   A String representing an Integer
     * @param argStr
     */
    void go_to_menu_plus_arg(String menuName, String argNum, String argStr);

    /**
     * Adds this JavaEntry to the list of Entries in memory. Note: The entry is not
     * yet encrypted and saved in the file.
     *
     * @param javaEntry
     */
    void add_entry(JavaEntry javaEntry);

    /**
     * Replaces the Entry located at the provided index.
     *
     * @param javaEntry
     * @param index
     */
    void replace_entry(JavaEntry javaEntry, int index);

    /**
     * Deletes the Entry located at the provided index.
     *
     * @param index
     */
    void delete_entry(int index);

    /**
     * Provides to Rust a path to export to / import from
     *
     * @param path
     * @param export   Bypass issues with boolean. 0 for false, > 0 for true
     * @param password
     * @param number
     */
    void export_import(String path, int export, String password, int number);

    /**
     * Provides to Rust a UserOption that was selected
     *
     * @param label
     * @param value
     * @param short_label
     */
    void user_option_selected(String label, String value, String short_label);

    /**
     * Instructs Rust to deallocate the heap memory for this JavaEntry
     *
     * @param javaEntry
     */
    void drop_java_entry(JavaEntry javaEntry);

    /**
     * Instructs Rust to deallocate the heap memory for this JavaEntriesSet
     *
     * @param javaEntriesSet
     */
    void drop_java_entries_set(JavaEntriesSet javaEntriesSet);

    /**
     * Sets the configuration values
     *
     * @param stringList A list that contains elements in the order: - Nextcloud server URL
     *                   - Nextcloud user - Nextcloud password - Nextcloud DER certificate
     *                   file location
     */
    void set_configuration(StringList stringList);

    /**
     * Copies the data in the Clipboard
     * @param data
     */
    void copy(String data);
}