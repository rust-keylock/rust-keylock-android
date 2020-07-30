package org.astonbitecode.rustkeylock;

import android.util.Log;
import org.astonbitecode.j4rs.api.java2rust.Java2RustUtils;
import org.astonbitecode.rustkeylock.api.InterfaceWithRust;
import org.astonbitecode.rustkeylock.callbacks.*;

import java.io.*;

public class RustRunnable implements Runnable {
    private final String TAG = getClass().getName();
    private MainActivity mainActivity;
    private static final String CertsExternalBasePath = "/sdcard/Download/rust-keylock/etc/ssl/certs";
    private static final String CertsBasePath = "/data/data/org.astonbitecode.rustkeylock/files/etc/ssl/certs";
    private static final String CertTargetPath = CertsBasePath + "/rkl_cacert.pem";

    public RustRunnable(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
        copyCerts(mainActivity);
    }

    private void copyCerts(MainActivity mainActivity) {
        try {
            File targetExternal = new File(CertsExternalBasePath);
            targetExternal.mkdirs();

            File target = new File(CertTargetPath);
            if (!target.exists()) {
                File base = new File(CertsBasePath);
                base.mkdirs();

                Log.w(TAG, "Copying the certificates in " + CertTargetPath);
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
            InterfaceWithRust.INSTANCE.execute(Java2RustUtils.createInstance(CertTargetPath));
        } catch (Exception error) {
            Log.e(TAG, "Native rust-keylock error detected", error);
        } finally {
            mainActivity.finish();
            System.exit(0);
        }
    }
}