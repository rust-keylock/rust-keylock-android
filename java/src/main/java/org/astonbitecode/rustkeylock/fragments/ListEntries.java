package org.astonbitecode.rustkeylock.fragments;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.astonbitecode.rustkeylock.R;
import org.astonbitecode.rustkeylock.adapters.EntriesAdapter;
import org.astonbitecode.rustkeylock.api.InterfaceWithRust;
import org.astonbitecode.rustkeylock.api.JavaEntry;
import org.astonbitecode.rustkeylock.handlers.back.BackButtonHandler;
import org.astonbitecode.rustkeylock.utils.Defs;

import android.app.ListFragment;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

public class ListEntries extends ListFragment implements OnClickListener, BackButtonHandler {
	private static final long serialVersionUID = 8765819759487480794L;
	private final String TAG = getClass().getName();
	private List<JavaEntry> entries;
	private EntriesAdapter entriesAdapter;
	private String filter;

	public ListEntries() {
		this.entries = new ArrayList<>();
	}

	public ListEntries(List<JavaEntry> entries, String filter) {
		this.entries = entries;
		this.filter = filter;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		restore(savedInstanceState);
		if (savedInstanceState != null) {
			InterfaceWithRust.INSTANCE.go_to_menu_plus_arg(Defs.MENU_ENTRIES_LIST, Defs.EMPTY_ARG, filter);
		}
		View rootView = inflater.inflate(R.layout.fragment_list_entries, container, false);
		Button nb = (Button) rootView.findViewById(R.id.addNewButton);
		nb.setOnClickListener(this);

		EditText filterText = (EditText) rootView.findViewById(R.id.editFilter);
		filterText.setText(filter);
		filterText.addTextChangedListener(new TextWatcher() {
			private Timer timer = new Timer();
			private final long DELAY = 500;

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				// ignore
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				// ignore
			}

			@Override
			public void afterTextChanged(final Editable s) {
				timer.cancel();
				timer = new Timer();
				timer.schedule(new TimerTask() {
					@Override
					public void run() {
						InterfaceWithRust.INSTANCE.go_to_menu_plus_arg(Defs.MENU_ENTRIES_LIST, Defs.EMPTY_ARG,
								s != null ? s.toString() : "");
					}
				}, DELAY);
			}
		});
		if (filter.length() > 0) {
			filterText.setFocusableInTouchMode(true);
			filterText.requestFocus();
		} else {
			// Hide the soft keyboard
			final InputMethodManager imm = (InputMethodManager) getActivity()
					.getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(rootView.getWindowToken(), 0);
		}

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
		InterfaceWithRust.INSTANCE.go_to_menu_plus_arg(Defs.MENU_SHOW_ENTRY, pos + "", Defs.EMPTY_ARG);
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
	public void onSaveInstanceState(Bundle state) {
		state.putString("filter", filter);
	}

	private void restore(Bundle state) {
		if (state != null) {
			filter = state.getString("filter");
		}
	}

}
