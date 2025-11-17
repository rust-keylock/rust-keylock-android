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
package org.astonbitecode.rustkeylock.fragments;

import android.annotation.SuppressLint;
import androidx.fragment.app.ListFragment;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import org.astonbitecode.rustkeylock.MainActivity;
import org.astonbitecode.rustkeylock.R;
import org.astonbitecode.rustkeylock.adapters.EntriesAdapter;
import org.astonbitecode.rustkeylock.api.InterfaceWithRust;
import org.astonbitecode.rustkeylock.api.JavaEntry;
import org.astonbitecode.rustkeylock.api.stubs.JavaMenu;
import org.astonbitecode.rustkeylock.handlers.back.BackButtonHandler;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class ListEntries extends ListFragment implements OnClickListener, BackButtonHandler {
    private static final long serialVersionUID = 8765819759487480794L;
    private final String TAG = getClass().getName();
    private final List<JavaEntry> entries;
    private String filter;
    private transient EditText filterEditText;

    public ListEntries() {
        this.entries = new ArrayList<>();
    }

    @SuppressLint("ValidFragment")
    public ListEntries(List<JavaEntry> entries, String filter) {
        this.entries = entries;
        this.filter = filter;
    }

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        restore(savedInstanceState);
        if (savedInstanceState != null) {
            InterfaceWithRust.INSTANCE.go_to_menu(JavaMenu.EntriesList(filter));
        }
        View rootView = inflater.inflate(R.layout.fragment_list_entries, container, false);
        Button nb = rootView.findViewById(R.id.addNewButton);
        nb.setOnClickListener(this);
        Button mmb = rootView.findViewById(R.id.mainMenuButton);
        mmb.setOnClickListener(this);
        Button cp = rootView.findViewById(R.id.checkPasswordsButton);
        cp.setOnClickListener(this);
        Button fb = rootView.findViewById(R.id.filterButton);
        fb.setOnClickListener(this);

        filterEditText = rootView.findViewById(R.id.editFilter);
        filterEditText.setText(filter);
        if (!filter.isEmpty()) {
            filterEditText.setFocusableInTouchMode(true);
            filterEditText.requestFocus();
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
        EntriesAdapter entriesAdapter = new EntriesAdapter(getActivity(), R.layout.entry_element, entries);
        setListAdapter(entriesAdapter);
    }

    @Override
    public void onListItemClick(@NotNull ListView l, @NotNull View v, int pos, long id) {
        Log.d(TAG, "Clicked entry with index " + pos + " in the list of entries");
        super.onListItemClick(l, v, pos, id);
        InterfaceWithRust.INSTANCE.go_to_menu(JavaMenu.ShowEntry(pos));
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.mainMenuButton) {
            Log.d(TAG, "Clicked go to the Main menu");
            InterfaceWithRust.INSTANCE.go_to_menu(JavaMenu.Main());
        } else if (view.getId() == R.id.addNewButton) {
            Log.d(TAG, "Clicked add new entry");
            InterfaceWithRust.INSTANCE.go_to_menu(JavaMenu.NewEntry());
        } else if (view.getId() == R.id.checkPasswordsButton) {
            Log.d(TAG, "Clicked check passwords");
            new Thread(new PleaseWaitRunnable()).start();
            InterfaceWithRust.INSTANCE.check_passwords();
        } else if (view.getId() == R.id.filterButton) {
            Log.d(TAG, "Applying filter");
            InterfaceWithRust.INSTANCE.go_to_menu(JavaMenu.EntriesList(filterEditText.getText() != null ? filterEditText.getText().toString() : ""));
        }
    }

    @Override
    public void onBackButton() {
        Log.d(TAG, "Back button pressed");
        InterfaceWithRust.INSTANCE.go_to_menu(JavaMenu.Main());
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

    private static class PleaseWaitRunnable implements Runnable {
        private final MainActivity mainActivity;

        public PleaseWaitRunnable() {
            this.mainActivity = MainActivity.getActiveActivity();
        }

        @Override
        public void run() {
            PleaseWait pw = new PleaseWait();
            mainActivity.setBackButtonHandler(null);
            mainActivity.getSupportFragmentManager().beginTransaction().replace(R.id.container, pw).commitAllowingStateLoss();
        }
    }
}
