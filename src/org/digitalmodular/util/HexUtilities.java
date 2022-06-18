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

	/**
	 * Converts an array to a sequence of hexadecimal octets, optionally separated at the indicated markers.
	 * <p>
	 * The markers can be relative to the beginning or the end (when negative).
	 * For example:<br>
	 * {@code hexArrayToString(new byte[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9}, 10, 2, -3)}<br>
	 * returns:<br>
	 * {@code "00 01 | 02 03 04 05 06 | 07 08 09"}
	 * </pre>
	 *
	 * @param markers A list of markers, ordered in printing sequence (not sorted numerically)
	 */
	public static String hexArrayToString(byte[] array, int length, int... markers) {
		StringBuilder sb          = new StringBuilder(length * 3 + markers.length * 2 - 1);
		int           markerIndex = 0;

		for (int i = 0; i < length; i++) {
			if (i > 0) {
				sb.append(' ');
			}

			if (markerIndex < markers.length) {
				if (i == markers[markerIndex] || length - i == -markers[markerIndex]) {
					sb.append("| ");
					markerIndex++;
				}
			}

			sb.append(hexByteToString(array[i]));
		}

		return sb.toString();
	}
}
