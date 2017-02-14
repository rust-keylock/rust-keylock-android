package org.astonbitecode.rustkeylock.fragments;

import java.io.File;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.astonbitecode.rustkeylock.R;
import org.astonbitecode.rustkeylock.api.InterfaceWithRust;
import org.astonbitecode.rustkeylock.handlers.back.BackButtonHandler;
import org.astonbitecode.rustkeylock.handlers.state.SaveStateHandler;
import org.astonbitecode.rustkeylock.utils.Defs;

import android.app.Fragment;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class SelectPath extends Fragment implements BackButtonHandler, SaveStateHandler, OnClickListener {
	private static final long serialVersionUID = 1503736744138963548L;
	private final String TAG = getClass().getName();
	private boolean export;
	private transient EditText editPath;
	private transient EditText editPassword;
	private transient EditText editNumber;

	public SelectPath() {
	}

	public SelectPath(boolean export) {
		this.export = export;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_select_path, container, false);

		TextView title = (TextView) rootView.findViewById(R.id.selectPathLabel);
		if (export) {
			title.setText("Where to export?");
		} else {
			title.setText("What to import?");
		}

		ImageButton bib = (ImageButton) rootView.findViewById(R.id.browseButton);
		bib.setOnClickListener(this);

		EditText editPath = (EditText) rootView.findViewById(R.id.editCustomPath);
		File downloads = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmm");
		String proposedFilename = sdf.format(new Date()) + "_rust_keylock.exported";
		editPath.setText(downloads.getAbsolutePath() + File.separator + proposedFilename);
		this.editPath = editPath;

		TextView editPasswordLabel = (TextView) rootView.findViewById(R.id.selectPathPasswordLabel);
		editPasswordLabel.setVisibility(export ? View.GONE : View.VISIBLE);
		EditText editPassword = (EditText) rootView.findViewById(R.id.selectPathPassword);
		editPassword.setVisibility(export ? View.GONE : View.VISIBLE);
		this.editPassword = editPassword;

		TextView editNumberLabel = (TextView) rootView.findViewById(R.id.selectPathNumberLabel);
		editNumberLabel.setVisibility(export ? View.GONE : View.VISIBLE);
		EditText editNumber = (EditText) rootView.findViewById(R.id.selectPathFavoriteNumber);
		editNumber.setVisibility(export ? View.GONE : View.VISIBLE);
		this.editNumber = editNumber;

		Button setPathButton = (Button) rootView.findViewById(R.id.setPathButton);
		setPathButton.setOnClickListener(this);

		return rootView;
	}

	@Override
	public void onClick(View view) {
		if (view.getId() == R.id.browseButton) {
			try {
				Log.d(TAG, "Selecting a file");
				Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
				intent.setType("*/*");
				intent.addCategory(Intent.CATEGORY_OPENABLE);
				intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
				startActivityForResult(intent, 11);
			} catch (RuntimeException error) {
				Log.e(TAG, "Could not set Intent for choosing directory", error);
				Toast toast = Toast.makeText(getActivity(),
						"Could not locate a File Explorer application in the device. Please consider installing one.",
						Toast.LENGTH_LONG);
				toast.show();
			}
		} else if (view.getId() == R.id.setPathButton) {
			String path = editPath.getText().toString();
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

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);
		if (intent != null) {
			String retrievedPath = intent.getDataString();
			String realPath = null;
			// Try retrieving the path form content (Android Uri)
			try {
				Uri androidUri = Uri.parse(retrievedPath);
				realPath = getRealPathFromUri(androidUri);
				File f = new File(realPath);
				Log.d(TAG, "Selected path from Android Uri: " + f.getAbsolutePath());
				editPath.setText(f.getAbsolutePath());
			} catch (Exception error1) {
				// ignore
			}
			// If the path is not retrieved yet, try retrieving it from Java URI
			if (realPath == null) {
				try {
					URI javaUri = new URI(retrievedPath);
					File f = new File(javaUri);
					realPath = f.getAbsolutePath();
					Log.d(TAG, "Selected path from Java URI: " + realPath);
					editPath.setText(realPath);
				} catch (Exception e) {
					// ignore
				}
			}
			// If the path is still not retrieved yet, change the layout for
			// manual path input
			if (realPath == null) {
				Log.e(TAG, "The path is not valid");
				Toast toast = Toast.makeText(getActivity(), "The path is not valid", Toast.LENGTH_LONG);
				toast.show();
			}
		}
	}

	public String getRealPathFromUri(Uri contentUri) {
		String res = null;
		String[] proj = { MediaStore.MediaColumns.DATA };
		Cursor cursor = getActivity().getContentResolver().query(contentUri, proj, null, null, null);
		if (cursor.moveToFirst()) {
			int column_index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
			res = cursor.getString(column_index);
		}
		cursor.close();
		return res;
	}

	@Override
	public void onBackButton() {
		Log.d(TAG, "Back button pressed");
		InterfaceWithRust.INSTANCE.go_to_menu(Defs.MENU_MAIN);
	}

	@Override
	public void onSave(Bundle state) {
		state.putBoolean("export", export);
	}

	@Override
	public void onRestore(Bundle state) {
		export = state.getBoolean("export");
	}
}
