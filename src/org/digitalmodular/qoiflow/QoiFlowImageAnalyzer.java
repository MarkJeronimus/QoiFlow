package org.digitalmodular.qoiflow;

import java.awt.image.BufferedImage;
import java.awt.image.ComponentSampleModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.awt.image.SinglePixelPackedSampleModel;

/**
 * @author Mark Jeronimus
 */
// Created 2022-06-24
public final class QoiFlowImageAnalyzer {
	private QoiFlowImageAnalyzer() {
		throw new AssertionError();
	}

	public static QoiFlowComponentFormat analyze(BufferedImage image) {
		DataBuffer dataBuffer = image.getRaster().getDataBuffer();
		if (dataBuffer instanceof DataBufferByte) {
			return analyzeComponentColorModelImage(image);
		} else if (dataBuffer instanceof DataBufferInt) {
			return analyzeDirectColorModelImage(image);
		} else {
			throw new IllegalArgumentException("Incompatible image: " + image);
		}
	}

	private static QoiFlowComponentFormat analyzeComponentColorModelImage(BufferedImage image) {
		byte[] samples     = ((DataBufferByte)image.getRaster().getDataBuffer()).getData();
		int[]  bandOffsets = ((ComponentSampleModel)image.getRaster().getSampleModel()).getBandOffsets();

		int usedBitsR = 0;
		int usedBitsG = 0;
		int usedBitsB = 0;
		int usedBitsA = 0;

		int p = 0;
		if (bandOffsets.length == 4) {
			while (p < samples.length) {
				usedBitsR |= samples[p + bandOffsets[0]];
				usedBitsG |= samples[p + bandOffsets[1]];
				usedBitsB |= samples[p + bandOffsets[2]];
				usedBitsA |= samples[p + bandOffsets[3]];
				p += 4;
			}
		} else if (bandOffsets.length == 3) {
			while (p < samples.length) {
				usedBitsR |= samples[p + bandOffsets[0]];
				usedBitsG |= samples[p + bandOffsets[1]];
				usedBitsB |= samples[p + bandOffsets[2]];
				p += 3;
			}
		} else {
			throw new IllegalArgumentException("Incompatible image: " + image);
		}

		return new QoiFlowComponentFormat(bitsRequiredFor(usedBitsR),
		                                  bitsRequiredFor(usedBitsG),
		                                  bitsRequiredFor(usedBitsB),
		                                  bitsRequiredFor(usedBitsA));
	}

	private static QoiFlowComponentFormat analyzeDirectColorModelImage(BufferedImage image) {
		int[] pixels     = ((DataBufferInt)image.getRaster().getDataBuffer()).getData();
		int[] bitOffsets = ((SinglePixelPackedSampleModel)image.getSampleModel()).getBitOffsets();

		int usedBitsR = 0;
		int usedBitsG = 0;
		int usedBitsB = 0;
		int usedBitsA = 0;

		if (bitOffsets.length == 4) {
			for (int pixel : pixels) {
				usedBitsR |= pixel >> bitOffsets[0];
				usedBitsG |= pixel >> bitOffsets[1];
				usedBitsB |= pixel >> bitOffsets[2];
				usedBitsA |= pixel >> bitOffsets[3];
			}
		} else if (bitOffsets.length == 3) {
			for (int pixel : pixels) {
				usedBitsR |= pixel >> bitOffsets[0];
				usedBitsG |= pixel >> bitOffsets[1];
				usedBitsB |= pixel >> bitOffsets[2];
			}
		} else {
			throw new IllegalArgumentException("Incompatible image: " + image);
		}

		return new QoiFlowComponentFormat(bitsRequiredFor(usedBitsR),
		                                  bitsRequiredFor(usedBitsG),
		                                  bitsRequiredFor(usedBitsB),
		                                  bitsRequiredFor(usedBitsA));
	}

	private static int bitsRequiredFor(int value) {
		if (value == 0) {
			return 0;
		} else {
			return 8 - Integer.numberOfTrailingZeros(value);
		}
	}
}
