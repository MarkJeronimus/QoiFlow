package org.digitalmodular.qoiflow;

import java.nio.ByteBuffer;
import java.util.Arrays;

import org.digitalmodular.qoiflow.instruction.QoiInstruction;
import org.digitalmodular.qoiflow.instruction.QoiInstructionChroma;
import org.digitalmodular.qoiflow.instruction.QoiInstructionColorHistory;
import org.digitalmodular.qoiflow.instruction.QoiInstructionRGBA;
import org.digitalmodular.qoiflow.instruction.QoiInstructionRunLength;

/**
 * @author Mark Jeronimus
 */
// Created 2022-06-05
public class TestMain {
	public static void main(String... args) {
		QoiInstruction rle      = new QoiInstructionRunLength();
		QoiInstruction hist     = new QoiInstructionColorHistory();
		QoiInstruction chroma6  = new QoiInstructionChroma(2, 2, 2, 0);
		QoiInstruction chroma12 = new QoiInstructionChroma(3, 3, 3, 3);
		QoiInstruction rgba24   = new QoiInstructionRGBA(8, 8, 8, 0);
		QoiInstruction rgba32   = new QoiInstructionRGBA(8, 8, 8, 8);

		QoiFlowCodec enc = new QoiFlowCodec(Arrays.asList(rle, hist, chroma6, chroma12, rgba24, rgba32));
		enc.setVariableLength(0, 10);

		QOIEncoderStatistics statistics = new QOIEncoderStatistics();
		enc.setStatistics(statistics);

		ByteBuffer dst = ByteBuffer.allocate(100);

		enc.reset();
		enc.printCodeOffsets();
		enc.encode(new QoiColor(2, 2, 2, 0), dst);
		enc.encode(new QoiColor(3, 2, 2, 0), dst);
		enc.encode(new QoiColor(3, 3, 2, 0), dst);
		enc.encode(new QoiColor(3, 3, 3, 0), dst);
		enc.encode(new QoiColor(6, 4, 6, 0), dst);
		enc.encode(new QoiColor(7, 5, 7, 0), dst);

		enc.finishEncoding(dst);
		System.out.println(Arrays.toString(Arrays.copyOf(dst.array(), dst.position())));
	}
}
