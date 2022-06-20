package org.digitalmodular.qoiflow.instruction;

import java.nio.ByteBuffer;

import org.digitalmodular.qoiflow.QoiColor;
import org.digitalmodular.qoiflow.QoiColorChroma;
import org.digitalmodular.qoiflow.QoiColorRun;
import org.digitalmodular.qoiflow.QoiPixelData;

/**
 * @author Mark Jeronimus
 */
// Created 2022-06-14
public class QoiInstructionChroma extends QoiInstruction {
	/**
	 * The amount to move the LSB bits from the original position to the left-most position.
	 * <p>
	 * Examples for RGBA5654:
	 * <pre>
	 * msbShiftDY = 27; // i.e. 0b00000000000000000000000000011111 -> 0b11111000000000000000000000000000
	 * msbShiftCB = 26; // i.e. 0b00000000000000000000000000111111 -> 0b11111100000000000000000000000000
	 * msbShiftCR = 27; // i.e. 0b00000000000000000000000000011111 -> 0b11111000000000000000000000000000
	 * msbShiftDA = 28; // i.e. 0b00000000000000000000000000001111 -> 0b11110000000000000000000000000000
	 * </pre>
	 */
	private final int msbShiftDY;
	private final int msbShiftCB;
	private final int msbShiftCR;
	private final int msbShiftDA;

	/**
	 * The amount to move the LSB bits from the left-most position to the position in the datagram.
	 * <p>
	 * Examples for RGBA5654:
	 * <pre>
	 * dataShiftDY = 12; // i.e. 0b11111000000000000000000000000000 -> 0b00000000000011111000000000000000
	 * dataShiftCB = 17; // i.e. 0b11111100000000000000000000000000 -> 0b00000000000000000111111000000000
	 * dataShiftCR = 23; // i.e. 0b11111000000000000000000000000000 -> 0b00000000000000000000000111110000
	 * dataShiftDA = 28; // i.e. 0b11110000000000000000000000000000 -> 0b00000000000000000000000000001111
	 * </pre>
	 */
	private final int dataShiftDY;
	private final int dataShiftCB;
	private final int dataShiftCR;
	private final int dataShiftDA;

	private final int numBytes;

	public QoiInstructionChroma(int bitsR, int bitsG, int bitsB, int bitsA) {
		super(bitsR, bitsG, bitsB, bitsA);

		msbShiftDY = 32 - bitsR;
		msbShiftCB = 32 - bitsG;
		msbShiftCR = 32 - bitsB;
		msbShiftDA = 32 - bitsA;

		dataShiftDA = msbShiftDA;
		dataShiftCR = dataShiftDA - bitsB;
		dataShiftCB = dataShiftCR - bitsG;
		dataShiftDY = dataShiftCB - bitsR;

		//noinspection OverridableMethodCallDuringObjectConstruction
		numBytes = getMaxSize();
	}

	@Override
	public int encode(QoiPixelData pixel, byte[] dst) {
		QoiColorChroma chroma = pixel.getChroma();

		if ((bitsA == 0) && chroma.da() != 0) {
			return -1;
		}

		int dy = chroma.dy() << msbShiftDY;
		int cb = chroma.cb() << msbShiftCB;
		int cr = chroma.cr() << msbShiftCR;
		int da = chroma.da() << msbShiftDA;

		int recoveredDY = (dy >> msbShiftDY);
		int recoveredCB = (cb >> msbShiftCB);
		int recoveredCR = (cr >> msbShiftCR);
		int recoveredDA = (da >> msbShiftDA);
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

		dy >>>= dataShiftDY;
		cb >>>= dataShiftCB;
		cr >>>= dataShiftCR;
		da >>>= dataShiftDA;
		int chromaRGBA = dy | cb | cr | da;

		int shift = 8 * (numBytes - 1);
		for (int i = 0; i < numBytes; i++) {
			if (shift < 32) {
				dst[i] = (byte)(chromaRGBA >> shift);
			} else {
				dst[i] = 0;
			}

			shift -= 8;
		}

		dst[0] += codeOffset;

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
	public QoiColorRun decode(int code, ByteBuffer src, QoiColor lastColor) {
		throw new UnsupportedOperationException("TODO");
	}

	@Override
	public boolean canRepeatBytes() {
		return true;
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
