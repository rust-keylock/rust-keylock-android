package org.astonbitecode.rustkeylock.callbacks;

import org.astonbitecode.rustkeylock.MainActivity;
import org.astonbitecode.rustkeylock.R;
import org.astonbitecode.rustkeylock.api.InterfaceWithRust;
import org.astonbitecode.rustkeylock.api.JavaEntry;
import org.astonbitecode.rustkeylock.fragments.ShowEntry;

import android.util.Log;

public class ShowEntryCb implements InterfaceWithRust.EntryCallback {
	private final String TAG = getClass().getName();

	@Override
	public void apply(JavaEntry.ByReference anEntry, int entryIndex, boolean edit, boolean delete) {
		Log.d(TAG, "Callback with JavaEntry name " + anEntry.getName());
		MainActivity mainActivity = MainActivity.getActiveActivity();
		Runnable uiRunnable = new UiThreadRunnable(anEntry, entryIndex, edit, delete, mainActivity);
		mainActivity.runOnUiThread(uiRunnable);
	}

	private class UiThreadRunnable implements Runnable {
		private MainActivity mainActivity = null;
		private JavaEntry entry = null;
		private int entryIndex;
		private boolean edit = false;
		private boolean delete = false;

		public UiThreadRunnable(JavaEntry entry, int entryIndex, boolean edit, boolean delete, MainActivity mainActivity) {
			this.entry = entry;
			this.entryIndex = entryIndex;
			this.edit = edit;
			this.delete = delete;
			this.mainActivity = mainActivity;
		}

		@Override
		public void run() {
			ShowEntry se = new ShowEntry(entry, entryIndex, edit, delete);
			mainActivity.setBackButtonHandler(se);
			mainActivity.getFragmentManager().beginTransaction().replace(R.id.container, se).commitAllowingStateLoss();
		}
	}

}
