package org.digitalmodular.qoiflow;

import org.digitalmodular.qoiflow.instruction.QoiInstruction;
import org.digitalmodular.util.HexUtilities;
import static org.digitalmodular.util.Validators.requireAtLeast;

/**
 * @author Mark Jeronimus
 */
// Created 2022-06-13
public class QOIEncoderStatistics {
	private int maxInstructionSize = 0;
	private int maxNameLength      = 0;

	public void setMaxInstructionSize(int maxInstructionSize) {
		this.maxInstructionSize = requireAtLeast(1, maxInstructionSize, "maxInstructionSize");
	}

	public void setMaxNameLength(int maxNameLength) {
		this.maxNameLength = requireAtLeast(1, maxNameLength, "maxNameLength");
	}

	public void record(QoiInstruction instruction, byte[] dst, int start, int len, int... parameters) {
		if (maxInstructionSize == 0)
			throw new IllegalStateException("maxInstructionSize has not been set yet!");
		if (maxNameLength == 0)
			throw new IllegalStateException("maxNameLength has not been set yet!");

		StringBuilder sb = new StringBuilder(80);

		appendData(sb, dst, start, len);
		appendName(sb, instruction);
		appendParameters(sb, parameters);

		System.out.println(sb);
	}

	private void appendData(StringBuilder sb, byte[] dst, int start, int len) {
		for (int i = start; i < start + len; i++) {
			sb.append(HexUtilities.hexByteToString(dst[i]));
			sb.append(' ');
		}

		sb.append("   ".repeat(maxInstructionSize - len));
	}

	private void appendName(StringBuilder sb, QoiInstruction instruction) {
		String name = instruction.toString();

		sb.append(name).append(" ".repeat(maxNameLength - name.length()));
	}

	private static void appendParameters(StringBuilder sb, int[] parameters) {
		sb.append('(');

		for (int i = 0; i < parameters.length; i++) {
			if (i > 0) {
				sb.append(", ");
			}

			String s = Integer.toString(parameters[i]);
			sb.append(" ".repeat(4 - s.length())).append(s);
		}

		sb.append(')');
	}
}
