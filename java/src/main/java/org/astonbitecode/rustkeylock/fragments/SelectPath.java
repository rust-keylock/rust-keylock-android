package org.astonbitecode.rustkeylock.fragments;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.astonbitecode.rustkeylock.R;
import org.astonbitecode.rustkeylock.api.InterfaceWithRust;
import org.astonbitecode.rustkeylock.handlers.back.BackButtonHandler;
import org.astonbitecode.rustkeylock.utils.Defs;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

public class SelectPath extends Fragment implements BackButtonHandler, OnClickListener {
	private static final long serialVersionUID = 1503736744138963548L;
	private final String TAG = getClass().getName();
	private boolean export;
	private transient EditText editPath;
	private transient EditText editFileName;
	private transient EditText editPassword;
	private transient EditText editNumber;
	private String workingDirectoryPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
	private String filename = new SimpleDateFormat("yyyyMMdd_HHmm").format(new Date()) + "_rust_keylock";
	private int FRAGMENT_CODE_DIR = 11;
	private int FRAGMENT_CODE_FILE = 33;
	private transient BackButtonHandler backButtonHandler = this;

	public SelectPath() {
	}

	public SelectPath(boolean export) {
		this.export = export;
	}

	@Override
	public void onStart() {
		super.onStart();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (savedInstanceState != null) {
			this.export = savedInstanceState.getBoolean("export");
		}
		super.onCreateView(inflater, container, savedInstanceState);
		View rootView = inflater.inflate(R.layout.fragment_select_path, container, false);
		initialize(rootView);
		return rootView;
	}

	private void initialize(View view) {
		TextView title = (TextView) view.findViewById(R.id.selectPathLabel);
		if (export) {
			title.setText("Where to export?");
		} else {
			title.setText("What to import?");
		}

		ImageButton bib = (ImageButton) view.findViewById(R.id.browseButton);
		bib.setOnClickListener(this);
		ImageButton bfb = (ImageButton) view.findViewById(R.id.browseFileButton);
		bfb.setOnClickListener(this);
		bfb.setVisibility(export ? View.GONE : View.VISIBLE);

		EditText editPath = (EditText) view.findViewById(R.id.editCustomPath);
		editPath.setText(workingDirectoryPath);
		this.editPath = editPath;

		EditText editFilename = (EditText) view.findViewById(R.id.editFileName);
		filename = export ? filename : "";
		editFilename.setText(filename);
		editFilename.setEnabled(export);
		this.editFileName = editFilename;

		TextView editPasswordLabel = (TextView) view.findViewById(R.id.selectPathPasswordLabel);
		editPasswordLabel.setVisibility(export ? View.GONE : View.VISIBLE);
		EditText editPassword = (EditText) view.findViewById(R.id.selectPathPassword);
		editPassword.setVisibility(export ? View.GONE : View.VISIBLE);
		this.editPassword = editPassword;

		TextView editNumberLabel = (TextView) view.findViewById(R.id.selectPathNumberLabel);
		editNumberLabel.setVisibility(export ? View.GONE : View.VISIBLE);
		EditText editNumber = (EditText) view.findViewById(R.id.selectPathFavoriteNumber);
		editNumber.setVisibility(export ? View.GONE : View.VISIBLE);
		this.editNumber = editNumber;

		Button setPathButton = (Button) view.findViewById(R.id.setPathButton);
		setPathButton.setOnClickListener(this);
	}

	@Override
	public void onClick(View view) {
		if (view.getId() == R.id.browseButton) {
			FragmentTransaction ft = getActivity().getFragmentManager().beginTransaction();
			ft.addToBackStack(this.getClass().getName());
			DirectorySelector ds = new DirectorySelector();
			ds.setTargetFragment(this, FRAGMENT_CODE_DIR);
			ft.replace(R.id.container, ds);
			ft.commit();
			backButtonHandler = ds;
		} else if (view.getId() == R.id.browseFileButton) {
			FragmentTransaction ft = getActivity().getFragmentManager().beginTransaction();
			ft.addToBackStack(this.getClass().getName());
			FileSelector fs = new FileSelector(workingDirectoryPath);
			fs.setTargetFragment(this, FRAGMENT_CODE_FILE);
			ft.replace(R.id.container, fs);
			ft.commit();
			backButtonHandler = fs;
		} else if (view.getId() == R.id.setPathButton) {
			String path = editPath.getText().toString() + File.separator + editFileName.getText().toString();
			String pwd = !export ? editPassword.getText().toString() : "DUMMY";
			String num = !export ? editNumber.getText().toString() : "-1";
			Log.d(TAG, (export ? "Exporting" : "Importing") + ". Path: " + path);
			if (path.isEmpty()) {
				editPath.setError("Required Field");
			} else if (pwd.isEmpty()) {
				editPassword.setError("Required Field");
			} else if (num.isEmpty()) {
				editNumber.setError("Required Field");
			} else {
				InterfaceWithRust.INSTANCE.export_import(path, export ? 1 : 0, pwd, Integer.parseInt(num));
			}
		} else {
			Log.e(TAG, "Unhandled id: " + view.getId());
		}
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		backButtonHandler = this;
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == FRAGMENT_CODE_DIR) {
			workingDirectoryPath = data.getStringExtra("directory");
			filename = export ? filename : "";
		} else if (requestCode == FRAGMENT_CODE_FILE) {
			filename = data.getStringExtra("file");
		}
	}

	@Override
	public void onBackButton() {
		Log.d(TAG, "Back button pressed");
		if (backButtonHandler == this) {
			InterfaceWithRust.INSTANCE.go_to_menu(Defs.MENU_MAIN);
		} else {
			try {
				backButtonHandler.onBackButton();
			} catch (Exception e) {
				InterfaceWithRust.INSTANCE.go_to_menu(Defs.MENU_MAIN);
			}
		}
	}

	@Override
	public void onSaveInstanceState(Bundle state) {
		state.putBoolean("export", export);
	}
}
