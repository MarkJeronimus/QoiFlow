package org.digitalmodular.qoiflow;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;

/**
 * @author Mark Jeronimus
 */
// Created 2022-06-26
public final class QoiFlowUtilities {
	private QoiFlowUtilities() {
		throw new AssertionError();
	}

	/**
	 * @return The compatible image, which is either the original (not a copy) if it's already compatible,
	 * or a new image with the most efficient memory layout, losslessly representing the same image data.
	 */
	public static BufferedImage asCompatibleImage(BufferedImage image) {
		DataBuffer dataBuffer = image.getRaster().getDataBuffer();
		if (dataBuffer instanceof DataBufferByte || dataBuffer instanceof DataBufferInt) {
			int numComponents = (image.getSampleModel()).getNumBands();
			if (numComponents == 3 || numComponents == 4) {
				return image;
			}
		}

		return convertImage(image);
	}

	private static BufferedImage convertImage(BufferedImage image) {
		int numComponents = (image.getSampleModel()).getNumBands();
		int imageType = (numComponents & 1) == 0 ?
		                BufferedImage.TYPE_4BYTE_ABGR : // Typically 2 or 4 components
		                BufferedImage.TYPE_3BYTE_BGR;   // Typically 1 or 3 components

		BufferedImage convertedImage = new BufferedImage(image.getWidth(), image.getHeight(), imageType);

		Graphics2D g = convertedImage.createGraphics();
		try {
			g.setComposite(AlphaComposite.Src);
			g.drawImage(image, 0, 0, null);
		} finally {
			g.dispose();
		}

		return convertedImage;
	}
}
