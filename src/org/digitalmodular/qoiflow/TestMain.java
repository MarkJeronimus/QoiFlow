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
		QoiInstruction diff6  = new QoiInstructionDelta(2, 2, 2, 0);
		QoiInstruction diff8  = new QoiInstructionDelta(2, 2, 2, 2);
		QoiInstruction rgba24 = new QoiInstructionRGBA(8, 8, 8, 0);
		QoiInstruction rgba32 = new QoiInstructionRGBA(8, 8, 8, 8);

		QoiFlowCodec enc = new QoiFlowCodec(Arrays.asList(rle, hist, diff6, diff8, rgba24, rgba32));
		enc.setVariableLength(0, 10);

		QOIEncoderStatistics statistics = new QOIEncoderStatistics();
		enc.setStatistics(statistics);

		ByteBuffer dst = ByteBuffer.allocate(100);

		enc.reset();
		enc.printCodeOffsets();
		enc.encode(new QoiColor(9, 0, 0, 0), dst);
		enc.encode(new QoiColor(9, 9, 1, 1), dst);
		enc.encode(new QoiColor(9, 9, 9, 1), dst);
		enc.encode(new QoiColor(0, 9, 9, 0), dst);
		enc.encode(new QoiColor(0, 0, 9, 0), dst);
		enc.encode(new QoiColor(0, 0, 7, 0), dst);
		enc.encode(new QoiColor(0, 0, 5, 1), dst);
		enc.encode(new QoiColor(0, 0, 3, 1), dst);
		enc.encode(new QoiColor(0, 0, 1, 0), dst);
		enc.encode(new QoiColor(0, 9, 9, 0), dst);
		enc.encode(new QoiColor(0, 0, 7, 0), dst);
		for (int i = 0; i < 110; i++)
			enc.encode(new QoiColor(9, 9, 1, 1), dst);
		for (int i = 0; i < 111; i++)
			enc.encode(new QoiColor(9, 0, 0, 0), dst);

		enc.finishEncoding(dst);
		System.out.println(Arrays.toString(Arrays.copyOf(dst.array(), dst.position())));
	}
}
