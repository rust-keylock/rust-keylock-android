package org.astonbitecode.rustkeylock.handlers.state;

import java.io.Serializable;

import android.os.Bundle;

public interface SaveStateHandler extends Serializable {
	void onSave(Bundle state);

	void onRestore(Bundle state);
}
