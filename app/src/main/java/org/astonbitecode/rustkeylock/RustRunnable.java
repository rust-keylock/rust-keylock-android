package org.astonbitecode.rustkeylock;

import android.os.Environment;
import android.util.Log;
import org.astonbitecode.j4rs.api.java2rust.Java2RustUtils;
import org.astonbitecode.rustkeylock.api.InterfaceWithRust;

import java.io.*;

public class RustRunnable implements Runnable {
    private final String TAG = getClass().getName();
    private MainActivity mainActivity;
    private final String certsExternalBasePath;
    private final String certsBasePath;
    private final String certTargetPath;

    public RustRunnable(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
        certsExternalBasePath = Environment.getExternalStorageDirectory().getPath() + "/Download/rust-keylock/etc/ssl/certs";
        certsBasePath = mainActivity.getBaseContext().getFilesDir().getPath() + "/data/org.astonbitecode.rustkeylock/files/etc/ssl/certs";
        certTargetPath = certsBasePath + "/rkl_cacert.pem";
        copyCerts(mainActivity);
    }

    private void copyCerts(MainActivity mainActivity) {
        try {
            File targetExternal = new File(certsExternalBasePath);
            targetExternal.mkdirs();

            File target = new File(certTargetPath);
            if (!target.exists()) {
                File base = new File(certsBasePath);
                base.mkdirs();

                Log.w(TAG, "Copying the certificates in " + certTargetPath);
                final InputStream in = mainActivity.getAssets().open("certs/rkl_cacert.pem");
                byte[] buffer = new byte[in.available()];
                in.read(buffer);
                OutputStream out = new FileOutputStream(target);
                out.write(buffer);
                out.flush();
                out.close();
                in.close();
            }
        } catch (IOException error) {
            Log.e(TAG, "Could not copy the certificates...", error);
        }
    }

    @Override
    public void run() {
        Log.d(TAG, "Initializing rust-keylock native");
        try {
            InterfaceWithRust.INSTANCE.execute(Java2RustUtils.createInstance(certTargetPath));
        } catch (Exception error) {
            Log.e(TAG, "Native rust-keylock error detected", error);
        } finally {
            mainActivity.finish();
            System.exit(0);
        }
    }
}