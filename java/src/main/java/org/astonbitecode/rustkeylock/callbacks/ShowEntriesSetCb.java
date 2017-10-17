package org.astonbitecode.rustkeylock.callbacks;

import java.util.ArrayList;
import java.util.List;

import org.astonbitecode.rustkeylock.MainActivity;
import org.astonbitecode.rustkeylock.R;
import org.astonbitecode.rustkeylock.api.InterfaceWithRust;
import org.astonbitecode.rustkeylock.api.JavaEntriesSet;
import org.astonbitecode.rustkeylock.api.JavaEntry;
import org.astonbitecode.rustkeylock.fragments.ListEntries;
import org.astonbitecode.rustkeylock.utils.Defs;

import android.util.Log;

public class ShowEntriesSetCb implements InterfaceWithRust.EntriesSetCallback {
	private final String TAG = getClass().getName();

	@Override
	public void apply(JavaEntriesSet.ByReference entriesSet, String filter) {
		Log.d(TAG, "Callback with JavaEntriesSet " + entriesSet.numberOfEntries);
		List<JavaEntry> entries;
		// Workaround for handling empty list from Rust
		if (entriesSet.numberOfEntries == 1 && entriesSet.getEntries().get(0).name.equals(Defs.EMPTY_ARG)
				&& entriesSet.getEntries().get(0).user.equals(Defs.EMPTY_ARG)
				&& entriesSet.getEntries().get(0).pass.equals(Defs.EMPTY_ARG)
				&& entriesSet.getEntries().get(0).desc.equals(Defs.EMPTY_ARG)) {
			entries = new ArrayList<>();
		} else {
			entries = entriesSet.getEntries();
		}

		MainActivity mainActivity = MainActivity.getActiveActivity();
		Runnable uiRunnable = new UiThreadRunnable(entries, filter, mainActivity);
		mainActivity.runOnUiThread(uiRunnable);
	}

	private class UiThreadRunnable implements Runnable {
		private List<JavaEntry> entries = null;
		private MainActivity mainActivity = null;
		private String filter = null;

		public UiThreadRunnable(List<JavaEntry> entries, String filter, MainActivity mainActivity) {
			this.entries = entries;
			this.mainActivity = mainActivity;
			this.filter = filter.equals(Defs.EMPTY_ARG) ? "" : filter;
		}

		@Override
		public void run() {
			ListEntries le = new ListEntries(entries, filter);
			mainActivity.setBackButtonHandler(le);
			mainActivity.getFragmentManager().beginTransaction().replace(R.id.container, le).commitAllowingStateLoss();
		}
	}

}