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

		if ((bitsA == 0) && delta.da() != 0) {
			return -1;
		}

		int dr = (delta.dr() << shiftR) & maskR;
		int dg = (delta.dg() << shiftG) & maskG;
		int db = (delta.db() << shiftB) & maskB;
		int da = delta.da() & maskA;

		int recoveredDR = (dr << (32 - numBits)) >> (32 - bitsR);
		int recoveredDG = (dg << (32 - shiftR)) >> (32 - bitsG);
		int recoveredDB = (db << (32 - shiftG)) >> (32 - bitsB);
		int recoveredDA = (da << (32 - shiftB)) >> (32 - bitsA);
		if (bitsA > 0) {
			if (recoveredDR != delta.dr() ||
			    recoveredDG != delta.dg() ||
			    recoveredDB != delta.db() ||
			    recoveredDA != delta.da()) {
				return -1;
			}
		} else {
			if (recoveredDR != delta.dr() ||
			    recoveredDG != delta.dg() ||
			    recoveredDB != delta.db()) {
				return -1;
			}
		}

		int rgba = dr | dg | db | da;

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
				statistics.record(this, dst, 0, numBytes, recoveredDR, recoveredDG, recoveredDB, recoveredDA);
			} else {
				statistics.record(this, dst, 0, numBytes, recoveredDR, recoveredDG, recoveredDB);
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
			return "DELTA" + bitsR + bitsR + bitsR + bitsA;
		} else {
			return "DELTA" + bitsR + bitsR + bitsR;
		}
	}
}
