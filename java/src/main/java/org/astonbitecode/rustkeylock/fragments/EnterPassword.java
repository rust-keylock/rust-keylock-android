package org.astonbitecode.rustkeylock.fragments;

import org.astonbitecode.rustkeylock.R;
import org.astonbitecode.rustkeylock.api.InterfaceWithRust;
import org.astonbitecode.rustkeylock.handlers.back.BackButtonHandler;
import org.astonbitecode.rustkeylock.handlers.state.SaveStateHandler;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

public class EnterPassword extends Fragment implements OnClickListener, BackButtonHandler, SaveStateHandler {
	private static final long serialVersionUID = -9046678064745197531L;
	private final String TAG = getClass().getName();
	private transient EditText passwordText;
	private transient EditText numberText;

	public EnterPassword() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_enter_password, container, false);

		// TODO: Should this be included?
		/*
		ImageView image = (ImageView) rootView.findViewById(R.id.changableImage);
		Calendar cal = Calendar.getInstance();
		int month = cal.get(Calendar.MONTH);
		if (month == Calendar.NOVEMBER || month == Calendar.DECEMBER || month == Calendar.JANUARY) {
			image.setImageResource(R.drawable.santa);
		} else if (month >= Calendar.FEBRUARY && month < Calendar.JUNE) {
			image.setImageResource(R.drawable.flower);
		} else if (month >= Calendar.JUNE && month < Calendar.SEPTEMBER) {
			image.setImageResource(R.drawable.summer);
		} else if (month >= Calendar.SEPTEMBER && month < Calendar.NOVEMBER) {
			image.setImageResource(R.drawable.unmbrella);
		}
		*/
		EditText passwordText = (EditText) rootView.findViewById(R.id.editPassword);
		this.passwordText = passwordText;
		EditText numberText = (EditText) rootView.findViewById(R.id.editFavoriteNumber);
		this.numberText = numberText;
		Button b = (Button) rootView.findViewById(R.id.buttonDecrypt);
		b.setOnClickListener(this);

		return rootView;
	}

	@Override
	public void onClick(View view) {
		String pass = passwordText.getText() != null ? passwordText.getText().toString() : "";
		String numString = numberText.getText() != null ? numberText.getText().toString() : "";

		if (pass.isEmpty()) {
			passwordText.setError("Required Field");
			passwordText.setText("");
		} else if (numString.isEmpty()) {
			numberText.setText("");
			numberText.setError("Required Field");
		} else {
			InterfaceWithRust.INSTANCE.set_password(pass, new Integer(numString));
		}

	}

	@Override
	public void onBackButton() {
		Log.d(TAG, "Back button pressed");
		System.exit(0);
	}

	@Override
	public void onSave(Bundle savedInstanceState) {
		// ignore for now
	}

	@Override
	public void onRestore(Bundle savedInstanceState) {
		// ignore for now
	}
}