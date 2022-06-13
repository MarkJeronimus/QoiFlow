package org.digitalmodular.util;

/**
 * @author Mark Jeronimus
 */
// Created 2022-06-13
public final class HexUtilities {
	private HexUtilities() {
		throw new AssertionError();
	}

	public static final char[] DIGITS =
			{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

	public static String hexByteToString(int i) {
		return new String(new char[]{DIGITS[(i >>> 4) & 0xF], DIGITS[i & 0xF]});
	}
}
