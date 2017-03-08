package org.astonbitecode.rustkeylock.fragments;

import org.astonbitecode.rustkeylock.R;
import org.astonbitecode.rustkeylock.api.InterfaceWithRust;
import org.astonbitecode.rustkeylock.api.JavaEntry;
import org.astonbitecode.rustkeylock.handlers.back.BackButtonHandler;
import org.astonbitecode.rustkeylock.utils.Defs;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class ShowEntry extends Fragment implements OnClickListener, BackButtonHandler {
	private static final long serialVersionUID = 163106573370997557L;
	private final String TAG = getClass().getName();
	private transient JavaEntry entry;
	private int entryIndex;
	private boolean edit;
	private boolean delete;
	private transient EditText nameText;
	private transient EditText userText;
	private transient EditText passwordText;
	private transient EditText descriptionText;

	/**
	 * Creates a ShowEntry fragment.
	 * 
	 * @param entry
	 *            The entry to present in this Fragment
	 * @param entryIndex
	 *            The index of the Entry in the overall Entries Vec in Rust. -1
	 *            denotes a new Entry
	 * @param edit
	 * @param delete
	 */
	public ShowEntry(JavaEntry entry, int entryIndex, boolean edit, boolean delete) {
		this.entry = entry;
		this.entryIndex = entryIndex;
		this.edit = edit;
		this.delete = delete;
	}

	public ShowEntry() {
		this.entry = null;
		this.entryIndex = 0;
		this.edit = false;
		this.delete = false;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		restore(savedInstanceState);
		View rootView = inflater.inflate(R.layout.fragment_show_entry, container, false);
		if (this.entry != null) {
			prepareUiElements(rootView);
		}
		return rootView;
	}

	@Override
	public void onClick(View view) {
		if (view.getId() == R.id.editButton) {
			Log.d(TAG, "Clicked edit on entry with id " + entryIndex);

			InterfaceWithRust.INSTANCE.go_to_menu_plus_arg(Defs.MENU_EDIT_ENTRY, entryIndex);
		} else if (view.getId() == R.id.updateButton) {
			Log.d(TAG, "Clicked Update for entry with id " + entryIndex);

			JavaEntry javaEntry = new JavaEntry();
			javaEntry.name = nameText.getText() != null ? nameText.getText().toString() : "";
			javaEntry.user = userText.getText() != null ? userText.getText().toString() : "";
			javaEntry.pass = passwordText.getText() != null ? passwordText.getText().toString() : "";
			javaEntry.desc = descriptionText.getText() != null ? descriptionText.getText().toString() : "";

			boolean errorsOccured = false;
			if (javaEntry.name.isEmpty()) {
				nameText.setError("Required field");
				errorsOccured = true;
			}
			if (javaEntry.user.isEmpty()) {
				userText.setError("Required field");
				errorsOccured = true;
			}
			if (javaEntry.pass.isEmpty()) {
				passwordText.setError("Required field");
				errorsOccured = true;
			}
			if (!errorsOccured) {
				if (entryIndex >= 0) {
					InterfaceWithRust.INSTANCE.replace_entry(javaEntry, entryIndex);
				} else {
					InterfaceWithRust.INSTANCE.add_entry(javaEntry);
				}
			}
		} else if (view.getId() == R.id.deleteButton) {
			Log.d(TAG, "Clicked delete on entry with id " + entryIndex);

			InterfaceWithRust.INSTANCE.go_to_menu_plus_arg(Defs.MENU_DELETE_ENTRY, entryIndex);
		} else if (view.getId() == R.id.areYouSureButton) {
			Log.d(TAG, "Clicked confirm deletion on entry with id " + entryIndex);

			InterfaceWithRust.INSTANCE.delete_entry(entryIndex);
		}
	}

	private void prepareUiElements(View v) {
		Button eb = (Button) v.findViewById(R.id.editButton);
		eb.setOnClickListener(this);
		eb.setVisibility((edit || delete) ? View.GONE : View.VISIBLE);
		Button db = (Button) v.findViewById(R.id.deleteButton);
		db.setOnClickListener(this);
		db.setVisibility((edit || delete) ? View.GONE : View.VISIBLE);

		TextView title = (TextView) v.findViewById(R.id.showEntryLabel);
		if (delete) {
			title.setText("Deleting Password! Are you sure?");
		}

		EditText nameText = (EditText) v.findViewById(R.id.editName);
		nameText.setText(entry.getName());
		nameText.setEnabled(edit);
		this.nameText = nameText;
		EditText userText = (EditText) v.findViewById(R.id.editUser);
		userText.setText(entry.getUser());
		userText.setEnabled(edit);
		this.userText = userText;
		EditText passwordText = (EditText) v.findViewById(R.id.editPassword);
		passwordText.setText(entry.getPass());
		passwordText.setEnabled(edit);
		this.passwordText = passwordText;
		EditText descriptionText = (EditText) v.findViewById(R.id.editDescription);
		descriptionText.setText(entry.getDesc());
		descriptionText.setEnabled(edit);
		this.descriptionText = descriptionText;

		Button ub = (Button) v.findViewById(R.id.updateButton);
		ub.setOnClickListener(this);
		ub.setVisibility((edit && !delete) ? View.VISIBLE : View.GONE);
		Button aysb = (Button) v.findViewById(R.id.areYouSureButton);
		aysb.setOnClickListener(this);
		aysb.setVisibility((!edit && delete) ? View.VISIBLE : View.GONE);
	}

	@Override
	public void onBackButton() {
		Log.d(TAG, "Back button pressed");
		InterfaceWithRust.INSTANCE.go_to_menu(Defs.MENU_ENTRIES_LIST);
	}

	@Override
	public void onSaveInstanceState(Bundle state) {
		state.putInt("entryIndex", entryIndex);
		state.putBoolean("edit", edit);
		state.putBoolean("delete", delete);
	}

	private void restore(Bundle state) {
		if (state != null) {
			int entryIndex = state.getInt("entryIndex");
			boolean edit = state.getBoolean("edit");
			boolean delete = state.getBoolean("delete");

			if (edit && !delete) {
				InterfaceWithRust.INSTANCE.go_to_menu_plus_arg(Defs.MENU_EDIT_ENTRY, entryIndex);
			} else if (!edit && delete) {
				InterfaceWithRust.INSTANCE.go_to_menu_plus_arg(Defs.MENU_DELETE_ENTRY, entryIndex);
			} else {
				InterfaceWithRust.INSTANCE.go_to_menu_plus_arg(Defs.MENU_SHOW_ENTRY, entryIndex);
			}
		}
	}
}
