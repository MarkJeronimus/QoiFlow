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
	private final List<QoiInstruction> instructions;
	private final int                  numFixedCodes;
	private final int[]                variableLengths;
	private final int[]                startCodes;

	public QoiFlowCodec(Collection<QoiInstruction> instructions) {
		this.instructions = new ArrayList<>(requireSizeAtLeast(2, instructions, "instructions"));
		startCodes = new int[instructions.size()];

		int numVariableInstructions = 0;
		int numFixedCodes           = 0;
		for (QoiInstruction instruction : instructions) {
			if (instruction.getNumCodes() == 0) {
				numVariableInstructions++;
			}

			numFixedCodes += instruction.getNumCodes();
		}

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
		int codeValue                = 256;
		int variableInstructionIndex = 0;
		for (int i = 0; i < instructions.size(); i++) {
			QoiInstruction instruction = instructions.get(i);

			int numCodes = instruction.getNumCodes();
			if (numCodes > 0) {
				codeValue -= numCodes;
			} else {
				codeValue -= getVariableLength(variableInstructionIndex);
				variableInstructionIndex++;
			}

			startCodes[i] = codeValue;
		}

		System.out.println(Arrays.toString(startCodes));
	}

	public void encode(QoiColor color, ByteBuffer dst) {

	}
}
