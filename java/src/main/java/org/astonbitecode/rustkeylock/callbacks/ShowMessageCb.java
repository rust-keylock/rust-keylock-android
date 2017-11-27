package org.astonbitecode.rustkeylock.callbacks;

import java.util.List;

import org.astonbitecode.rustkeylock.MainActivity;
import org.astonbitecode.rustkeylock.R;
import org.astonbitecode.rustkeylock.api.InterfaceWithRust;
import org.astonbitecode.rustkeylock.api.JavaUserOption;
import org.astonbitecode.rustkeylock.api.JavaUserOptionsSet;
import org.astonbitecode.rustkeylock.fragments.ShowMessage;

import android.util.Log;

public class ShowMessageCb implements InterfaceWithRust.ShowMessageCallback {
	private final String TAG = getClass().getName();

	@Override
	public void apply(JavaUserOptionsSet.ByReference options, String message, String severity) {
		Log.d(TAG, "Callback for showing message " + message + " of severity " + severity);

		List<JavaUserOption> optionsList = options.getOptions();

		MainActivity mainActivity = MainActivity.getActiveActivity();
		Runnable uiRunnable = new UiThreadRunnable(optionsList, message, severity, mainActivity);
		mainActivity.runOnUiThread(uiRunnable);
	}

	private class UiThreadRunnable implements Runnable {
		private MainActivity mainActivity = null;
		private List<JavaUserOption> optionsList = null;
		private String message = null;
		private String severity = null;

		public UiThreadRunnable(List<JavaUserOption> optionsList, String message, String severity,
				MainActivity mainActivity) {
			this.optionsList = optionsList;
			this.message = message;
			this.severity = severity;
			this.mainActivity = mainActivity;
		}

		@Override
		public void run() {
			ShowMessage sm = new ShowMessage(severity, message, optionsList);
			mainActivity.setBackButtonHandler(sm);
			mainActivity.getFragmentManager().beginTransaction().replace(R.id.container, sm).commitAllowingStateLoss();
		}
	}

}
