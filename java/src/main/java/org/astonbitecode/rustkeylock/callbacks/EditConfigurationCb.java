package org.astonbitecode.rustkeylock.callbacks;

import java.util.List;

import org.astonbitecode.rustkeylock.MainActivity;
import org.astonbitecode.rustkeylock.R;
import org.astonbitecode.rustkeylock.api.InterfaceWithRust.StringListCallback;
import org.astonbitecode.rustkeylock.api.StringList.ByReference;
import org.astonbitecode.rustkeylock.fragments.EditConfiguration;

import android.util.Log;

public class EditConfigurationCb implements StringListCallback {
	private final String TAG = getClass().getName();

	@Override
	public void apply(ByReference stringList) {
		Log.d(TAG, "Callback for editing configuration");
		MainActivity mainActivity = MainActivity.getActiveActivity();
		Runnable uiRunnable = new UiThreadRunnable(stringList.getStrings(), mainActivity);
		mainActivity.runOnUiThread(uiRunnable);
	}

	private class UiThreadRunnable implements Runnable {
		private List<String> strings = null;
		private MainActivity mainActivity = null;

		public UiThreadRunnable(List<String> strings, MainActivity mainActivity) {
			this.strings = strings;
			this.mainActivity = mainActivity;
		}

		@Override
		public void run() {
			EditConfiguration ec = new EditConfiguration(strings);
			mainActivity.setBackButtonHandler(ec);
			mainActivity.getFragmentManager().beginTransaction().replace(R.id.container, ec).commitAllowingStateLoss();
		}
	}
}
