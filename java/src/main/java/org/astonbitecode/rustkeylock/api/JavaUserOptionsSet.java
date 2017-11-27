package org.astonbitecode.rustkeylock.api;

import java.util.Arrays;
import java.util.List;

import com.sun.jna.Structure;

public class JavaUserOptionsSet extends Structure {

	public static class ByReference extends JavaUserOptionsSet implements Structure.ByReference {
	}

	public static class ByValue extends JavaUserOptionsSet implements Structure.ByValue {
	}

	public JavaUserOption.ByReference options;

	public int numberOfOptions;

	public List<JavaUserOption> getOptions() {
		JavaUserOption[] array = (JavaUserOption[]) options.toArray(numberOfOptions);
		return Arrays.asList(array);
	}

	@Override
	protected List<String> getFieldOrder() {
		return Arrays.asList("options", "numberOfOptions");
	}
}