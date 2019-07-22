// Copyright 2019 astonbitecode
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
package org.astonbitecode.rustkeylock.api.stubs;

import org.astonbitecode.rustkeylock.utils.Defs;

import java.util.HashMap;
import java.util.Map;

public class JavaMenu {

    public static Map<String, Object> EntriesList(String filter) {
        Map<String, Object> inner = new HashMap<>();
        inner.put("filter", filter);
        Map<String, Object> map = new HashMap<>();
        map.put(Defs.MENU_ENTRIES_LIST, inner);
        return map;
    }

    public static String Exit() {
        return Defs.MENU_EXIT;
    }

    public static String ForceExit() {
        return Defs.MENU_FORCE_EXIT;
    }

    public static String ChangePass() {
        return Defs.MENU_CHANGE_PASS;
    }

    public static Map<String, Object> Save(Boolean b) {
        Map<String, Object> inner = new HashMap<>();
        inner.put("b", b);
        Map<String, Object> map = new HashMap<>();
        map.put(Defs.MENU_SAVE, inner);
        return map;
    }

    public static Map<String, Object> TryPass(Boolean b) {
        Map<String, Object> inner = new HashMap<>();
        inner.put("b", b);
        Map<String, Object> map = new HashMap<>();
        map.put(Defs.MENU_TRY_PASS, inner);
        return map;
    }

    public static String ExportEntries() {
        return Defs.MENU_EXPORT_ENTRIES;
    }

    public static String ImportEntries() {
        return Defs.MENU_IMPORT_ENTRIES;
    }

    public static String ShowConfiguration() {
        return Defs.MENU_SHOW_CONFIGURATION;
    }

    public static String Main() {
        return Defs.MENU_MAIN;
    }

    public static String NewEntry() {
        return Defs.MENU_NEW_ENTRY;
    }

    public static Map<String, Object> ShowEntry(Integer idx) {
        Map<String, Object> inner = new HashMap<>();
        inner.put("idx", idx);
        Map<String, Object> map = new HashMap<>();
        map.put(Defs.MENU_SHOW_ENTRY, inner);
        return map;
    }

    public static Map<String, Object> EditEntry(Integer idx) {
        Map<String, Object> inner = new HashMap<>();
        inner.put("idx", idx);
        Map<String, Object> map = new HashMap<>();
        map.put(Defs.MENU_EDIT_ENTRY, inner);
        return map;
    }

    public static Map<String, Object> DeleteEntry(Integer idx) {
        Map<String, Object> inner = new HashMap<>();
        inner.put("idx", idx);
        Map<String, Object> map = new HashMap<>();
        map.put(Defs.MENU_DELETE_ENTRY, inner);
        return map;
    }

    public static Map<String, Object> SetDbToken(String token) {
        Map<String, Object> inner = new HashMap<>();
        inner.put("token", token);
        Map<String, Object> map = new HashMap<>();
        map.put(Defs.MENU_SET_DB_TOKEN, inner);
        return map;
    }
}
