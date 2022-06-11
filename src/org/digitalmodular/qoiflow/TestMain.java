package org.digitalmodular.qoiflow;

import java.nio.ByteBuffer;
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
		QoiInstruction rgba16 = new QoiInstructionRGBA(5, 6, 5, 0);
		QoiInstruction hist4  = new QoiInstructionColorHistory();
		QoiInstruction rgba24 = new QoiInstructionRGBA(8, 8, 8, 0);
		QoiInstruction rgba32 = new QoiInstructionRGBA(8, 8, 8, 8);

		QoiFlowCodec enc = new QoiFlowCodec(Arrays.asList(hist4, rgba16, rgba24, rgba32));

		ByteBuffer dst = ByteBuffer.allocate(100);

		enc.reset();
		enc.printCodeOffsets();
		enc.encode(new QoiColor(0xE8, 0xF4, 0xE8, 0), dst);
		enc.encode(new QoiColor(1, 2, 3, 0), dst);
		enc.encode(new QoiColor(1, 2, 3, 0), dst);
		enc.encode(new QoiColor(1, 2, 3, 0), dst);
		enc.encode(new QoiColor(4, 3, 2, 0), dst);
		enc.encode(new QoiColor(4, 3, 2, 0), dst);
		enc.encode(new QoiColor(4, 3, 2, 0), dst);
		enc.encode(new QoiColor(1, 2, 3, 0), dst);
	}
}
