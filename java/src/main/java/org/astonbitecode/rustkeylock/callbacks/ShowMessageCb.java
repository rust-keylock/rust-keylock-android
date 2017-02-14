package org.astonbitecode.rustkeylock.callbacks;

import org.astonbitecode.rustkeylock.MainActivity;
import org.astonbitecode.rustkeylock.api.InterfaceWithRust;
import org.astonbitecode.rustkeylock.utils.Defs;

import android.util.Log;
import android.widget.Toast;

public class ShowMessageCb implements InterfaceWithRust.RustCallback {
	private final String TAG = getClass().getName();

	@Override
	public void apply(String message) {
		Log.d(TAG, "Callback for showing message " + message);
		MainActivity mainActivity = MainActivity.getActiveActivity();
		Runnable uiRunnable = new UiThreadRunnable(message, mainActivity);
		mainActivity.runOnUiThread(uiRunnable);
	}

	private class UiThreadRunnable implements Runnable {
		private MainActivity mainActivity = null;
		private String message = null;

		public UiThreadRunnable(String message, MainActivity mainActivity) {
			this.message = message;
			this.mainActivity = mainActivity;
		}

		@Override
		public void run() {
			Toast toast = Toast.makeText(mainActivity, message, Toast.LENGTH_LONG);
			toast.show();
			// It doesn't matter which menu we return from the show message screen. The logic of the rust-keylock library only needs something to proceed. 
			InterfaceWithRust.INSTANCE.go_to_menu(Defs.MENU_MAIN);
		}
	}

}
