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
import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import org.astonbitecode.rustkeylock.R;
import org.astonbitecode.rustkeylock.adapters.FilesAdapter;
import org.astonbitecode.rustkeylock.handlers.back.BackButtonHandler;

import androidx.fragment.app.ListFragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public class FileSelector extends ListFragment implements OnClickListener, BackButtonHandler {
    private static final long serialVersionUID = 1382314701594691684L;
    private final String TAG = getClass().getName();
    private final String NO_FILE_SELECTED = "No File selected";

    private FilesAdapter filesAdapter;
    private File currentDirectory;
    private String selectedFileName;
    private TextView currentDirectoryTextView;
    private TextView selectedFileTextView;

    @SuppressLint("ValidFragment")
    public FileSelector(String directoryFullPath) {
        currentDirectory = new File(directoryFullPath);
    }

    public FileSelector() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_file_selector, container, false);
        Button sdb = rootView.findViewById(R.id.selectFileButton);
        sdb.setOnClickListener(this);
        TextView cdtv = rootView.findViewById(R.id.fileSelectorCurrent);
        cdtv.setText("Showing files in \"" + currentDirectory.getName() + "\"");
        currentDirectoryTextView = cdtv;
        TextView sftv = rootView.findViewById(R.id.fileSelectorSelectedFile);
        sftv.setText(NO_FILE_SELECTED);
        selectedFileTextView = sftv;

        // Hide the soft keyboard
        final InputMethodManager imm = (InputMethodManager) getActivity()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(rootView.getWindowToken(), 0);

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        filesAdapter = new FilesAdapter(getActivity(), R.layout.file_element, getFilesOfDirectory());
        setListAdapter(filesAdapter);
        currentDirectoryTextView.setText("Showing files in \"" + currentDirectory.getName() + "\"");
        selectedFileTextView
                .setText(selectedFileName != null ? "Selected file: " + selectedFileName : NO_FILE_SELECTED);
    }

    @Override
    public void onListItemClick(ListView l, View v, int pos, long id) {
        Log.d(TAG, "Clicked file with index " + pos + " in the list of files");
        super.onListItemClick(l, v, pos, id);

        FileEntry selectedFileEntry = filesAdapter.getItem(pos);
        selectedFileName = selectedFileEntry.getName();
        selectedFileTextView.setText("Selected file: " + selectedFileName);
        Log.d(TAG, "Selected file " + selectedFileName);
    }

    @Override
    public void onClick(View view) {
        Log.d(TAG, "Returning File " + selectedFileName);
        Intent intent = new Intent(getActivity(), this.getClass());
        intent.putExtra("file", selectedFileName);
        getTargetFragment().onActivityResult(getTargetRequestCode(), 0, intent);
        getFragmentManager().popBackStack();
    }

    @Override
    public void onBackButton() {
        Log.d(TAG, "Back button pressed");
        getFragmentManager().popBackStack();
    }

    private List<FileEntry> getFilesOfDirectory() {
        String[] files = currentDirectory.list((current, name) -> new File(current, name).isFile());

        List<FileEntry> fileEntries = new ArrayList<>();
        if (currentDirectory != null) {
            // Add the rest directories
            if (files != null) {
                for (String file : files) {
                    fileEntries.add(new FileEntry(currentDirectory.getAbsolutePath(), file));
                }
            }
        }

        return fileEntries;
    }

    public class FileEntry {
        private String name;
        private String absolutePath;

        public FileEntry(String parentPath, String name) {
            this.name = name;
            this.absolutePath = parentPath;
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
