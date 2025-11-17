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

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

import org.astonbitecode.rustkeylock.R;
import org.astonbitecode.rustkeylock.adapters.DirectoriesAdapter;
import org.astonbitecode.rustkeylock.handlers.back.BackButtonHandler;

import androidx.fragment.app.ListFragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import org.jetbrains.annotations.NotNull;

public class DirectorySelector extends ListFragment implements OnClickListener, BackButtonHandler {
    private static final long serialVersionUID = 1382314701594691684L;
    private final String TAG = getClass().getName();
    private final String PARENT_IDENTIFIER = "..";
    private final String CURR_DIR_PREFIX = "Current directory: ";

    private DirectoriesAdapter directoriesAdapter;
    private File currentDirectory;
    private TextView currentDirectoryTextView;

    public DirectorySelector() {
        currentDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_directory_selector, container, false);
        Button sdb = rootView.findViewById(R.id.selectDirectoryButton);
        sdb.setOnClickListener(this);
        TextView cdtv = rootView.findViewById(R.id.directorySelectorCurrent);
        currentDirectoryTextView = cdtv;

        // Hide the soft keyboard
        final InputMethodManager imm = (InputMethodManager) getActivity()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(rootView.getWindowToken(), 0);

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        directoriesAdapter = new DirectoriesAdapter(getActivity(), R.layout.directory_element,
                new ArrayList<>());
        setListAdapter(directoriesAdapter);
        currentDirectoryTextView.setText(CURR_DIR_PREFIX + currentDirectory.getAbsolutePath());
        applyCurrentDirectory();
    }

    private void applyCurrentDirectory() {
        List<DirectoryEntry> currentEntries = getSubdirectories();
        if (!currentEntries.isEmpty()) {
            directoriesAdapter.clear();
            directoriesAdapter.addAll(getSubdirectories());
        } else {
            Toast toast = Toast.makeText(getActivity(), "Cannot enter directory " + currentDirectory.getAbsolutePath(),
                    Toast.LENGTH_LONG);
            toast.show();
        }
    }

    @Override
    public void onListItemClick(@NotNull ListView l, @NotNull View v, int pos, long id) {
        Log.d(TAG, "Clicked directory with index " + pos + " in the list of directories");
        super.onListItemClick(l, v, pos, id);

        DirectoryEntry selectedDirectory = directoriesAdapter.getItem(pos);
        currentDirectory = new File(selectedDirectory.getAbsolutePath());
        currentDirectoryTextView.setText(CURR_DIR_PREFIX + currentDirectory.getAbsolutePath());
        Log.d(TAG, "Current directory is " + currentDirectory);

        applyCurrentDirectory();
    }

    @Override
    public void onClick(View view) {
        Log.d(TAG, "Selected directory " + currentDirectory.getAbsolutePath());
        Intent intent = new Intent(getActivity(), this.getClass());
        intent.putExtra("directory", currentDirectory.getAbsolutePath());
        getTargetFragment().onActivityResult(getTargetRequestCode(), 0, intent);
        getFragmentManager().popBackStack();
    }

    @Override
    public void onBackButton() {
        Log.d(TAG, "Back button pressed");
        getFragmentManager().popBackStack();
    }

    private List<DirectoryEntry> getSubdirectories() {
        String[] directories = currentDirectory.list(new FilenameFilter() {
            @Override
            public boolean accept(File current, String name) {
                return new File(current, name).isDirectory();
            }
        });

        List<DirectoryEntry> dirEntries = new ArrayList<>();
        File parent = currentDirectory.getParentFile();
        if (parent != null) {
            // Add a go up directory
            dirEntries.add(new DirectoryEntry(parent.getAbsolutePath(), PARENT_IDENTIFIER));
            // Add the rest directories
            if (directories != null) {
                for (String directory : directories) {
                    dirEntries.add(new DirectoryEntry(currentDirectory.getAbsolutePath(), directory));
                }
            }
        }

        return dirEntries;
    }

    public class DirectoryEntry {
        private String name;
        private String absolutePath;

        public DirectoryEntry(String parentPath, String name) {
            this.name = name;
            if (name == PARENT_IDENTIFIER) {
                this.absolutePath = parentPath;
            } else {
                this.absolutePath = parentPath + File.separator + name;
            }
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getAbsolutePath() {
            return absolutePath;
        }

        public void setAbsolutePath(String absolutePath) {
            this.absolutePath = absolutePath;
        }

    }
}
