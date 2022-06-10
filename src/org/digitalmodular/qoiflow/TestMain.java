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
		QoiInstruction rgba  = new QoiInstructionRGBA(8, 8, 8, 0);
		QoiInstruction hist1 = new QoiInstructionColorHistory();
		QoiInstruction hist2 = new QoiInstructionColorHistory();
		QoiInstruction hist3 = new QoiInstructionColorHistory();
		QoiInstruction hist4 = new QoiInstructionColorHistory();

		QoiFlowCodec enc = new QoiFlowCodec(Arrays.asList(hist1, hist2, rgba, hist3, hist4));
		enc.setVariableLength(0, 6);
		enc.setVariableLength(1, 10);
		enc.setVariableLength(2, 100);

		enc.reset();
	}
}
