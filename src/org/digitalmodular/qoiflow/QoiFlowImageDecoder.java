package org.digitalmodular.qoiflow;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Objects;

/**
 * @author Mark Jeronimus
 */
// Created 2022-05-14
// Changed 2022-06-18 Copied from FluidQOI
public class QoiFlowImageDecoder {
	private int width  = 0;
	private int height = 0;

	private final QoiFlowStreamCodec codec;

	public QoiFlowImageDecoder(QoiFlowStreamCodec codec) {
		this.codec = Objects.requireNonNull(codec, "'codec' can't be null");
	}

	public BufferedImage decode(ByteBuffer src) throws IOException {
		readHeader(src);

		codec.reset();

		BufferedImage image  = createImage(width, height);
		byte[]        pixels = ((DataBufferByte)image.getRaster().getDataBuffer()).getData();
		decodeImage(src, pixels);

		return image;
	}

	private void readHeader(ByteBuffer src) throws IOException {
		int magic = src.getInt();

		if (magic != QoiFlowImageEncoder.QOIF_MAGIC) { // "fqoi" in big-endian
			throw new IOException("Bad 'magic': " + Integer.toString(magic, 16));
		}

		width = src.getInt();
		if (width < 0 || width > 32768) {
			throw new IOException("Bad width: " + width);
		}

		height = src.getInt();
		if (height < 0 || height > 32768) {
			throw new IOException("Bad height: " + height);
		}

		src.get();
		src.get();

		// TODO read instruction table
	}

	private static BufferedImage createImage(int width, int height) {
		return new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
	}

	private void decodeImage(ByteBuffer src, byte[] pixels) {
		byte footerCode = codec.getFooterCode();

		boolean  detectFooter = false;
		QoiColor lastColor    = QoiFlowStreamCodec.START_COLOR;

		int p = 0;
		while (true) {
			int code = src.get() & 0xFF;

			boolean isFooterCode = code == footerCode;
			if (detectFooter && isFooterCode) {
				break;
			}

			QoiColorRun colorRun = codec.decode(code, src, lastColor);
			lastColor = colorRun.color();
			int count = colorRun.count();

			if (count * 4 > pixels.length - p) {
				count = (pixels.length - p) / 4;
			}

			p = setRGBA(lastColor, count, pixels, p);

			if (p == pixels.length || src.remaining() < codec.getMaxInstructionSize()) {
				break;
			}

			codec.postDecode(lastColor);

			detectFooter = isFooterCode;
		}
	}

	private static int setRGBA(QoiColor color, int count, byte[] pixels, int p) {
		for (int i = count; i > 0; i--) {
			pixels[p++] = (byte)color.a();
			pixels[p++] = (byte)color.b();
			pixels[p++] = (byte)color.g();
			pixels[p++] = (byte)color.r();
		}

		return p;
	}
}
