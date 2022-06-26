package org.digitalmodular.qoiflow;

import java.nio.ByteBuffer;
import java.util.Map;
import java.util.TreeMap;

import org.digitalmodular.qoiflow.instruction.QoiFlowInstruction;
import org.digitalmodular.util.HexUtilities;
import static org.digitalmodular.util.Validators.requireAtLeast;

/**
 * @author Mark Jeronimus
 */
// Created 2022-06-13
@SuppressWarnings("FieldHasSetterButNoGetter")
public class QoiFlowStatistics {
	private int     maxInstructionSize         = 0;
	private int     maxNameLength              = 0;
	private boolean dumpIndividualInstructions = false;

	/** Instruction name -> single-element array */
	private final Map<String, int[]> instructionCounts = new TreeMap<>();

	public void setMaxInstructionSize(int maxInstructionSize) {
		this.maxInstructionSize = requireAtLeast(1, maxInstructionSize, "maxInstructionSize");
	}

	public void setMaxNameLength(int maxNameLength) {
		this.maxNameLength = requireAtLeast(1, maxNameLength, "maxNameLength");
	}

	public boolean isDumpIndividualInstructions() {
		return dumpIndividualInstructions;
	}

	public void setDumpIndividualInstructions(boolean dumpIndividualInstructions) {
		this.dumpIndividualInstructions = dumpIndividualInstructions;
	}

	public void add(QoiFlowStatistics other) {
		maxInstructionSize = other.maxInstructionSize;
		maxNameLength = other.maxNameLength;

		for (Map.Entry<String, int[]> entry : other.instructionCounts.entrySet()) {
			int[] count = instructionCounts.computeIfAbsent(entry.getKey(), ignored -> new int[1]);
			count[0] += entry.getValue()[0];
		}
	}

	public void reset() {
		instructionCounts.clear();
	}

	public void record(QoiFlowInstruction instruction, ByteBuffer src, int len, QoiFlowColor color, int... parameters) {
		record(instruction, src.array(), src.position() - len, len, color, parameters);
	}

	public void record(
			QoiFlowInstruction instruction, byte[] dst, int start, int len, QoiFlowColor color, int... parameters) {
		if (maxInstructionSize == 0)
			throw new IllegalStateException("maxInstructionSize has not been set yet!");
		if (maxNameLength == 0)
			throw new IllegalStateException("maxNameLength has not been set yet!");

		String instructionName = instruction.toString();
		countInstruction(instructionName);

		if (!dumpIndividualInstructions) {
			return;
		}

		StringBuilder sb = new StringBuilder(80);

		appendData(sb, dst, start, len);
		appendName(sb, instructionName);
		sb.append('(');
		appendParameters(sb, parameters);
		appendTabs(sb, maxInstructionSize - parameters.length);
		sb.append(") = ");
		appendColor(sb, color);

		System.out.println(sb);
	}

	public void recordMask(QoiFlowInstruction instruction,
	                       byte[] dst,
	                       int start,
	                       int len,
	                       int mask,
	                       QoiFlowColor color,
	                       int... parameters) {
		if (maxInstructionSize == 0)
			throw new IllegalStateException("maxInstructionSize has not been set yet!");
		if (maxNameLength == 0)
			throw new IllegalStateException("maxNameLength has not been set yet!");

		String instructionName = instruction.toString();
		countInstruction(instructionName);

		if (!dumpIndividualInstructions) {
			return;
		}

		StringBuilder sb = new StringBuilder(80);

		appendData(sb, dst, start, len);
		appendName(sb, instructionName);
		sb.append('(');
		appendMask(sb, mask, instruction.hasAlpha());
		sb.append(", ");
		appendParameters(sb, parameters);
		appendTabs(sb, maxInstructionSize - parameters.length - 1);
		sb.append(") = ");
		appendColor(sb, color);

		System.out.println(sb);
	}

	public int getInstructionCount(String instructionName) {
		int[] count = instructionCounts.get(instructionName);
		if (count == null) {
			return 0;
		} else {
			return count[0];
		}
	}

	public void dumpCounts() {
		StringBuilder sb = new StringBuilder(1000);

		for (Map.Entry<String, int[]> entry : instructionCounts.entrySet()) {
			sb.append('#');
			appendName(sb, entry.getKey());
			sb.append(": ");
			sb.append(entry.getValue()[0]);
			sb.append('\n');
		}

		sb.setLength(sb.length() - 1);

		System.out.println(sb);
	}

	private void countInstruction(String instructionName) {
		int[] count = instructionCounts.computeIfAbsent(instructionName, ignored -> new int[1]);
		count[0]++;
	}

	private void appendData(StringBuilder sb, byte[] dst, int start, int len) {
		for (int i = start; i < start + len; i++) {
			sb.append(HexUtilities.hexByteToString(dst[i]));
			sb.append(' ');
		}

		sb.append("   ".repeat(Math.max(0, maxInstructionSize - len)));
	}

	private void appendName(StringBuilder sb, String instructionName) {
		sb.append(instructionName).append(" ".repeat(maxNameLength - instructionName.length()));
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

	private static void appendColor(StringBuilder sb, QoiFlowColor color) {
		sb.append(String.format("QoiCOLOR(%3d, %3d, %3d, %3d)", color.r(), color.g(), color.b(), color.a()));
	}
}
