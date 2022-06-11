package org.digitalmodular.qoiflow;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.digitalmodular.qoiflow.instruction.QoiInstruction;
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

	// Codec state
	private QoiColor previousColor = START_COLOR;

	// Temporary state (prevent rapid allocation/de-allocation)
	private final byte[] buffer;

	public QoiFlowCodec(Collection<QoiInstruction> instructions) {
		this.instructions = new ArrayList<>(requireSizeAtLeast(2, instructions, "instructions"));

		int maxInstructionSize      = 0;
		int numVariableInstructions = 0;
		int numFixedCodes           = 0;
		for (QoiInstruction instruction : instructions) {
			maxInstructionSize = Math.max(maxInstructionSize, instruction.getMaxSize());

			if (instruction.getNumCodes() == 0) {
				numVariableInstructions++;
			}

			numFixedCodes += instruction.getNumCodes();
		}

		this.maxInstructionSize = maxInstructionSize;
		buffer = new byte[maxInstructionSize];

		if (numVariableInstructions == 0) {
			throw new IllegalArgumentException("At least one variable-length instruction is required");
		} else if (numFixedCodes + numVariableInstructions > 256) {
			throw new IllegalArgumentException(
					"Code space overflow: " + (numFixedCodes + numVariableInstructions) + " > 256");
		}

		this.numFixedCodes = numFixedCodes;
		variableLengths = new int[numVariableInstructions - 1];
		Arrays.fill(variableLengths, 1);
	}

	public int getMaxInstructionSize() {
		return maxInstructionSize;
	}

	public int getNumVariableCodes() {
		return 256 - numFixedCodes;
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

	public void reset() {
		prepareCodeOffsets();

		previousColor = START_COLOR;

		for (QoiInstruction instruction : instructions) {
			instruction.reset();
		}
	}

	private void prepareCodeOffsets() {
		int codeOffset               = 256;
		int variableInstructionIndex = 0;
		for (QoiInstruction instruction : instructions) {
			int numCodes = instruction.getNumCodes();

			if (numCodes > 0) {
				codeOffset -= numCodes;
			} else {
				codeOffset -= getVariableLength(variableInstructionIndex);
				variableInstructionIndex++;
			}

			instruction.setCodeOffset(codeOffset);
		}
	}

	public void printCodeOffsets() {
		int lastCodeOffset = 256;
		for (QoiInstruction instruction : instructions) {
			int codeOffset = instruction.getCodeOffset();
			if (lastCodeOffset - 1 == codeOffset) {
				System.out.println(instruction + ": " + codeOffset);
			} else {
				System.out.println(instruction + ": " + (lastCodeOffset - 1) + ".." + codeOffset);
			}
			lastCodeOffset = codeOffset;
		}
	}

	public void encode(QoiColor color, ByteBuffer dst) {
		QoiPixelData pixel = new QoiPixelData(previousColor, color);

		boolean encoded = false;

		for (QoiInstruction instruction : instructions) {
			int numBytes = instruction.encode(pixel, buffer);
			if (numBytes > 0) {
				dst.put(buffer, 0, numBytes);
				encoded = true;
				break;
			}
		}

		if (!encoded) {
			throw new AssertionError("None of the instructions could encode: " + pixel);
		}

		previousColor = color;
		System.out.println(Arrays.toString(Arrays.copyOf(dst.array(), dst.position())));
	}
}
