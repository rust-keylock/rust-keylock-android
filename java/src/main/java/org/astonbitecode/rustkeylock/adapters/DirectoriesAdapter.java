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
package org.astonbitecode.rustkeylock.adapters;

import java.util.List;

import org.astonbitecode.rustkeylock.R;
import org.astonbitecode.rustkeylock.fragments.DirectorySelector;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class DirectoriesAdapter extends ArrayAdapter<DirectorySelector.DirectoryEntry> {
    private List<DirectorySelector.DirectoryEntry> entries;
    private Context context;
    private int layoutResourceId;

    public DirectoriesAdapter(Context context, int resourceId, List<DirectorySelector.DirectoryEntry> entries) {
        super(context, resourceId, entries);
        this.entries = entries;
        this.context = context;
        this.layoutResourceId = resourceId;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;

        if (v == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            v = inflater.inflate(layoutResourceId, parent, false);
        }

        DirectorySelector.DirectoryEntry i = entries.get(position);

        if (i != null) {
            TextView tvn = (TextView) v.findViewById(R.id.directoryname);

            if (tvn != null) {
                tvn.setText(i.getName());
            }
        }
        return v;
    }
}
