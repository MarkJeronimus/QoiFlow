package org.digitalmodular.qoiflow;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.IOException;
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
	public static void main(String... args) throws IOException {
		QoiInstruction rle      = new QoiInstructionRunLength();
		QoiInstruction hist     = new QoiInstructionColorHistory();
		QoiInstruction delta6   = new QoiInstructionDelta(2, 2, 2, 0);
		QoiInstruction delta8   = new QoiInstructionDelta(2, 2, 2, 2);
		QoiInstruction chroma6  = new QoiInstructionChroma(2, 2, 2, 0);
		QoiInstruction chroma8  = new QoiInstructionChroma(2, 2, 2, 2);
		QoiInstruction maskRGB  = new QoiInstructionMaskRGBA(false);
		QoiInstruction maskRGBA = new QoiInstructionMaskRGBA(true);
		QoiInstruction rgba24   = new QoiInstructionRGBA(8, 8, 8, 0);
		QoiInstruction rgba32   = new QoiInstructionRGBA(8, 8, 8, 8);

		QoiFlowStreamCodec codec = new QoiFlowStreamCodec(Arrays.asList(
				rle, hist, chroma6, chroma8, maskRGB, maskRGBA));
		codec.setVariableLength(0, 10);

		QOIEncoderStatistics statistics = new QOIEncoderStatistics();
		codec.setStatistics(statistics);

		BufferedImage image  = new BufferedImage(792, 10, BufferedImage.TYPE_4BYTE_ABGR);
		byte[]        pixels = ((DataBufferByte)image.getRaster().getDataBuffer()).getData();
		int           p      = 0;
		p = addPixel(pixels, new QoiColor(128, 128, 128, 255), p);
		p = addPixel(pixels, new QoiColor(255, 128, 255, 255), p);
		p = addPixel(pixels, new QoiColor(255, 255, 255, 128), p);
		p = addPixel(pixels, new QoiColor(128, 128, 128, 128), p);
//		p = addPixel(pixels, new QoiColor(8, 8, 8, 8), p); // Test Delta
//		p = addPixel(pixels, new QoiColor(8, 8, 9, 8), p); // Test Delta
//		p = addPixel(pixels, new QoiColor(8, 8, 7, 8), p); // Test Delta
//		p = addPixel(pixels, new QoiColor(8, 9, 7, 8), p); // Test Delta
//		p = addPixel(pixels, new QoiColor(8, 7, 7, 8), p); // Test Delta
//		p = addPixel(pixels, new QoiColor(9, 7, 7, 8), p); // Test Delta
//		p = addPixel(pixels, new QoiColor(7, 7, 7, 8), p); // Test Delta
//		p = addPixel(pixels, new QoiColor(7, 7, 7, 9), p); // Test Delta
//		p = addPixel(pixels, new QoiColor(7, 7, 7, 7), p); // Test Delta
//		p = addPixel(pixels, new QoiColor(7, 7, 8, 8), p); // Test Delta
//		p = addPixel(pixels, new QoiColor(7, 7, 6, 6), p); // Test Delta
//		p = addPixel(pixels, new QoiColor(7, 8, 6, 7), p); // Test Delta
//		p = addPixel(pixels, new QoiColor(7, 6, 6, 5), p); // Test Delta
//		p = addPixel(pixels, new QoiColor(8, 6, 6, 6), p); // Test Delta
//		p = addPixel(pixels, new QoiColor(6, 6, 6, 4), p); // Test Delta
		p = addPixel(pixels, new QoiColor(8, 8, 8, 8), p); // Test Chroma
		p = addPixel(pixels, new QoiColor(9, 8, 8, 8), p); // Test Chroma
		p = addPixel(pixels, new QoiColor(7, 8, 8, 8), p); // Test Chroma
		p = addPixel(pixels, new QoiColor(7, 8, 9, 8), p); // Test Chroma
		p = addPixel(pixels, new QoiColor(7, 8, 7, 8), p); // Test Chroma
		p = addPixel(pixels, new QoiColor(8, 9, 8, 8), p); // Test Chroma
		p = addPixel(pixels, new QoiColor(6, 7, 6, 8), p); // Test Chroma
		p = addPixel(pixels, new QoiColor(6, 7, 6, 9), p); // Test Chroma
		p = addPixel(pixels, new QoiColor(6, 7, 6, 7), p); // Test Chroma
		p = addPixel(pixels, new QoiColor(7, 7, 6, 8), p); // Test Chroma
		p = addPixel(pixels, new QoiColor(5, 7, 6, 6), p); // Test Chroma
		p = addPixel(pixels, new QoiColor(5, 7, 7, 7), p); // Test Chroma
		p = addPixel(pixels, new QoiColor(5, 7, 5, 5), p); // Test Chroma
		p = addPixel(pixels, new QoiColor(6, 8, 6, 6), p); // Test Chroma
		p = addPixel(pixels, new QoiColor(4, 6, 4, 4), p); // Test Chroma

		ByteBuffer dst = new QoiFlowImageEncoder(codec).encode(image);

		System.out.println(HexUtilities.hexArrayToString(dst.array(), dst.position(), 4, 8, 12, -5));

		dst.flip();
		new QoiFlowImageDecoder(codec).decode(dst);
	}

	private static int addPixel(byte[] pixels, QoiColor color, int p) {
		pixels[p++] = (byte)color.a();
		pixels[p++] = (byte)color.b();
		pixels[p++] = (byte)color.g();
		pixels[p++] = (byte)color.r();
		return p;
	}
}
