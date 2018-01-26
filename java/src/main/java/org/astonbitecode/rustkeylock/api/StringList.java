package org.astonbitecode.rustkeylock.api;

import java.util.Arrays;
import java.util.List;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;

public class StringList extends Structure {
	public static class ByReference extends StringList implements Structure.ByReference {
	}

	public static class ByValue extends StringList implements Structure.ByValue {
	}

	public Pointer strings;

	public int numberOfstrings;

	public List<String> getStrings() {
		return Arrays.asList(strings.getStringArray(0, numberOfstrings));
	}

	@Override
	protected List<String> getFieldOrder() {
		return Arrays.asList("strings", "numberOfstrings");
	}
}
