package org.digitalmodular.qoiflow;

import java.util.Arrays;

import org.digitalmodular.qoiflow.instruction.QoiInstruction;
import org.digitalmodular.qoiflow.instruction.QoiInstructionColorHistory;
import org.digitalmodular.qoiflow.instruction.QoiInstructionRGBA;

/**
 * @author Mark Jeronimus
 */
// Created 2022-06-05
public class TestMain {
	public static void main(String... args) {
		QoiInstruction hist4  = new QoiInstructionColorHistory();
		QoiInstruction rgba24 = new QoiInstructionRGBA(8, 8, 8, 0);
		QoiInstruction rgba32 = new QoiInstructionRGBA(8, 8, 8, 8);

		QoiFlowCodec enc = new QoiFlowCodec(Arrays.asList(hist4, rgba24, rgba32));

		enc.reset();
		enc.printCodeOffsets();
	}
}
