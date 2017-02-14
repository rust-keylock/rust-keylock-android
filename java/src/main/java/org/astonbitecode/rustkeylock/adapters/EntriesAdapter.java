package org.astonbitecode.rustkeylock.adapters;

import java.util.List;

import org.astonbitecode.rustkeylock.R;
import org.astonbitecode.rustkeylock.api.JavaEntry;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

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
			LayoutInflater inflater = ((Activity)context).getLayoutInflater();
			v = inflater.inflate(layoutResourceId, parent, false);
		}

		JavaEntry i = entries.get(position);

		if (i != null) {
			TextView tvn = (TextView) v.findViewById(R.id.entryname);

			if (tvn != null) {
				tvn.setText(i.getName());
			}
		}
		return v;
	}
}
