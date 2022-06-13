package org.digitalmodular.qoiflow.instruction;

import java.nio.ByteBuffer;

import org.digitalmodular.qoiflow.QoiColor;
import org.digitalmodular.qoiflow.QoiColorDelta;
import org.digitalmodular.qoiflow.QoiPixelData;

/**
 * @author Mark Jeronimus
 */
// Created 2022-06-12
public class QoiInstructionDelta extends QoiInstruction {
	private final int numBytes;

	public QoiInstructionDelta(int bitsR, int bitsG, int bitsB, int bitsA) {
		super(bitsR, bitsG, bitsB, bitsA);

		//noinspection OverridableMethodCallDuringObjectConstruction
		numBytes = getMaxSize();
	}

	@Override
	public int encode(QoiPixelData pixel, byte[] dst) {
		QoiColorDelta delta = pixel.getDelta();

		if ((bitsA == 0) && delta.deltaA() != 0) {
			return -1;
		}

		int r = (delta.deltaR() << shiftR) & maskR;
		int g = (delta.deltaG() << shiftG) & maskG;
		int b = (delta.deltaB() << shiftB) & maskB;
		int a = delta.deltaA() & maskA;


		if (bitsA > 0) {
		if ((r << (32 - numBits)) >> (32 - bitsR) != delta.deltaR() ||
		    (g << (32 - shiftR)) >> (32 - bitsG) != delta.deltaG() ||
		    (b << (32 - shiftG)) >> (32 - bitsB) != delta.deltaB() ||
		    (a << (32 - shiftB)) >> (32 - bitsA) != delta.deltaA()) {
			return -1;
			}
		} else {
			if ((r << (32 - numBits)) >> (32 - bitsR) != delta.deltaR() ||
			    (g << (32 - shiftR)) >> (32 - bitsG) != delta.deltaG() ||
			    (b << (32 - shiftG)) >> (32 - bitsB) != delta.deltaB()) {
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

		return numBytes;
	}

	@Override
	public void decode(ByteBuffer src, QoiColor color) {
	}

	@SuppressWarnings("StringConcatenationMissingWhitespace")
	@Override
	public String toString() {
		if (bitsA > 0) {
			return "DELTA" + bitsR + bitsR + bitsR + bitsA;
		} else {
			return "DELTA" + bitsR + bitsR + bitsR;
		}
	}
}
