package org.astonbitecode.rustkeylock.callbacks;

import org.astonbitecode.rustkeylock.MainActivity;
import org.astonbitecode.rustkeylock.R;
import org.astonbitecode.rustkeylock.api.InterfaceWithRust;
import org.astonbitecode.rustkeylock.fragments.ChangePassword;
import org.astonbitecode.rustkeylock.fragments.EnterPassword;
import org.astonbitecode.rustkeylock.fragments.ExitMenu;
import org.astonbitecode.rustkeylock.fragments.MainMenu;
import org.astonbitecode.rustkeylock.fragments.SelectPath;
import org.astonbitecode.rustkeylock.utils.Defs;

import android.util.Log;

public class ShowMenuCb implements InterfaceWithRust.RustCallback {
	private final String TAG = getClass().getName();

	@Override
	public void apply(String menu) {
		Log.d(TAG, "Callback for showing menu " + menu);
		MainActivity mainActivity = MainActivity.getActiveActivity();
		Runnable uiRunnable = new UiThreadRunnable(menu, MainActivity.getActiveActivity());
		mainActivity.runOnUiThread(uiRunnable);
	}

	private class UiThreadRunnable implements Runnable {
		private String menu = null;
		private MainActivity mainActivity = null;

		public UiThreadRunnable(String menu, MainActivity mainActivity) {
			this.menu = menu;
			this.mainActivity = mainActivity;
		}

		@Override
		public void run() {
			if (menu.equals(Defs.MENU_TRY_PASS)) {
				EnterPassword ep = new EnterPassword();
				mainActivity.setBackButtonHandler(ep);
				mainActivity.setSaveStateHandler(ep);
				mainActivity.getFragmentManager().beginTransaction().replace(R.id.container, ep)
						.commitAllowingStateLoss();
			} else if (menu.equals(Defs.MENU_CHANGE_PASS)) {
				ChangePassword cp = new ChangePassword();
				mainActivity.setBackButtonHandler(cp);
				mainActivity.setSaveStateHandler(cp);
				mainActivity.getFragmentManager().beginTransaction().replace(R.id.container, cp)
						.commitAllowingStateLoss();
			} else if (menu.equals(Defs.MENU_MAIN)) {
				MainMenu mm = new MainMenu();
				mainActivity.setBackButtonHandler(mm);
				mainActivity.setSaveStateHandler(mm);
				mainActivity.getFragmentManager().beginTransaction().replace(R.id.container, mm)
						.commitAllowingStateLoss();
			} else if (menu.equals(Defs.MENU_EXIT)) {
				ExitMenu em = new ExitMenu();
				mainActivity.setBackButtonHandler(em);
				mainActivity.setSaveStateHandler(em);
				mainActivity.getFragmentManager().beginTransaction().replace(R.id.container, em)
						.commitAllowingStateLoss();
			} else if (menu.equals(Defs.MENU_EXPORT_ENTRIES)) {
				SelectPath sp = new SelectPath(true);
				mainActivity.setBackButtonHandler(sp);
				mainActivity.setSaveStateHandler(sp);
				mainActivity.getFragmentManager().beginTransaction().replace(R.id.container, sp)
						.commitAllowingStateLoss();
			} else if (menu.equals(Defs.MENU_IMPORT_ENTRIES)) {
				SelectPath sp = new SelectPath(false);
				mainActivity.setBackButtonHandler(sp);
				mainActivity.setSaveStateHandler(sp);
				mainActivity.getFragmentManager().beginTransaction().replace(R.id.container, sp)
						.commitAllowingStateLoss();
			} else {
				throw new RuntimeException("Cannot Show Menu with name '" + menu + "' and no arguments");
			}
		}
	}

}
