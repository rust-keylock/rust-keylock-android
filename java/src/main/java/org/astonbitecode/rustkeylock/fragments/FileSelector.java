package org.astonbitecode.rustkeylock.fragments;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

import org.astonbitecode.rustkeylock.R;
import org.astonbitecode.rustkeylock.adapters.FilesAdapter;
import org.astonbitecode.rustkeylock.handlers.back.BackButtonHandler;

import android.app.ListFragment;
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

	private FilesAdapter filesAdapter;
	private File currentDirectory;
	private String selectedFileName;
	private TextView currentDirectoryTextView;
	private TextView selectedFileTextView;

	public FileSelector(String directoryFullPath) {
		currentDirectory = new File(directoryFullPath);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_file_selector, container, false);
		Button sdb = (Button) rootView.findViewById(R.id.selectFileButton);
		sdb.setOnClickListener(this);
		TextView cdtv = (TextView) rootView.findViewById(R.id.fileSelectorCurrent);
		cdtv.setText("Showing files in " + currentDirectory.getName());
		currentDirectoryTextView = cdtv;
		TextView sftv = (TextView) rootView.findViewById(R.id.fileSelectorSelectedFile);
		sftv.setText("No file selected");
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
		currentDirectoryTextView.setText(currentDirectory.getAbsolutePath());
	}

	@Override
	public void onListItemClick(ListView l, View v, int pos, long id) {
		Log.d(TAG, "Clicked file with index " + pos + " in the list of files");
		super.onListItemClick(l, v, pos, id);

		FileEntry selectedFileEntry = filesAdapter.getItem(pos);
		selectedFileName = selectedFileEntry.getName();
		selectedFileTextView.setText(selectedFileName);
		Log.d(TAG, "Selected file " + selectedFileName);
	}

	@Override
	public void onClick(View view) {
		Log.d(TAG, "Selected File " + selectedFileName);
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
		String[] files = currentDirectory.list(new FilenameFilter() {
			@Override
			public boolean accept(File current, String name) {
				return new File(current, name).isFile();
			}
		});

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
