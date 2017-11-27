package org.astonbitecode.rustkeylock.api;

import static java.util.Arrays.asList;

import java.io.Serializable;
import java.util.List;

import com.sun.jna.Structure;

public class JavaUserOption extends Structure implements Serializable {

	private static final long serialVersionUID = 8738491202638465205L;

	public static class ByReference extends JavaUserOption implements Structure.ByReference {
	}

	public static class ByValue extends JavaUserOption implements Structure.ByValue {
	}

	public String label;
	public String value;
	public String shortLabel;

	public String getLabel() {
		return label;
	}

	public String getValue() {
		return value;
	}

	public String getShortLabel() {
		return shortLabel;
	}

	@Override
	protected List<String> getFieldOrder() {
		return asList("label", "value", "shortLabel");
	}
}
