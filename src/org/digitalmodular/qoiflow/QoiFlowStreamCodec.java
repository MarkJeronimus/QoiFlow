package org.digitalmodular.qoiflow;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.digitalmodular.qoiflow.instruction.QoiFlowInstruction;
import org.digitalmodular.util.HexUtilities;
import static org.digitalmodular.util.Validators.requireRange;
import static org.digitalmodular.util.Validators.requireSizeAtLeast;

/**
 * @author Mark Jeronimus
 */
// Created 2022-06-05
public class QoiFlowStreamCodec {
	public static final QoiFlowColor START_COLOR = new QoiFlowColor(0, 0, 0, 0);

	// Codec configuration
	private final List<QoiFlowInstruction> instructions;
	private final int                      maxInstructionSize;
	private final int                      numFixedCodes;
	private final int[]                    variableLengths;

	// Codec state during encoding/decoding (prevent rapid allocation/de-allocation)
	private QoiFlowColor previousColor = START_COLOR;
	private byte         footerCode    = 0;

	// Temporary state (prevent rapid allocation/de-allocation)
	private final byte[] buffer;

	public QoiFlowStreamCodec(Collection<QoiFlowInstruction> instructions) {
		this.instructions = new ArrayList<>(requireSizeAtLeast(2, instructions, "instructions"));

		maxInstructionSize = findMaxInstructionSize(instructions);
		int numVariableInstructions = countNumVariableInstructions(instructions);
		numFixedCodes = countNumFixedCodes(instructions, numVariableInstructions);

		variableLengths = new int[numVariableInstructions - 1];
		Arrays.fill(variableLengths, 1);

		buffer = new byte[maxInstructionSize];
	}

	private static int findMaxInstructionSize(Iterable<QoiFlowInstruction> instructions) {
		int maxInstructionSize = 0;

		for (QoiFlowInstruction instruction : instructions) {
			maxInstructionSize = Math.max(maxInstructionSize, instruction.getMaxSize());
		}

		return maxInstructionSize;
	}

	private static int countNumVariableInstructions(Iterable<QoiFlowInstruction> instructions) {
		int numVariableInstructions = 0;

		for (QoiFlowInstruction instruction : instructions) {
			if (instruction.getNumCodes() == 0) {
				numVariableInstructions++;
			}
		}

		if (numVariableInstructions == 0) {
			throw new IllegalArgumentException("At least one variable-length instruction is required");
		}

		return numVariableInstructions;
	}

	private static int countNumFixedCodes(Iterable<QoiFlowInstruction> instructions, int numVariableInstructions) {
		int numFixedCodes = 0;

		for (QoiFlowInstruction instruction : instructions) {
			numFixedCodes += instruction.getNumCodes();
		}

		if (numFixedCodes + numVariableInstructions > 256) {
			throw new IllegalArgumentException(
					"Code space overflow: " + (numFixedCodes + numVariableInstructions) + " > 256");
		}

		return numFixedCodes;
	}

	public int getMaxInstructionSize() {
		return maxInstructionSize;
	}

	public int getNumVariableCodes() {
		return 256 - numFixedCodes;
	}

	public byte getFooterCode() {
		return footerCode;
	}

	public int getNumVariableLengths() {
		return variableLengths.length;
	}

	public void setVariableLength(int index, int variableLength) {
		requireRange(0, variableLengths.length - 1, index, "index");
		int maximumLength = 256 - numFixedCodes - variableLengths.length;
		requireRange(1, maximumLength, variableLength, "variableLength");

		variableLengths[index] = variableLength;

		int remaining = 256 - numFixedCodes;
		for (int length : variableLengths) {
			remaining -= length;
		}

		if (remaining < 1) {
			// From the last to the first, decrease lengths until the code space doesn't overflow anymore.
			//
			// Example, in case of 4 variable length instructions, which have these lengths assigned:
			// [6, 10, 100] (140 = fourth length, not explicitly stored)
			// When assigning 252 to index 2:
			// [6, 10, 252] (-12 remaining, still < 1)
			// [6,  1, 252] (-3 remaining, still < 1)
			// [2,  1, 252] (1) = Final resulting lengths.
			remaining = 1 - remaining;
			for (int i = variableLengths.length - 1; i >= 0; i--) {
				if (i == index) {
					continue;
				}

				int amount = Math.min(remaining, variableLengths[i] - 1);
				variableLengths[i] -= amount;
				remaining -= amount;
				if (remaining == 0)
					break;
			}
		}
	}

	public int getVariableLength(int index) {
		requireRange(0, variableLengths.length, index, "index");

		if (index < variableLengths.length) {
			return variableLengths[index];
		} else {
			int remaining = 256 - numFixedCodes;
			for (int length : variableLengths) {
				remaining -= length;
			}

			return remaining;
		}
	}

	public Collection<? extends QoiFlowInstruction> instructions() {
		return Collections.unmodifiableList(instructions);
	}

	public void reset() {
		prepareCodeOffsets();

		previousColor = START_COLOR;
		footerCode = findFooterCode(instructions);

		for (QoiFlowInstruction instruction : instructions) {
			instruction.reset();
		}
	}

	private void prepareCodeOffsets() {
		int codeOffset               = 256;
		int variableInstructionIndex = 0;
		for (QoiFlowInstruction instruction : instructions) {
			int numCodes = instruction.getNumCodes();

			if (numCodes == 0) {
				numCodes = getVariableLength(variableInstructionIndex);
				variableInstructionIndex++;
			}

			codeOffset -= numCodes;

			instruction.setCodeOffsetAndCount(codeOffset, numCodes);
		}
	}

	private static byte findFooterCode(Iterable<QoiFlowInstruction> instructions) {
		for (QoiFlowInstruction instruction : instructions) {
			if (!instruction.canRepeatBytes()) {
				return (byte)instruction.getCodeOffset();
			}
		}

		throw new IllegalArgumentException("At least one non-repeatable instruction is required");
	}

	public void printCodeOffsets() {
		int lastCodeOffset = 256;
		for (QoiFlowInstruction instruction : instructions) {
			int codeOffset = instruction.getCodeOffset();
			int numCodes   = instruction.getCalculatedCodeCount();
			if (lastCodeOffset - 1 == codeOffset) {
				System.out.println(instruction + ": 0x" + HexUtilities.hexByteToString(codeOffset) +
				                   " (" + numCodes + ')');
			} else {
				System.out.println(instruction + ": 0x" + HexUtilities.hexByteToString(codeOffset)
				                   + "..0x" + HexUtilities.hexByteToString(lastCodeOffset - 1) + " (" + numCodes + ')');
			}
			lastCodeOffset = codeOffset;
		}
	}

	/**
	 * Sets or clears the object to track statistics with.
	 * <p>
	 * This method gives the statistics object to all instructions contained within this encoder,
	 * by calling their {@link QoiFlowInstruction#setStatistics(QoiFlowStatistics)}.
	 * <p>
	 * Set to {@code null} to disable statistics.
	 */
	public void setStatistics(QoiFlowStatistics statistics) {
		int maxNameLength = 1;
		for (QoiFlowInstruction instruction : instructions) {
			instruction.setStatistics(statistics);

			maxNameLength = Math.max(maxNameLength, instruction.toString().length());
		}

		statistics.setMaxInstructionSize(maxInstructionSize);
		statistics.setMaxNameLength(maxNameLength);
	}

	public QoiFlowStatistics getStatistics() {
		return instructions.get(0).getStatistics();
	}

	public void encode(QoiFlowColor color, ByteBuffer dst) {
		QoiFlowPixelData pixel = new QoiFlowPixelData(previousColor, color);

		preEncode(pixel, dst);
		mainEncode(pixel, dst);

		previousColor = color;
	}

	/**
	 * Give instructions the opportunity to emit deferred data based on the new pixel, before actual encoding begins.
	 * <p>
	 * This is required, for example, for RLE, to emit instructions when the color is no longer equal to the previous.
	 * <p>
	 * Does nothing unless overridden.
	 */
	private void preEncode(QoiFlowPixelData pixel, ByteBuffer dst) {
		for (QoiFlowInstruction instruction : instructions) {
			instruction.preEncode(pixel, dst);
		}
	}

	private void mainEncode(QoiFlowPixelData pixel, ByteBuffer dst) {
		for (QoiFlowInstruction instruction : instructions) {
			int numBytes = instruction.encode(pixel, buffer);
			if (numBytes >= 0) {
				dst.put(buffer, 0, numBytes);
				return;
			}
		}

		throw new AssertionError("None of the instructions could encode: " + pixel);
	}

	/**
	 * Give instructions the opportunity to emit deferred data at the end of the stream.
	 * <p>
	 * This marks the end of an encoding cycle.
	 * Continuing to use this encoder without first calling {@link #reset()} results in unspecified behavior.
	 * <p>
	 * This is required, for example, for RLE, to emit instructions when the counter is {@code > 1}.
	 */
	public void finishEncoding(ByteBuffer dst) {
		for (QoiFlowInstruction instruction : instructions) {
			instruction.postEncode(dst);
		}
	}

	public QoiFlowColorRun decode(int code, ByteBuffer src, QoiFlowColor lastColor) {
		for (QoiFlowInstruction instruction : instructions) {
			if (code >= instruction.getCodeOffset()) {
				return instruction.decode(code, src, lastColor);
			}
		}

		throw new AssertionError("None of the instructions could decode: " + HexUtilities.hexByteToString(code));
	}

	/**
	 * Give instructions the opportunity to update their internal state depending on the decoded color.
	 * <p>
	 * This is required, for example, for Color History, to record a color not decoded by itself.
	 */
	public void postDecode(QoiFlowColor color) {
		for (QoiFlowInstruction instruction : instructions) {
			instruction.postDecode(color);
		}
	}
}
