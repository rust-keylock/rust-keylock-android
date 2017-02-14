package org.astonbitecode.rustkeylock.fragments;

import java.util.ArrayList;
import java.util.List;

import org.astonbitecode.rustkeylock.R;
import org.astonbitecode.rustkeylock.adapters.EntriesAdapter;
import org.astonbitecode.rustkeylock.api.InterfaceWithRust;
import org.astonbitecode.rustkeylock.api.JavaEntry;
import org.astonbitecode.rustkeylock.handlers.back.BackButtonHandler;
import org.astonbitecode.rustkeylock.handlers.state.SaveStateHandler;
import org.astonbitecode.rustkeylock.utils.Defs;

import android.app.ListFragment;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;

public class ListEntries extends ListFragment implements OnClickListener, BackButtonHandler, SaveStateHandler {
	private static final long serialVersionUID = 8765819759487480794L;
	private final String TAG = getClass().getName();
	private List<JavaEntry> entries;
	private EntriesAdapter entriesAdapter;

	public ListEntries() {
		this.entries = new ArrayList<>();
	}

	public ListEntries(List<JavaEntry> entries) {
		this.entries = entries;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_list_entries, container, false);
		Button nb = (Button) rootView.findViewById(R.id.addNewButton);
		nb.setOnClickListener(this);

		// Hide the soft keyboard
		final InputMethodManager imm = (InputMethodManager) getActivity()
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(rootView.getWindowToken(), 0);

		return rootView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		entriesAdapter = new EntriesAdapter(getActivity(), R.layout.entry_element, entries);
		setListAdapter(entriesAdapter);
	}

	@Override
	public void onListItemClick(ListView l, View v, int pos, long id) {
		Log.d(TAG, "Clicked entry with index " + pos + " in the list of entries");
		super.onListItemClick(l, v, pos, id);
		InterfaceWithRust.INSTANCE.go_to_menu_plus_arg(Defs.MENU_SHOW_ENTRY, pos);
	}

	@Override
	public void onClick(View view) {
		Log.d(TAG, "Clicked add new entry");
		InterfaceWithRust.INSTANCE.go_to_menu(Defs.MENU_NEW_ENTRY);
	}

	@Override
	public void onBackButton() {
		Log.d(TAG, "Back button pressed");
		InterfaceWithRust.INSTANCE.go_to_menu(Defs.MENU_MAIN);
	}

	@Override
	public void onSave(Bundle state) {
		// ignore for now
	}

	@Override
	public void onRestore(Bundle state) {
		InterfaceWithRust.INSTANCE.go_to_menu(Defs.MENU_ENTRIES_LIST);
	}
}
