package org.astonbitecode.rustkeylock;

import org.astonbitecode.rustkeylock.R;
import org.astonbitecode.rustkeylock.api.InterfaceWithRust;
import org.astonbitecode.rustkeylock.callbacks.LogCb;
import org.astonbitecode.rustkeylock.callbacks.ShowEntriesSetCb;
import org.astonbitecode.rustkeylock.callbacks.ShowEntryCb;
import org.astonbitecode.rustkeylock.callbacks.ShowMenuCb;
import org.astonbitecode.rustkeylock.callbacks.ShowMessageCb;
import org.astonbitecode.rustkeylock.handlers.back.BackButtonHandlable;
import org.astonbitecode.rustkeylock.handlers.back.BackButtonHandler;
import org.astonbitecode.rustkeylock.handlers.state.SaveStateHandleable;
import org.astonbitecode.rustkeylock.handlers.state.SaveStateHandler;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

public class MainActivity extends Activity implements BackButtonHandlable, SaveStateHandleable {
	private static MainActivity ACTIVE_ACTIVITY;

	public static MainActivity getActiveActivity() {
		return ACTIVE_ACTIVITY;
	}

	private final String TAG = getClass().getName();
	private Thread rustThread = null;
	private BackButtonHandler backButtonHandler;
	private SaveStateHandler saveStateHandler;
	private long savedStateAt = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		ACTIVE_ACTIVITY = this;
		if (savedInstanceState == null) {
			initializeRust();
		} else {
			savedStateAt = savedInstanceState.getLong("saveStateAt");
			checkIdleTime();
			// Restore the back button handler
			backButtonHandler = (BackButtonHandler) savedInstanceState.get("backButtonHandler");
			// Restore and invoke the state handler
			saveStateHandler = (SaveStateHandler) savedInstanceState.get("saveStateHandler");
			saveStateHandler.onRestore(savedInstanceState);
		}
	}

	private void checkIdleTime() {
		if (savedStateAt > 0) {
			long now = System.currentTimeMillis();
			// If paused for more than 60 seconds, close the application for
			// security reasons
			if (now - savedStateAt > 60000) {
				Log.w(TAG, "Closing because of beeing idle for too long...");
				 finish();
				 System.exit(0);
			}
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// Save the back button handler
		outState.putSerializable("backButtonHandler", backButtonHandler);
		// Save the state handler
		if (saveStateHandler != null) {
			outState.putSerializable("saveStateHandler", saveStateHandler);
			saveStateHandler.onSave(outState);
		}
		super.onSaveInstanceState(outState);
		outState.putString("WORKAROUND_FOR_BUG_19917_KEY", "WORKAROUND_FOR_BUG_19917_VALUE");
		outState.putLong("savedStateAt", System.currentTimeMillis());
	}

	@Override
	protected void onPause() {
		Log.w(TAG, "rust-keylock is being paused...");
		super.onPause();
		savedStateAt = System.currentTimeMillis();
	}

	@Override
	protected void onResume() {
		Log.w(TAG, "resuming rust-keylock...");
		super.onResume();
		checkIdleTime();
		savedStateAt = 0;
	}

	@Override
	public void setSaveStateHandler(SaveStateHandler saveStateHandler) {
		this.saveStateHandler = saveStateHandler;
	}

	// @Override
	// public boolean onCreateOptionsMenu(Menu menu) {
	// // Inflate the menu; this adds items to the action bar if it is present.
	// getMenuInflater().inflate(R.menu.main, menu);
	// return true;
	// }

	// @Override
	// public boolean onOptionsItemSelected(MenuItem item) {
	// // Handle action bar item clicks here. The action bar will
	// // automatically handle clicks on the Home/Up button, so long
	// // as you specify a parent activity in AndroidManifest.xml.
	// int id = item.getItemId();
	// if (id == R.id.action_settings) {
	// return true;
	// }
	// return super.onOptionsItemSelected(item);
	// }

	@Override
	public void onBackPressed() {
		if (backButtonHandler != null) {
			backButtonHandler.onBackButton();
		} else {
			super.onBackPressed();
		}
	}

	@Override
	public void setBackButtonHandler(BackButtonHandler backButtonHandler) {
		this.backButtonHandler = backButtonHandler;
	}

	private void initializeRust() {
		if (rustThread == null) {
			rustThread = new Thread(new RustRunnable(this));
			rustThread.start();
		} else {
			Log.w(TAG, "Native rust-keylock is already running!");
		}
	}

	private class RustRunnable implements Runnable {
		private MainActivity mainActivity;

		public RustRunnable(MainActivity mainActivity) {
			this.mainActivity = mainActivity;
		}

		@Override
		public void run() {
			Log.d(TAG, "Initializing rust-keylock native");
			try {
				InterfaceWithRust.INSTANCE.execute(new ShowMenuCb(), new ShowEntryCb(), new ShowEntriesSetCb(),
						new ShowMessageCb(), new LogCb());
			} catch (Exception error) {
				Log.e(TAG, "Native rust-keylock error detected", error);
			} finally {
				mainActivity.finish();
				System.exit(0);
			}
		}
	}
}
