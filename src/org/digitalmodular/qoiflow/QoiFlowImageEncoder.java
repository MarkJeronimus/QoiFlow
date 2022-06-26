package org.digitalmodular.qoiflow;

import java.awt.image.BufferedImage;
import java.awt.image.ComponentSampleModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.awt.image.SinglePixelPackedSampleModel;
import java.nio.ByteBuffer;
import java.util.Objects;

/**
 * @author Mark Jeronimus
 */
// Created 2022-05-16
// Changed 2022-06-18 Copied from FluidQOI
public class QoiFlowImageEncoder {
	@SuppressWarnings("CharUsedInArithmeticContext")
	public static final int QOIF_MAGIC    = 'Q' << 24 |
	                                        'O' << 16 |
	                                        'I' << 8 |
	                                        'F';
	public static final int HEADER_LENGTH = 32;

	private final QoiFlowStreamCodec codec;

	public QoiFlowImageEncoder(QoiFlowStreamCodec codec) {
		this.codec = Objects.requireNonNull(codec, "'codec' can't be null");
	}

	/**
	 * @return A new ByteBuffer, backed by a heap array (with royally overestimated capacity).
	 */
	public ByteBuffer encode(BufferedImage image) {
		Objects.requireNonNull(image, "image");

		image = QoiFlowUtilities.asCompatibleImage(image);

		QoiComponentFormat componentFormat = QoiFlowImageAnalyzer.analyze(image);
		System.out.println(componentFormat);

		int width  = image.getWidth();
		int height = image.getHeight();

		ByteBuffer dst = ByteBuffer.allocate(HEADER_LENGTH + width * height * codec.getMaxInstructionSize());

		codec.reset();

		writeHeader(width, height, componentFormat, dst);
		encodeImage(image, dst);
		codec.finishEncoding(dst);
		writeFooter(dst);

		return dst;
	}

	private static void writeHeader(int width, int height, QoiComponentFormat componentFormat, ByteBuffer dst) {
		dst.putInt(QOIF_MAGIC);
		dst.putInt(width);
		dst.putInt(height);
		dst.put((byte)(componentFormat.bitsR() << 4 | componentFormat.bitsG()));
		dst.put((byte)(componentFormat.bitsB() << 4 | componentFormat.bitsA()));

		// TODO write instruction table
	}

	private void encodeImage(BufferedImage image, ByteBuffer dst) {
		DataBuffer dataBuffer = image.getRaster().getDataBuffer();
		if (dataBuffer instanceof DataBufferByte) {
			encodeComponentColorModelImage(image, dst);
		} else if (dataBuffer instanceof DataBufferInt) {
			encodeDirectColorModelImage(image, dst);
		} else {
			throw new AssertionError("Analyzer returned invalid image: " + image);
		}
	}

	private void encodeComponentColorModelImage(BufferedImage image, ByteBuffer dst) {
		byte[] samples     = ((DataBufferByte)image.getRaster().getDataBuffer()).getData();
		int[]  bandOffsets = ((ComponentSampleModel)image.getSampleModel()).getBandOffsets();

		int p = 0;
		if (bandOffsets.length == 4) {
			while (p < samples.length) {
				byte r = samples[p + bandOffsets[0]];
				byte g = samples[p + bandOffsets[1]];
				byte b = samples[p + bandOffsets[2]];
				byte a = samples[p + bandOffsets[3]];
				codec.encode(new QoiColor(r, g, b, a), dst);
				p += 4;
			}
		} else if (bandOffsets.length == 3) {
			while (p < samples.length) {
				byte r = samples[p + bandOffsets[0]];
				byte g = samples[p + bandOffsets[1]];
				byte b = samples[p + bandOffsets[2]];
				codec.encode(new QoiColor(r, g, b, (byte)255), dst);
				p += 3;
			}
		} else {
			throw new AssertionError("Analyzer returned invalid image: " + image);
		}
	}

	private void encodeDirectColorModelImage(BufferedImage image, ByteBuffer dst) {
		int[] pixels     = ((DataBufferInt)image.getRaster().getDataBuffer()).getData();
		int[] bitOffsets = ((SinglePixelPackedSampleModel)image.getSampleModel()).getBitOffsets();

		if (bitOffsets.length == 4) {
			for (int pixel : pixels) {
				byte r = (byte)(pixel >> bitOffsets[0]);
				byte g = (byte)(pixel >> bitOffsets[1]);
				byte b = (byte)(pixel >> bitOffsets[2]);
				byte a = (byte)(pixel >> bitOffsets[3]);
				codec.encode(new QoiColor(r, g, b, a), dst);
			}
		} else if (bitOffsets.length == 3) {
			for (int pixel : pixels) {
				byte r = (byte)(pixel >> bitOffsets[0]);
				byte g = (byte)(pixel >> bitOffsets[1]);
				byte b = (byte)(pixel >> bitOffsets[2]);
				codec.encode(new QoiColor(r, g, b, (byte)255), dst);
			}
		} else {
			throw new AssertionError("Analyzer returned invalid image: " + image.getSampleModel());
		}
	}

	private void writeFooter(ByteBuffer dst) {
		int  maxInstructionSize = codec.getMaxInstructionSize();
		byte footerCode         = codec.getFooterCode();

		for (int i = 0; i < maxInstructionSize; i++) {
			dst.put(footerCode);
		}
	}
}
