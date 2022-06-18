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
	/**
	 * The amount to move the LSB bits from position 32 to the position in the datagram.
	 * <p>
	 * Examples for RGBA5654:
	 * <pre>
	 * shiftR = 15; // i.e. 0b00000000_00000000_00000000_00011111 -> 0b00000000_00001111_10000000_00000000
	 * shiftG =  9; // i.e. 0b00000000_00000000_00000000_00111111 -> 0b00000000_00000000_01111110_00000000
	 * shiftB =  4; // i.e. 0b00000000_00000000_00000000_00011111 -> 0b00000000_00000000_00000001_11110000
	 * </pre>
	 * Alternatively, think of this as the number of 0-bits to the right of the 1-bits in the corresponding mask.
	 */
	private final int shiftR;
	private final int shiftG;
	private final int shiftB;

	/**
	 * The occupied bits for each component in the datagram.
	 * <p>
	 * Examples for RGBA5654:
	 * <pre>
	 * maskR = 0b00000000_00001111_10000000_00000000;
	 * maskG = 0b00000000_00000000_01111110_00000000;
	 * maskB = 0b00000000_00000000_00000001_11110000;
	 * maskA = 0b00000000_00000000_00000000_00001111;
	 * </pre>
	 * Alternatively, think of this as the number of 0-bits to the right of the 1-bits in the corresponding mask.
	 */
	private final int maskR;
	private final int maskG;
	private final int maskB;
	private final int maskA;

	private final int numBytes;

	public QoiInstructionDelta(int bitsR, int bitsG, int bitsB, int bitsA) {
		super(bitsR, bitsG, bitsB, bitsA);

		shiftB = bitsA;
		shiftG = shiftB + bitsB;
		shiftR = shiftG + bitsG;

		maskR = (1 << numBits) - (1 << shiftR);
		maskG = (1 << shiftR) - (1 << shiftG);
		maskB = (1 << shiftG) - (1 << shiftB);
		maskA = (1 << shiftB) - 1;

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

		dst[0] += codeOffset;

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
