package org.astonbitecode.rustkeylock;

import android.util.Log;
import org.astonbitecode.rustkeylock.api.InterfaceWithRust;

public class RustRunnable implements Runnable {
    private final String TAG = getClass().getName();
    private MainActivity mainActivity;

    public RustRunnable(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    @Override
    public void run() {
        Log.d(TAG, "Initializing rust-keylock native");
        try {
            InterfaceWithRust.INSTANCE.execute();
        } catch (Exception error) {
            Log.e(TAG, "Native rust-keylock error detected", error);
        } finally {
            mainActivity.finish();
            System.exit(0);
        }
    }
}