package org.astonbitecode.rustkeylock.fragments;

import org.astonbitecode.rustkeylock.R;
import org.astonbitecode.rustkeylock.api.InterfaceWithRust;
import org.astonbitecode.rustkeylock.handlers.back.BackButtonHandler;
import org.astonbitecode.rustkeylock.utils.Defs;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;

public class MainMenu extends Fragment implements OnClickListener, BackButtonHandler {
	private static final long serialVersionUID = -4385132544016979748L;
	private final String TAG = getClass().getName();

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_main_menu, container, false);
		addButtonListeners(rootView);

		// Hide the soft keyboard
		final InputMethodManager imm = (InputMethodManager) getActivity()
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(rootView.getWindowToken(), 0);

		return rootView;
	}

	private void addButtonListeners(View rootView) {
		Button bl = (Button) rootView.findViewById(R.id.listButton);
		bl.setOnClickListener(this);
		Button bs = (Button) rootView.findViewById(R.id.saveButton);
		bs.setOnClickListener(this);
		Button bcp = (Button) rootView.findViewById(R.id.changePasswordButton);
		bcp.setOnClickListener(this);
		Button be = (Button) rootView.findViewById(R.id.exitButton);
		be.setOnClickListener(this);
		Button bexp = (Button) rootView.findViewById(R.id.exportButton);
		bexp.setOnClickListener(this);
		Button binp = (Button) rootView.findViewById(R.id.importButton);
		binp.setOnClickListener(this);
	}

	@Override
	public void onClick(View view) {
		if (view.getId() == R.id.listButton) {
			Log.d(TAG, "The User Selected to List Entries");
			InterfaceWithRust.INSTANCE.go_to_menu_plus_arg(Defs.MENU_ENTRIES_LIST, Defs.EMPTY_ARG, "");
		} else if (view.getId() == R.id.saveButton) {
			Log.d(TAG, "The User Selected to Save Entries");
			InterfaceWithRust.INSTANCE.go_to_menu(Defs.MENU_SAVE);
		} else if (view.getId() == R.id.changePasswordButton) {
			Log.d(TAG, "The User Selected to Change the password");
			InterfaceWithRust.INSTANCE.go_to_menu(Defs.MENU_CHANGE_PASS);
		} else if (view.getId() == R.id.exitButton) {
			Log.d(TAG, "The User Selected to Exit rust-keylock");
			InterfaceWithRust.INSTANCE.go_to_menu(Defs.MENU_EXIT);
		} else if (view.getId() == R.id.exportButton) {
			Log.d(TAG, "The User Selected to export entries");
			InterfaceWithRust.INSTANCE.go_to_menu(Defs.MENU_EXPORT_ENTRIES);
		} else if (view.getId() == R.id.importButton) {
			Log.d(TAG, "The User Selected to import entries");
			InterfaceWithRust.INSTANCE.go_to_menu(Defs.MENU_IMPORT_ENTRIES);
		} else {
			Log.e(TAG, "The User selected a Menu that is not implemented yet in Rust");
		}
	}

	@Override
	public void onBackButton() {
		Log.d(TAG, "Back button pressed");
		InterfaceWithRust.INSTANCE.go_to_menu(Defs.MENU_EXIT);
	}
}
