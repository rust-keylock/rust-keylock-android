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
package org.astonbitecode.rustkeylock.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import com.sun.jna.StringArray;
import org.astonbitecode.rustkeylock.MainActivity;
import org.astonbitecode.rustkeylock.R;
import org.astonbitecode.rustkeylock.api.InterfaceWithRust;
import org.astonbitecode.rustkeylock.api.StringList;
import org.astonbitecode.rustkeylock.handlers.back.BackButtonHandler;
import org.astonbitecode.rustkeylock.utils.Defs;

import java.util.ArrayList;
import java.util.List;

public class EditConfiguration extends Fragment implements OnClickListener, BackButtonHandler {
    private final String TAG = getClass().getName();
    private transient ArrayList<String> strings;
    private transient CheckBox useSelfSignedCert;
    private transient EditText nextcloudUrlText;
    private transient EditText nextcloudUsernameText;
    private transient EditText nextcloudPasswordText;

    public EditConfiguration(List<String> strings) {
        this.strings = new ArrayList<>(strings);
    }

    public EditConfiguration() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        restore(savedInstanceState);
        View rootView = inflater.inflate(R.layout.fragment_edit_configuration, container, false);
        if (this.strings != null) {
            prepareUiElements(rootView);
        }
        return rootView;
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.editConfigurationOkButton) {
            Log.d(TAG, "Clicked Ok in configuration");
            String url = nextcloudUrlText.getText() != null ? nextcloudUrlText.getText().toString() : "";
            String user = nextcloudUsernameText.getText() != null ? nextcloudUsernameText.getText().toString() : "";
            String password = nextcloudPasswordText.getText() != null ? nextcloudPasswordText.getText().toString() : "";
            String useSelfSignedCertString = new Boolean(useSelfSignedCert.isChecked()).toString();
            Log.d(TAG, "Saving configuration (password not shown here): " + url + ", " + user + ", " + useSelfSignedCertString);

            boolean errorsOccured = false;

            if (!errorsOccured) {
                String[] stringsToSave = {url, user, password, useSelfSignedCertString};
                StringList sl = new StringList.ByReference();
                sl.numberOfstrings = 4;
                sl.strings = new StringArray(stringsToSave);
                InterfaceWithRust.INSTANCE.set_configuration(sl);
            }
        } else if (view.getId() == R.id.editConfigurationCancelButton) {
            Log.d(TAG, "Clicked Cancel in configuration");
            InterfaceWithRust.INSTANCE.go_to_menu(Defs.MENU_MAIN);
        }
    }

    private void prepareUiElements(View v) {
        Button ob = (Button) v.findViewById(R.id.editConfigurationOkButton);
        ob.setOnClickListener(this);
        Button cb = (Button) v.findViewById(R.id.editConfigurationCancelButton);
        cb.setOnClickListener(this);

        EditText urlText = (EditText) v.findViewById(R.id.editNextcloudUrl);
        urlText.setText(strings.get(0));
        this.nextcloudUrlText = urlText;
        EditText userText = (EditText) v.findViewById(R.id.editNextcloudUser);
        userText.setText(strings.get(1));
        this.nextcloudUsernameText = userText;
        EditText passwordText = (EditText) v.findViewById(R.id.editNextcloudPassword);
        passwordText.setText(strings.get(2));
        this.nextcloudPasswordText = passwordText;
        CheckBox useSsc = (CheckBox) v.findViewById(R.id.editNextcloudUseSelfSignedCert);
        useSsc.setChecked(new Boolean(strings.get(3)));
        this.useSelfSignedCert = useSsc;
    }

    @Override
    public void onBackButton() {
        Log.d(TAG, "Back button pressed");
        InterfaceWithRust.INSTANCE.go_to_menu(Defs.MENU_MAIN);
    }

    @Override
    public void onSaveInstanceState(Bundle state) {
        state.putStringArrayList("strings", strings);
    }

    private void restore(Bundle state) {
        if (state != null) {
            this.strings = state.getStringArrayList("strings");
        }
    }

}
