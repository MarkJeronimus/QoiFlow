package org.digitalmodular.qoiflow;

import java.nio.ByteBuffer;

import org.digitalmodular.qoiflow.instruction.QoiInstruction;
import org.digitalmodular.util.HexUtilities;
import static org.digitalmodular.util.Validators.requireAtLeast;

/**
 * @author Mark Jeronimus
 */
// Created 2022-06-13
@SuppressWarnings("FieldHasSetterButNoGetter")
public class QOIEncoderStatistics {
	private int maxInstructionSize = 0;
	private int maxNameLength      = 0;

	public void setMaxInstructionSize(int maxInstructionSize) {
		this.maxInstructionSize = requireAtLeast(1, maxInstructionSize, "maxInstructionSize");
	}

	public void setMaxNameLength(int maxNameLength) {
		this.maxNameLength = requireAtLeast(1, maxNameLength, "maxNameLength");
	}

	public void record(QoiInstruction instruction, ByteBuffer src, int len, QoiColor color, int... parameters) {
		record(instruction, src.array(), src.position() - len, len, color, parameters);
	}

	public void record(QoiInstruction instruction, byte[] dst, int start, int len, QoiColor color, int... parameters) {
		if (maxInstructionSize == 0)
			throw new IllegalStateException("maxInstructionSize has not been set yet!");
		if (maxNameLength == 0)
			throw new IllegalStateException("maxNameLength has not been set yet!");

		StringBuilder sb = new StringBuilder(80);

		appendData(sb, dst, start, len);
		appendName(sb, instruction);
		sb.append('(');
		appendParameters(sb, parameters);
		appendTabs(sb, maxInstructionSize - parameters.length);
		sb.append(") = ");
		appendColor(sb, color);

		System.out.println(sb);
	}

	public void recordMask(
			QoiInstruction instruction, byte[] dst, int start, int len, int mask, QoiColor color, int... parameters) {
		if (maxInstructionSize == 0)
			throw new IllegalStateException("maxInstructionSize has not been set yet!");
		if (maxNameLength == 0)
			throw new IllegalStateException("maxNameLength has not been set yet!");

		StringBuilder sb = new StringBuilder(80);

		appendData(sb, dst, start, len);
		appendName(sb, instruction);
		sb.append('(');
		appendMask(sb, mask, instruction.hasAlpha());
		sb.append(", ");
		appendParameters(sb, parameters);
		appendTabs(sb, maxInstructionSize - parameters.length - 1);
		sb.append(") = ");
		appendColor(sb, color);

		System.out.println(sb);
	}

	private void appendData(StringBuilder sb, byte[] dst, int start, int len) {
		for (int i = start; i < start + len; i++) {
			sb.append(HexUtilities.hexByteToString(dst[i]));
			sb.append(' ');
		}

		sb.append("   ".repeat(Math.max(0, maxInstructionSize - len)));
	}

	private void appendName(StringBuilder sb, QoiInstruction instruction) {
		String name = instruction.toString();

		sb.append(name).append(" ".repeat(maxNameLength - name.length()));
	}

	private static void appendMask(StringBuilder sb, int mask, boolean hasAlpha) {
		sb.append((mask & 0b1000) != 0 ? 'R' : '·');
		sb.append((mask & 0b0100) != 0 ? 'G' : '·');
		sb.append((mask & 0b0010) != 0 ? 'B' : '·');

		if (hasAlpha) {
			sb.append((mask & 0b0001) != 0 ? 'A' : '·');
		} else {
			sb.append(' ');
		}
	}

	private static void appendParameters(StringBuilder sb, int[] parameters) {
		for (int i = 0; i < parameters.length; i++) {
			if (i > 0) {
				sb.append(", ");
			}

			String s = Integer.toString(parameters[i]);
			sb.append(" ".repeat(Math.max(0, 4 - s.length()))).append(s);
		}
	}

	private static void appendTabs(StringBuilder sb, int n) {
		sb.append("      ".repeat(Math.max(0, n)));
	}

	private static void appendColor(StringBuilder sb, QoiColor color) {
		sb.append(String.format("QoiCOLOR(%3d, %3d, %3d, %3d)", color.r(), color.g(), color.b(), color.a()));
	}
}
