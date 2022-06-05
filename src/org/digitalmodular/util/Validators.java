package org.digitalmodular.util;

import java.util.Collection;

/**
 * @author Mark Jeronimus
 */
// Created 2022-06-05
public final class Validators {
	private Validators() {
		throw new AssertionError();
	}

	public static int requireAtLeast(int min, int actual, String varName) {
		if (actual < min) {
			throw new IllegalArgumentException('\'' + varName + "' must be at least " + min + ": " + actual);
		}

		return actual;
	}

	public static int requireRange(int min, int max, int actual, String varName) {
		if (actual < min || actual > max) {
			throw new IllegalArgumentException(
					'\'' + varName + "' must be in the range [" + min + ", " + max + "]: " + actual);
		}

		return actual;
	}

	public static <T> Collection<T> requireSizeAtLeast(int min, Collection<T> actual, String varName) {
		if (actual.size() < min) {
			throw new IllegalArgumentException(
					'\'' + varName + "' must contain at least " + min + " elements: " + actual.size());
		}

		return actual;
	}
}
