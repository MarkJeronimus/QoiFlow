package org.digitalmodular.qoiflow;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.digitalmodular.qoiflow.instruction.QoiInstruction;
import org.digitalmodular.util.HexUtilities;
import static org.digitalmodular.util.Validators.requireRange;
import static org.digitalmodular.util.Validators.requireSizeAtLeast;

/**
 * @author Mark Jeronimus
 */
// Created 2022-06-05
public class QoiFlowCodec {
	public static final QoiColor START_COLOR = new QoiColor(0, 0, 0, 0);

	// Codec configuration
	private final List<QoiInstruction> instructions;
	private final int                  maxInstructionSize;
	private final int                  numFixedCodes;
	private final int[]                variableLengths;

	// Codec state during encoding/decoding (prevent rapid allocation/de-allocation)
	private QoiColor previousColor = START_COLOR;
	private byte     footerCode    = 0;

	// Temporary state (prevent rapid allocation/de-allocation)
	private final byte[] buffer;

	public QoiFlowCodec(Collection<QoiInstruction> instructions) {
		this.instructions = new ArrayList<>(requireSizeAtLeast(2, instructions, "instructions"));

		maxInstructionSize = findMaxInstructionSize(instructions);
		int numVariableInstructions = countNumVariableInstructions(instructions);
		numFixedCodes = countNumFixedCodes(instructions, numVariableInstructions);

		variableLengths = new int[numVariableInstructions - 1];
		Arrays.fill(variableLengths, 1);

		buffer = new byte[maxInstructionSize];
	}

	private static int findMaxInstructionSize(Iterable<QoiInstruction> instructions) {
		int maxInstructionSize = 0;

		for (QoiInstruction instruction : instructions) {
			maxInstructionSize = Math.max(maxInstructionSize, instruction.getMaxSize());
		}

		return maxInstructionSize;
	}

	private static int countNumVariableInstructions(Iterable<QoiInstruction> instructions) {
		int numVariableInstructions = 0;

		for (QoiInstruction instruction : instructions) {
			if (instruction.getNumCodes() == 0) {
				numVariableInstructions++;
			}
		}

		if (numVariableInstructions == 0) {
			throw new IllegalArgumentException("At least one variable-length instruction is required");
		}

		return numVariableInstructions;
	}

	private static int countNumFixedCodes(Iterable<QoiInstruction> instructions, int numVariableInstructions) {
		int numFixedCodes = 0;

		for (QoiInstruction instruction : instructions) {
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

	public Collection<? extends QoiInstruction> instructions() {
		return Collections.unmodifiableList(instructions);
	}

	public void reset() {
		prepareCodeOffsets();

		previousColor = START_COLOR;
		footerCode = findFooterCode(instructions);

		for (QoiInstruction instruction : instructions) {
			instruction.reset();
		}
	}

	private void prepareCodeOffsets() {
		int codeOffset               = 256;
		int variableInstructionIndex = 0;
		for (QoiInstruction instruction : instructions) {
			int numCodes = instruction.getNumCodes();

			if (numCodes == 0) {
				numCodes = getVariableLength(variableInstructionIndex);
				variableInstructionIndex++;
			}

			codeOffset -= numCodes;

			instruction.setCodeOffsetAndCount(codeOffset, numCodes);
		}
	}

	private static byte findFooterCode(Iterable<QoiInstruction> instructions) {
		for (QoiInstruction instruction : instructions) {
			if (!instruction.canRepeatBytes()) {
				return (byte)instruction.getCodeOffset();
			}
		}

		throw new IllegalArgumentException("At least one non-repeatable instruction is required");
	}

	public void printCodeOffsets() {
		int lastCodeOffset = 256;
		for (QoiInstruction instruction : instructions) {
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
	 * by calling their {@link QoiInstruction#setStatistics(QOIEncoderStatistics)}.
	 * <p>
	 * Set to {@code null} to disable statistics.
	 */
	public void setStatistics(QOIEncoderStatistics statistics) {
		int maxNameLength = 1;
		for (QoiInstruction instruction : instructions) {
			instruction.setStatistics(statistics);

			maxNameLength = Math.max(maxNameLength, instruction.toString().length());
		}

		statistics.setMaxInstructionSize(maxInstructionSize);
		statistics.setMaxNameLength(maxNameLength);
	}

	public void encode(QoiColor color, ByteBuffer dst) {
		QoiPixelData pixel = new QoiPixelData(previousColor, color);

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
	private void preEncode(QoiPixelData pixel, ByteBuffer dst) {
		for (QoiInstruction instruction : instructions) {
			instruction.preEncode(pixel, dst);
		}
	}

	private void mainEncode(QoiPixelData pixel, ByteBuffer dst) {
		for (QoiInstruction instruction : instructions) {
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
		for (QoiInstruction instruction : instructions) {
			instruction.postEncode(dst);
		}
	}
}
