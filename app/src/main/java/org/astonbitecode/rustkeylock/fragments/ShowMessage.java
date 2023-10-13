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

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import org.astonbitecode.rustkeylock.R;
import org.astonbitecode.rustkeylock.api.InterfaceWithRust;
import org.astonbitecode.rustkeylock.api.JavaUserOption;
import org.astonbitecode.rustkeylock.api.stubs.JavaMenu;
import org.astonbitecode.rustkeylock.handlers.back.BackButtonHandler;

import java.util.LinkedList;
import java.util.List;

public class ShowMessage extends Fragment implements OnClickListener, BackButtonHandler {
    private static final long serialVersionUID = 163106573370997557L;
    private final String TAG = getClass().getName();
    private String severity;
    private String message;
    private List<JavaUserOption> optionsList;

    @SuppressLint("ValidFragment")
    public ShowMessage(String severity, String message, List<JavaUserOption> optionsList) {
        this.optionsList = optionsList;
        this.message = message;
        this.severity = severity;
    }

    public ShowMessage() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        restore(savedInstanceState);
        View rootView = inflater.inflate(R.layout.fragment_show_message, container, false);
        prepareUiElements(rootView);
        return rootView;
    }

    @Override
    public void onClick(View view) {
        int buttonIndex = -1;
        if (view.getId() == R.id.button1) {
            buttonIndex = 0;
            Log.d(TAG, "Button 1 pressed ");
        } else if (view.getId() == R.id.button2) {
            buttonIndex = 1;
            Log.d(TAG, "Button 2 pressed ");
        } else if (view.getId() == R.id.button3) {
            buttonIndex = 2;
            Log.d(TAG, "Button 3 pressed ");
        }
        if (buttonIndex <= optionsList.size()) {
            JavaUserOption juo = optionsList.get(buttonIndex);
            Log.d(TAG, "ShowMessage returns label " + juo.label + ", value " + juo.value + ", shortLabel " + juo.short_label);
            InterfaceWithRust.INSTANCE.user_option_selected(juo.label, juo.value, juo.short_label);
        } else {
            Log.e(TAG,
                    "A button that does not exist in the User Options offered just got pressed! How did it got here?? Please consider opening a bug to the developers.");
            InterfaceWithRust.INSTANCE.go_to_menu(JavaMenu.Main());
        }
    }

    private void prepareUiElements(View v) {
        ImageView iv = (ImageView) v.findViewById(R.id.messageImage);
        if (severity.equals("Info")) {
            iv.setImageResource(R.drawable.infoimage);
        } else if (severity.equals("Warning")) {
            iv.setImageResource(R.drawable.warningimage);
        } else if (severity.equals("Error")) {
            iv.setImageResource(R.drawable.errorimage);
        } else {
            iv.setVisibility(View.GONE);
        }
        TextView severityTextView = (TextView) v.findViewById(R.id.messageSeverity);
        severityTextView.setText(severity);
        TextView messageTextView = (TextView) v.findViewById(R.id.message);
        messageTextView.setText(message);
        Button b1 = (Button) v.findViewById(R.id.button1);
        Button b2 = (Button) v.findViewById(R.id.button2);
        Button b3 = (Button) v.findViewById(R.id.button3);

        if (optionsList.size() > 0) {
            JavaUserOption juo = optionsList.get(0);
            b1.setOnClickListener(this);
            b1.setText(juo.label);
        } else {
            b1.setVisibility(View.GONE);
        }
        if (optionsList.size() > 1) {
            JavaUserOption juo = optionsList.get(1);
            b2.setOnClickListener(this);
            b2.setText(juo.label);
        } else {
            b2.setVisibility(View.GONE);
        }
        if (optionsList.size() > 2) {
            JavaUserOption juo = optionsList.get(2);
            b3.setOnClickListener(this);
            b3.setText(juo.label);
        } else {
            b3.setVisibility(View.GONE);
        }
    }

    @Override
    public void onBackButton() {
        Log.d(TAG, "Back button pressed");
        InterfaceWithRust.INSTANCE.go_to_menu(JavaMenu.TryPass(false));
    }

    @Override
    public void onSaveInstanceState(Bundle state) {
        state.putString("severity", severity);
        state.putString("message", message);
        if (optionsList.size() > 0) {
            JavaUserOption juo = optionsList.get(0);
            state.putSerializable("javaUserOption1", juo);
        }
        if (optionsList.size() > 1) {
            JavaUserOption juo = optionsList.get(1);
            state.putSerializable("javaUserOption2", juo);
        }
        if (optionsList.size() > 2) {
            JavaUserOption juo = optionsList.get(2);
            state.putSerializable("javaUserOption3", juo);
        }
    }

    private void restore(Bundle state) {
        if (state != null) {
            String severity = state.getString("severity");
            String message = state.getString("message");
            JavaUserOption juo1 = (JavaUserOption) state.getSerializable("javaUserOption1");
            JavaUserOption juo2 = (JavaUserOption) state.getSerializable("javaUserOption2");
            JavaUserOption juo3 = (JavaUserOption) state.getSerializable("javaUserOption3");

            this.severity = severity;
            this.message = message;
            this.optionsList = new LinkedList<>();
            if (juo1 != null) {
                this.optionsList.add(juo1);
            }
            if (juo2 != null) {
                this.optionsList.add(juo2);
            }
            if (juo3 != null) {
                this.optionsList.add(juo3);
            }
        }
    }
}
