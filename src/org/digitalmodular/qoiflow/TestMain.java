package org.digitalmodular.qoiflow;

import java.nio.ByteBuffer;
import java.util.Arrays;

import org.digitalmodular.qoiflow.instruction.QoiInstruction;
import org.digitalmodular.qoiflow.instruction.QoiInstructionChroma;
import org.digitalmodular.qoiflow.instruction.QoiInstructionColorHistory;
import org.digitalmodular.qoiflow.instruction.QoiInstructionDelta;
import org.digitalmodular.qoiflow.instruction.QoiInstructionMaskRGBA;
import org.digitalmodular.qoiflow.instruction.QoiInstructionRGBA;
import org.digitalmodular.qoiflow.instruction.QoiInstructionRunLength;
import org.digitalmodular.util.HexUtilities;

/**
 * @author Mark Jeronimus
 */
// Created 2022-06-05
public class TestMain {
	public static void main(String... args) {
		QoiInstruction rle      = new QoiInstructionRunLength();
		QoiInstruction hist     = new QoiInstructionColorHistory();
		QoiInstruction delta6   = new QoiInstructionDelta(2, 2, 2, 0);
		QoiInstruction delta8   = new QoiInstructionDelta(2, 2, 2, 2);
		QoiInstruction chroma6  = new QoiInstructionChroma(2, 2, 2, 0);
		QoiInstruction chroma8  = new QoiInstructionChroma(2, 2, 2, 2);
		QoiInstruction rgba12   = new QoiInstructionRGBA(4, 4, 4, 0);
		QoiInstruction maskRGB  = new QoiInstructionMaskRGBA(false);
		QoiInstruction maskRGBA = new QoiInstructionMaskRGBA(true);
		QoiInstruction rgba24   = new QoiInstructionRGBA(8, 8, 8, 0);
		QoiInstruction rgba32   = new QoiInstructionRGBA(8, 8, 8, 8);

		QoiFlowCodec enc = new QoiFlowCodec(Arrays.asList(rle, hist, chroma6, chroma8, rgba12, maskRGB, maskRGBA));
		enc.setVariableLength(0, 10);

		QOIEncoderStatistics statistics = new QOIEncoderStatistics();
		enc.setStatistics(statistics);

		ByteBuffer dst = ByteBuffer.allocate(100);

		enc.reset();
		enc.printCodeOffsets();
		enc.encode(new QoiColor(128, 128, 128, 255), dst);
		enc.encode(new QoiColor(255, 128, 255, 255), dst);
		enc.encode(new QoiColor(255, 255, 255, 128), dst);
		enc.encode(new QoiColor(128, 128, 128, 128), dst);
//		enc.encode(new QoiColor(8, 8, 8, 8), dst); // Test Delta
//		enc.encode(new QoiColor(8, 8, 9, 8), dst); // Test Delta
//		enc.encode(new QoiColor(8, 8, 7, 8), dst); // Test Delta
//		enc.encode(new QoiColor(8, 9, 7, 8), dst); // Test Delta
//		enc.encode(new QoiColor(8, 7, 7, 8), dst); // Test Delta
//		enc.encode(new QoiColor(9, 7, 7, 8), dst); // Test Delta
//		enc.encode(new QoiColor(7, 7, 7, 8), dst); // Test Delta
//		enc.encode(new QoiColor(7, 7, 7, 9), dst); // Test Delta
//		enc.encode(new QoiColor(7, 7, 7, 7), dst); // Test Delta
//		enc.encode(new QoiColor(7, 7, 8, 8), dst); // Test Delta
//		enc.encode(new QoiColor(7, 7, 6, 6), dst); // Test Delta
//		enc.encode(new QoiColor(7, 8, 6, 7), dst); // Test Delta
//		enc.encode(new QoiColor(7, 6, 6, 5), dst); // Test Delta
//		enc.encode(new QoiColor(8, 6, 6, 6), dst); // Test Delta
//		enc.encode(new QoiColor(6, 6, 6, 4), dst); // Test Delta
		enc.encode(new QoiColor(8, 8, 8, 8), dst); // Test Chroma
		enc.encode(new QoiColor(9, 8, 8, 8), dst); // Test Chroma
		enc.encode(new QoiColor(7, 8, 8, 8), dst); // Test Chroma
		enc.encode(new QoiColor(7, 8, 9, 8), dst); // Test Chroma
		enc.encode(new QoiColor(7, 8, 7, 8), dst); // Test Chroma
		enc.encode(new QoiColor(8, 9, 8, 8), dst); // Test Chroma
		enc.encode(new QoiColor(6, 7, 6, 8), dst); // Test Chroma
		enc.encode(new QoiColor(6, 7, 6, 9), dst); // Test Chroma
		enc.encode(new QoiColor(6, 7, 6, 7), dst); // Test Chroma
		enc.encode(new QoiColor(7, 7, 6, 8), dst); // Test Chroma
		enc.encode(new QoiColor(5, 7, 6, 6), dst); // Test Chroma
		enc.encode(new QoiColor(5, 7, 7, 7), dst); // Test Chroma
		enc.encode(new QoiColor(5, 7, 5, 5), dst); // Test Chroma
		enc.encode(new QoiColor(6, 8, 6, 6), dst); // Test Chroma
		enc.encode(new QoiColor(4, 6, 4, 4), dst); // Test Chroma

		enc.finishEncoding(dst);

		System.out.println(HexUtilities.hexArrayToString(dst.array(), dst.position()));
	}
}
