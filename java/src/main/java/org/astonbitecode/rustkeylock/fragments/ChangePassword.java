package org.astonbitecode.rustkeylock.fragments;

import org.astonbitecode.rustkeylock.R;
import org.astonbitecode.rustkeylock.api.InterfaceWithRust;
import org.astonbitecode.rustkeylock.handlers.back.BackButtonHandler;
import org.astonbitecode.rustkeylock.handlers.state.SaveStateHandler;
import org.astonbitecode.rustkeylock.utils.Defs;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

public class ChangePassword extends Fragment implements OnClickListener, BackButtonHandler, SaveStateHandler {
	private static final long serialVersionUID = 8235249433565909373L;
	private final String TAG = getClass().getName();
	private transient EditText passwordText1;
	private transient EditText numberText1;
	private transient EditText passwordText2;
	private transient EditText numberText2;

	public ChangePassword() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_change_password, container, false);

		EditText passwordText1 = (EditText) rootView.findViewById(R.id.editPasswordChangePassword1);
		this.passwordText1 = passwordText1;
		EditText numberText1 = (EditText) rootView.findViewById(R.id.editFavoriteNumberChangePassword1);
		this.numberText1 = numberText1;
		EditText passwordText2 = (EditText) rootView.findViewById(R.id.editPasswordChangePassword2);
		this.passwordText2 = passwordText2;
		EditText numberText2 = (EditText) rootView.findViewById(R.id.editFavoriteNumberChangePassword2);
		this.numberText2 = numberText2;

		Button b = (Button) rootView.findViewById(R.id.buttonApplyChanges);
		b.setOnClickListener(this);

		return rootView;
	}

	@Override
	public void onClick(View view) {
		if (!passwordText1.getText().toString().equals(passwordText2.getText().toString())) {
			passwordText2.setError("The provided passwords did not match");
		} else if (!numberText1.getText().toString().equals(numberText2.getText().toString())) {
			numberText2.setError("The provided favorite numbers did not match");
		} else {
			if (passwordText1.getText().toString().isEmpty()) {
				passwordText1.setError("This Field cannot be empty");
				passwordText1.setText("");
			} else if (numberText1.getText().toString().isEmpty()) {
				numberText1.setText("");
				numberText1.setError("This Field cannot be empty");
			} else {
				String pass = passwordText1.getText() != null ? passwordText1.getText().toString() : "";
				int num = numberText1.getText() != null ? new Integer(numberText1.getText().toString()) : 0;
				InterfaceWithRust.INSTANCE.set_password(pass, num);
			}
		}
	}

	@Override
	public void onBackButton() {
		Log.d(TAG, "Back button pressed");
		InterfaceWithRust.INSTANCE.go_to_menu(Defs.MENU_MAIN);
	}

	@Override
	public void onSave(Bundle state) {
		state.putString("passwordText1", passwordText1.getText().toString());
		state.putString("passwordText2", passwordText2.getText().toString());
		state.putString("numberText1", numberText1.getText().toString());
		state.putString("numberText2", numberText2.getText().toString());
	}

	@Override
	public void onRestore(Bundle state) {
		passwordText1.setText(state.getString("passwordText1"));
		passwordText2.setText(state.getString("passwordText2"));
		numberText1.setText(state.getString("numberText1"));
		numberText2.setText(state.getString("numberText2"));
	}
}