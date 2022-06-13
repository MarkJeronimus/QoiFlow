package org.digitalmodular.qoiflow.instruction;

import java.nio.ByteBuffer;

import org.digitalmodular.qoiflow.QoiColor;
import org.digitalmodular.qoiflow.QoiPixelData;

/**
 * @author Mark Jeronimus
 */
// Created 2022-06-05
public class QoiInstructionRGBA extends QoiInstruction {
	private final int numBytes;

	public QoiInstructionRGBA(int bitsR, int bitsG, int bitsB, int bitsA) {
		super(bitsR, bitsG, bitsB, bitsA);

		//noinspection OverridableMethodCallDuringObjectConstruction
		numBytes = getMaxSize();
	}

	@Override
	public int encode(QoiPixelData pixel, byte[] dst) {
		if ((bitsA == 0) && pixel.getDelta().da() != 0) {
			return -1;
		}

		QoiColor color = pixel.getColor();

		int r = ((color.r() << 24) >>> msbShiftR) & maskR;
		int g = ((color.g() << 24) >>> msbShiftG) & maskG;
		int b = ((color.b() << 24) >>> msbShiftB) & maskB;
		int a = ((color.a() << 24) >>> msbShiftA) & maskA;

		int recoveredR = r << msbShiftR >>> 24;
		int recoveredG = g << msbShiftG >>> 24;
		int recoveredB = b << msbShiftB >>> 24;
		int recoveredA = a << msbShiftA >>> 24;
		if (bitsA > 0) {
			if (recoveredR != color.r() ||
			    recoveredG != color.g() ||
			    recoveredB != color.b() ||
			    recoveredA != color.a()) {
				return -1;
			}
		} else {
			if (recoveredR != color.r() ||
			    recoveredG != color.g() ||
			    recoveredB != color.b()) {
				return -1;
			}
		}

		int rgba = r | g | b | a;

		for (int i = 0; i < numBytes; i++) {
			int shift = 8 * (numBytes - i - 1);
			if (shift < 32)
				dst[i] = (byte)(rgba >> shift);
			else
				dst[i] = 0;
		}

		dst[0] |= codeOffset;

		if (statistics != null) {
			if (bitsA > 0) {
				statistics.record(this, dst, 0, numBytes, recoveredR, recoveredG, recoveredB, recoveredA);
			} else {
				statistics.record(this, dst, 0, numBytes, recoveredR, recoveredG, recoveredB);
			}
		}

		return numBytes;
	}

	@Override
	public void decode(ByteBuffer src, QoiColor color) {
	}

	@SuppressWarnings("StringConcatenationMissingWhitespace")
	@Override
	public String toString() {
		if (bitsA > 0) {
			return "RGBA" + bitsR + bitsR + bitsR + bitsA;
		} else {
			return "RGB" + bitsR + bitsR + bitsR;
		}
	}
}
