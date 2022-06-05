package org.digitalmodular.qoiflow;

import java.util.Arrays;

import org.digitalmodular.qoiflow.instruction.QoiInstruction;
import org.digitalmodular.qoiflow.instruction.QoiInstructionHistory;
import org.digitalmodular.qoiflow.instruction.QoiInstructionRGBA;

/**
 * @author Mark Jeronimus
 */
// Created 2022-06-05
public class TestMain {
	public static void main(String... args) {
		QoiInstruction ins1 = new QoiInstructionHistory(1, 1, 1, 0);
		QoiInstruction ins2 = new QoiInstructionHistory(1, 1, 1, 0);
		QoiInstruction ins3 = new QoiInstructionHistory(1, 1, 1, 0);
		QoiInstruction ins4 = new QoiInstructionHistory(1, 1, 1, 0);

		QoiFlowCodec enc = new QoiFlowCodec(Arrays.asList(ins1, ins2, ins3, ins4));
		System.out.println(enc.getNumVariableCodes());
		System.out.println(enc.getNumVariableLengths());

		enc.setVariableLength(0, 6);
		enc.setVariableLength(1, 10);
		enc.setVariableLength(2, 100);

		enc.setVariableLength(2, 252);
	}
}
