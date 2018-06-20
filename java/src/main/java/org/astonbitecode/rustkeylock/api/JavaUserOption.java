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

import static java.util.Arrays.asList;

import java.io.Serializable;
import java.util.List;

import com.sun.jna.Structure;

public class JavaUserOption extends Structure implements Serializable {

    private static final long serialVersionUID = 8738491202638465205L;

    public static class ByReference extends JavaUserOption implements Structure.ByReference {
    }

    public static class ByValue extends JavaUserOption implements Structure.ByValue {
    }

    public String label;
    public String value;
    public String shortLabel;

    public String getLabel() {
        return label;
    }

    public String getValue() {
        return value;
    }

    public String getShortLabel() {
        return shortLabel;
    }

    @Override
    protected List<String> getFieldOrder() {
        return asList("label", "value", "shortLabel");
    }
}
