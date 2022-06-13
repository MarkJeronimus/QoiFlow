package org.digitalmodular.qoiflow;

import java.nio.ByteBuffer;
import java.util.Arrays;

import org.digitalmodular.qoiflow.instruction.QoiInstruction;
import org.digitalmodular.qoiflow.instruction.QoiInstructionColorHistory;
import org.digitalmodular.qoiflow.instruction.QoiInstructionDelta;
import org.digitalmodular.qoiflow.instruction.QoiInstructionRGBA;
import org.digitalmodular.qoiflow.instruction.QoiInstructionRunLength;

/**
 * @author Mark Jeronimus
 */
// Created 2022-06-05
public class TestMain {
	public static void main(String... args) {
		QoiInstruction rle    = new QoiInstructionRunLength();
		QoiInstruction hist   = new QoiInstructionColorHistory();
		QoiInstruction diff8  = new QoiInstructionDelta(2, 2, 2, 2);
		QoiInstruction diff16 = new QoiInstructionDelta(4, 4, 4, 4);
		QoiInstruction rgba16 = new QoiInstructionRGBA(5, 6, 5, 0);
		QoiInstruction rgba24 = new QoiInstructionRGBA(8, 8, 8, 0);
		QoiInstruction rgba32 = new QoiInstructionRGBA(8, 8, 8, 8);

		QoiFlowCodec enc = new QoiFlowCodec(Arrays.asList(rle, hist, diff8, diff16, rgba16, rgba24, rgba32));
		enc.setVariableLength(0, enc.getNumVariableCodes() >> 1);

		ByteBuffer dst = ByteBuffer.allocate(100);

		enc.reset();
		enc.printCodeOffsets();
		enc.encode(new QoiColor(9, 0, 0, 0), dst);
		enc.encode(new QoiColor(9, 9, 1, 1), dst);
		enc.encode(new QoiColor(9, 9, 9, 1), dst);
		enc.encode(new QoiColor(0, 9, 9, 0), dst);
		enc.encode(new QoiColor(0, 0, 9, 0), dst);
		enc.encode(new QoiColor(0, 0, 9, 0), dst);
		enc.encode(new QoiColor(0, 0, 9, 0), dst);

		enc.finishEncoding(dst);
	}
}
