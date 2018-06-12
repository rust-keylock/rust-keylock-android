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

import java.util.Arrays;
import java.util.List;

import com.sun.jna.Structure;

/**
 * A struct that contains an array of structs. This is the Java representation
 * of the JavaEntriesSet struct in Rust
 */
public class JavaEntriesSet extends Structure {

    public static class ByReference extends JavaEntriesSet implements Structure.ByReference {
    }

    public static class ByValue extends JavaEntriesSet implements Structure.ByValue {
    }

    /**
     * An array of JavaEntriesSet returned from Rust.
     * <p>
     * Actually, this is a pointer to a bunch of struct instances that are next
     * to each other in memory. We cast it to an array in {@link #getEntries()}.
     * <p>
     * NB: We need to explicitly specify that the field is a pointer (i.e. we
     * need to use ByReference) because, by default, JNA assumes that struct
     * fields are not pointers (i.e. the if we just say "JavaEntry", JNA assumes
     * "JavaEntry.ByValue" here).
     */
    public JavaEntry.ByReference entries;

    /**
     * The size of the array from Rust.
     * <p>
     * Because we don't have any way to tell how long the array is, we've got to
     * return the length back separately. The reason we don't have to do this
     * with strings passed back from Rust (which are actually arrays of
     * characters) is that native strings have a special null character at the
     * end that JNA uses to tell how long each string is.
     */
    public int numberOfEntries;

    /**
     * Get the JavaEntry list this struct's pointer is pointing to.
     * <p>
     * Here we cast the native array into a Java list to make it more convenient
     * to work with in Java.
     */
    public List<JavaEntry> getEntries() {
        JavaEntry[] array = (JavaEntry[]) entries.toArray(numberOfEntries);
        return Arrays.asList(array);
    }

    /**
     * Specify the order of the struct's fields.
     * <p>
     * The order here needs to match the order of the fields in the Rust code.
     * The astute will notice that the field names only match the field names in
     * the Java class, but not the equivalent Rust struct (the Rust one's are in
     * snake_case, but could equally have had completely different names). This
     * is because the fields are mapped from the Rust representation to the Java
     * one by their order (i.e. their relative location in memory), not by their
     * names.
     */
    @Override
    protected List<String> getFieldOrder() {
        return Arrays.asList("entries", "numberOfEntries");
    }
}