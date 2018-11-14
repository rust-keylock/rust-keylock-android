// Copyright 2017 astonbitecode
// This file is part of rust-keylock password manager.
//
// rust-keylock is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// rust-keylock is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with rust-keylock.  If not, see <http://www.gnu.org/licenses/>.
package org.astonbitecode.rustkeylock;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import org.astonbitecode.rustkeylock.api.InterfaceWithRust;
import org.astonbitecode.rustkeylock.handlers.back.BackButtonHandlable;
import org.astonbitecode.rustkeylock.handlers.back.BackButtonHandler;

public class MainActivity extends Activity implements BackButtonHandlable {
    private static MainActivity ACTIVE_ACTIVITY;

    public static MainActivity getActiveActivity() {
        return ACTIVE_ACTIVITY;
    }

    private final String TAG = getClass().getName();
    private Thread rustThread = null;
    private BackButtonHandler backButtonHandler;
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
        super.onSaveInstanceState(outState);
        outState.putString("WORKAROUND_FOR_BUG_19917_KEY", "WORKAROUND_FOR_BUG_19917_VALUE");
        outState.putLong("savedStateAt", System.currentTimeMillis());
    }

    @Override
    protected void onPause() {
        Log.i(TAG, "rust-keylock is being paused...");
        super.onPause();
        savedStateAt = System.currentTimeMillis();
    }

    @Override
    protected void onResume() {
        Log.i(TAG, "resuming rust-keylock...");
        super.onResume();
        checkIdleTime();
        savedStateAt = 0;
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
}
