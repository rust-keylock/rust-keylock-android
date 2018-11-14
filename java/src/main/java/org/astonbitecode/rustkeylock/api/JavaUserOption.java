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

import java.io.Serializable;

public class JavaUserOption implements Serializable {
    private static final long serialVersionUID = 8738491202638465205L;

    public String label;
    public String value;
    public String short_label;

    public JavaUserOption() {

    }

    public JavaUserOption(String label, String value, String shortLabel) {
        this.label = label;
        this.value = value;
        this.short_label = shortLabel;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getShort_label() {
        return short_label;
    }

    public void setShort_label(String short_label) {
        this.short_label = short_label;
    }
}
