package org.digitalmodular.qoiflow.instruction;

import java.nio.ByteBuffer;

import org.digitalmodular.qoiflow.QoiColor;
import org.digitalmodular.qoiflow.QoiColorChroma;
import org.digitalmodular.qoiflow.QoiPixelData;

/**
 * @author Mark Jeronimus
 */
// Created 2022-06-14
public class QoiInstructionChroma extends QoiInstruction {
	private final int numBytes;

	public QoiInstructionChroma(int bitsR, int bitsG, int bitsB, int bitsA) {
		super(bitsR, bitsG, bitsB, bitsA);

		//noinspection OverridableMethodCallDuringObjectConstruction
		numBytes = getMaxSize();
	}

	@Override
	public int encode(QoiPixelData pixel, byte[] dst) {
		QoiColorChroma chroma = pixel.getChroma();

		if ((bitsA == 0) && chroma.da() != 0) {
			return -1;
		}

		int dy = (chroma.dy() << shiftR) & maskR;
		int cb = (chroma.cb() << shiftG) & maskG;
		int cr = (chroma.cr() << shiftB) & maskB;
		int da = chroma.da() & maskA;

		int recoveredDY = (dy << (32 - numBits)) >> (32 - bitsR);
		int recoveredCB = (cb << (32 - shiftR)) >> (32 - bitsG);
		int recoveredCR = (cr << (32 - shiftG)) >> (32 - bitsB);
		int recoveredDA = (da << (32 - shiftB)) >> (32 - bitsA);
		if (bitsA > 0) {
			if (recoveredDY != chroma.dy() ||
			    recoveredCB != chroma.cb() ||
			    recoveredCR != chroma.cr() ||
			    recoveredDA != chroma.da()) {
				return -1;
			}
		} else {
			if (recoveredDY != chroma.dy() ||
			    recoveredCB != chroma.cb() ||
			    recoveredCR != chroma.cr()) {
				return -1;
			}
		}

		int chromaRGBA = dy | cb | cr | da;

		for (int i = 0; i < numBytes; i++) {
			int shift = 8 * (numBytes - i - 1);
			if (shift < 32)
				dst[i] = (byte)(chromaRGBA >> shift);
			else
				dst[i] = 0;
		}

		dst[0] |= codeOffset;

		if (statistics != null) {
			if (bitsA > 0) {
				statistics.record(this, dst, 0, numBytes, recoveredDY, recoveredCB, recoveredCR, recoveredDA);
			} else {
				statistics.record(this, dst, 0, numBytes, recoveredDY, recoveredCB, recoveredCR);
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
			return "CHROMA" + bitsR + bitsR + bitsR + bitsA;
		} else {
			return "CHROMA" + bitsR + bitsR + bitsR;
		}
	}
}
