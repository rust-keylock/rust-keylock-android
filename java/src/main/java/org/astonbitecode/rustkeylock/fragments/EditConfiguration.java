package org.astonbitecode.rustkeylock.fragments;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import android.widget.Button;
import android.widget.CheckBox;
import com.sun.jna.StringArray;
import org.astonbitecode.rustkeylock.R;
import org.astonbitecode.rustkeylock.api.InterfaceWithRust;
import org.astonbitecode.rustkeylock.api.StringList;
import org.astonbitecode.rustkeylock.handlers.back.BackButtonHandler;
import org.astonbitecode.rustkeylock.utils.Defs;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;

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
            Log.d(TAG, "Clicked Ok in configuration ");
            String url = nextcloudUrlText.getText() != null ? nextcloudUrlText.getText().toString() : "";
            String user = nextcloudUsernameText.getText() != null ? nextcloudUsernameText.getText().toString() : "";
            String password = nextcloudPasswordText.getText() != null ? nextcloudPasswordText.getText().toString() : "";
            String useSelfSignedCertString = new Boolean(useSelfSignedCert.isChecked()).toString();
            Log.d(TAG, "Saving configuration (password not shown here): " + url + ", " + user + ", " + useSelfSignedCertString);

            boolean errorsOccured = false;
            if (url.isEmpty()) {
                nextcloudUrlText.setError("Required field");
                errorsOccured = true;
            }
            if (user.isEmpty()) {
                nextcloudUsernameText.setError("Required field");
                errorsOccured = true;
            }
            if (password.isEmpty()) {
                nextcloudPasswordText.setError("Required field");
                errorsOccured = true;
            }
            if (!errorsOccured) {
                String[] stringsToSave = {url, user, password, useSelfSignedCertString};
                StringList sl = new StringList.ByReference();
                sl.numberOfstrings = 4;
                sl.strings = new StringArray(stringsToSave);
                InterfaceWithRust.INSTANCE.set_configuration(sl);
            }
        } else if (view.getId() == R.id.editConfigurationCancelButton) {
            Log.d(TAG, "Clicked Cancel in configuration ");
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
        InterfaceWithRust.INSTANCE.go_to_menu_plus_arg(Defs.MENU_ENTRIES_LIST, Defs.EMPTY_ARG, "");
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
