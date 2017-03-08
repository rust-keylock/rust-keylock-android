package org.astonbitecode.rustkeylock.fragments;

import org.astonbitecode.rustkeylock.R;
import org.astonbitecode.rustkeylock.api.InterfaceWithRust;
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

public class ExitMenu extends Fragment implements OnClickListener, BackButtonHandler {
	private static final long serialVersionUID = -3867048671982686746L;
	private final String TAG = getClass().getName();

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_exit_menu, container, false);
		addButtonListeners(rootView);
		return rootView;
	}

	private void addButtonListeners(View rootView) {
		Button yesb = (Button) rootView.findViewById(R.id.exitButtonYes);
		yesb.setOnClickListener(this);
		Button nob = (Button) rootView.findViewById(R.id.exitButtonNo);
		nob.setOnClickListener(this);
	}

	@Override
	public void onClick(View view) {
		if (view.getId() == R.id.exitButtonYes) {
			Log.d(TAG, "The User selected to force Exit with unsaved data");
			InterfaceWithRust.INSTANCE.go_to_menu(Defs.MENU_FORCE_EXIT);
		} else {
			Log.e(TAG, "The User selected not to exit because of unsaved data");
			InterfaceWithRust.INSTANCE.go_to_menu(Defs.MENU_MAIN);
		}
	}

	@Override
	public void onBackButton() {
		Log.d(TAG, "Back button pressed");
		InterfaceWithRust.INSTANCE.go_to_menu(Defs.MENU_MAIN);
	}
}
