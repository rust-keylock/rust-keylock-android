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

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import org.astonbitecode.rustkeylock.R;
import org.astonbitecode.rustkeylock.api.JavaEntry;
import org.astonbitecode.rustkeylock.utils.Defs;

import java.util.List;

public class EntriesAdapter extends ArrayAdapter<JavaEntry> {
    private List<JavaEntry> entries;
    private Context context;
    private int layoutResourceId;

    public EntriesAdapter(Context context, int resourceId, List<JavaEntry> entries) {
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

        JavaEntry i = entries.get(position);

        if (i != null) {
            TextView tvn = (TextView) v.findViewById(R.id.entryname);

            if (tvn != null) {
                tvn.setText(i.getName());
                LinearLayout ll = (LinearLayout) v.findViewById(R.id.entrynamecontainer);
                if (i.getMeta().isLeakedpassword()) {
                    ll.setBackgroundColor(Defs.BACKROUND_ERROR);
                } else {
                    ll.setBackgroundColor(Defs.BACKROUND_NO_ERROR);
                }
            }
        }
        return v;
    }
}
