package org.digitalmodular.qoiflow;

import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.ComponentSampleModel;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.nio.ByteBuffer;
import java.util.Objects;

/**
 * @author Mark Jeronimus
 */
// Created 2022-05-16
// Changed 2022-06-18 Copied from FluidQOI
public class ImageEncoder {
	@SuppressWarnings("CharUsedInArithmeticContext")
	public static final int QOIF_MAGIC    = 'Q' << 24 |
	                                        'O' << 16 |
	                                        'I' << 8 |
	                                        'F';
	public static final int HEADER_LENGTH = 32;

	private final QoiFlowCodec codec;

	public ImageEncoder(QoiFlowCodec codec) {
		this.codec = Objects.requireNonNull(codec, "'codec' can't be null");
	}

	/**
	 * @return A new ByteBuffer, backed by a heap array (with royally overestimated capacity).
	 */
	public ByteBuffer encode(BufferedImage image) {
		Objects.requireNonNull(image, "image");

		int width  = image.getWidth();
		int height = image.getHeight();

		ByteBuffer dst = ByteBuffer.allocate(HEADER_LENGTH + width * height * codec.getMaxInstructionSize());

		codec.reset();
		codec.printCodeOffsets();

		writeHeader(width, height, dst);
		encodeImage(image, dst);
		codec.finishEncoding(dst);
		writeFooter(dst);

		return dst;
	}

	private static void writeHeader(int width, int height, ByteBuffer dst) {
		dst.putInt(QOIF_MAGIC);
		dst.putInt(width);
		dst.putInt(height);

		// TODO write instruction table
	}

	private void encodeImage(BufferedImage image, ByteBuffer dst) {
		boolean hasAlpha  = image.getColorModel().hasAlpha();
		int     imageType = hasAlpha ? BufferedImage.TYPE_4BYTE_ABGR : BufferedImage.TYPE_3BYTE_BGR;

		BufferedImage convertedImage = new BufferedImage(image.getWidth(), image.getHeight(), imageType);

		Graphics2D g = convertedImage.createGraphics();
		try {
			g.setComposite(AlphaComposite.Src);
			g.drawImage(image,0,0,null);
		} finally {
			g.dispose();
		}

		encodeComponentColorModelImage(convertedImage.getRaster(), hasAlpha, dst);
	}

	private void encodeComponentColorModelImage(Raster raster, boolean hasAlpha, ByteBuffer dst) {
		byte[] samples     = ((DataBufferByte)raster.getDataBuffer()).getData();
		int[]  bandOffsets = ((ComponentSampleModel)raster.getSampleModel()).getBandOffsets();

		int p = 0;
		if (hasAlpha) {
			while (p < samples.length) {
				byte r = samples[p + bandOffsets[0]];
				byte g = samples[p + bandOffsets[1]];
				byte b = samples[p + bandOffsets[2]];
				byte a = samples[p + bandOffsets[3]];
				codec.encode(new QoiColor(r, g, b, a), dst);
				p += 4;
			}
		} else {
			while (p < samples.length) {
				byte r = samples[p + bandOffsets[0]];
				byte g = samples[p + bandOffsets[1]];
				byte b = samples[p + bandOffsets[2]];
				codec.encode(new QoiColor(r, g, b, (byte)255), dst);
				p += 3;
			}
		}
	}

	private void writeFooter(ByteBuffer dst) {
		int maxInstructionSize = codec.getMaxInstructionSize();
		for (int i = 0; i < maxInstructionSize; i++) {
			dst.put((byte)0);
		}
	}
}
