package org.digitalmodular.qoiflow.instruction;

import java.nio.ByteBuffer;

import org.digitalmodular.qoiflow.QoiColor;
import org.digitalmodular.qoiflow.QoiColorDelta;
import org.digitalmodular.qoiflow.QoiColorRun;
import org.digitalmodular.qoiflow.QoiPixelData;

/**
 * @author Mark Jeronimus
 */
// Created 2022-06-12
public class QoiInstructionDelta extends QoiInstruction {
	/**
	 * The amount to move the LSB bits from the original position to the left-most position.
	 * <p>
	 * Examples for RGBA5654:
	 * <pre>
	 * msbShiftDR = 27; // i.e. 0b00000000000000000000000000011111 -> 0b11111000000000000000000000000000
	 * msbShiftDG = 26; // i.e. 0b00000000000000000000000000111111 -> 0b11111100000000000000000000000000
	 * msbShiftDB = 27; // i.e. 0b00000000000000000000000000011111 -> 0b11111000000000000000000000000000
	 * msbShiftDA = 28; // i.e. 0b00000000000000000000000000001111 -> 0b11110000000000000000000000000000
	 * </pre>
	 */
	private final int msbShiftDR;
	private final int msbShiftDG;
	private final int msbShiftDB;
	private final int msbShiftDA;

	/**
	 * The amount to move the LSB bits from the left-most position to the position in the datagram.
	 * <p>
	 * Examples for RGBA5654:
	 * <pre>
	 * dataShiftDR = 12; // i.e. 0b11111000000000000000000000000000 -> 0b00000000000011111000000000000000
	 * dataShiftDG = 17; // i.e. 0b11111100000000000000000000000000 -> 0b00000000000000000111111000000000
	 * dataShiftDB = 23; // i.e. 0b11111000000000000000000000000000 -> 0b00000000000000000000000111110000
	 * dataShiftDA = 28; // i.e. 0b11110000000000000000000000000000 -> 0b00000000000000000000000000001111
	 * </pre>
	 */
	private final int dataShiftDR;
	private final int dataShiftDG;
	private final int dataShiftDB;
	private final int dataShiftDA;

	private final int numBytes;

	public QoiInstructionDelta(int bitsR, int bitsG, int bitsB, int bitsA) {
		super(bitsR, bitsG, bitsB, bitsA);

		msbShiftDR = 32 - bitsR;
		msbShiftDG = 32 - bitsG;
		msbShiftDB = 32 - bitsB;
		msbShiftDA = 32 - bitsA;

		dataShiftDA = msbShiftDA;
		dataShiftDB = dataShiftDA - bitsB;
		dataShiftDG = dataShiftDB - bitsG;
		dataShiftDR = dataShiftDG - bitsR;

		//noinspection OverridableMethodCallDuringObjectConstruction
		numBytes = getMaxSize();
	}

	@Override
	public int encode(QoiPixelData pixel, byte[] dst) {
		QoiColorDelta delta = pixel.getDelta();

		if ((bitsA == 0) && delta.da() != 0) {
			return -1;
		}

		int dr = delta.dr() << msbShiftDR;
		int dg = delta.dg() << msbShiftDG;
		int db = delta.db() << msbShiftDB;
		int da = delta.da() << msbShiftDA;

		int recoveredDR = (dr >> msbShiftDR);
		int recoveredDG = (dg >> msbShiftDG);
		int recoveredDB = (db >> msbShiftDB);
		int recoveredDA = (da >> msbShiftDA);
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

		dr >>>= dataShiftDR;
		dg >>>= dataShiftDG;
		db >>>= dataShiftDB;
		da >>>= dataShiftDA;
		int rgba = dr | dg | db | da;

		int shift = 8 * (numBytes - 1);
		for (int i = 0; i < numBytes; i++) {
			if (shift < 32) {
				dst[i] = (byte)(rgba >> shift);
			} else {
				dst[i] = 0;
			}

			shift -= 8;
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
	public QoiColorRun decode(int code, ByteBuffer src, QoiColor lastColor) {
		int rgba = code - codeOffset;

		for (int i = 1; i < numBytes; i++) {
			rgba = (rgba << 8) | (src.get() & 0xFF);
		}

		int dr = (rgba << dataShiftDR) >> msbShiftDR;
		int dg = (rgba << dataShiftDG) >> msbShiftDG;
		int db = (rgba << dataShiftDB) >> msbShiftDB;
		int da = (rgba << dataShiftDA) >> msbShiftDA;

		if (statistics != null) {
			if (bitsA > 0) {
				statistics.record(this, src, numBytes, dr, dg, db, da);
			} else {
				statistics.record(this, src, numBytes, dr, dg, db);
			}
		}

		return new QoiColorRun(new QoiColorDelta(dr, dg, db, da).applyTo(lastColor), 1);
	}

	@Override
	public boolean canRepeatBytes() {
		return true;
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
