package org.digitalmodular.qoiflow;

import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.util.Arrays;

import org.digitalmodular.qoiflow.instruction.QoiInstruction;
import org.digitalmodular.qoiflow.instruction.QoiInstructionChroma;
import org.digitalmodular.qoiflow.instruction.QoiInstructionColorHistory;
import org.digitalmodular.qoiflow.instruction.QoiInstructionMaskRGBA;
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
		QoiInstruction rgba12   = new QoiInstructionRGBA(4, 4, 4, 0);
		QoiInstruction maskRGB  = new QoiInstructionMaskRGBA(false);
		QoiInstruction maskRGBA = new QoiInstructionMaskRGBA(true);
		QoiInstruction rgba24   = new QoiInstructionRGBA(8, 8, 8, 0);
		QoiInstruction rgba32   = new QoiInstructionRGBA(8, 8, 8, 8);

		QoiFlowCodec codec = new QoiFlowCodec(Arrays.asList(rle, hist, rgba32));
		codec.setVariableLength(0, 10);

		QOIEncoderStatistics statistics = new QOIEncoderStatistics();
		codec.setStatistics(statistics);

		BufferedImage image = new BufferedImage(3, 3, BufferedImage.TYPE_3BYTE_BGR);
		ByteBuffer    dst   = new ImageEncoder(codec).encode(image);

		System.out.println(Arrays.toString(Arrays.copyOf(dst.array(), dst.position())));
	}
}
