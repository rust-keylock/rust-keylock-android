package org.astonbitecode.rustkeylock.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import org.astonbitecode.rustkeylock.R;

public class PleaseWait extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_please_wait, container, false);
        return rootView;
    }
}
