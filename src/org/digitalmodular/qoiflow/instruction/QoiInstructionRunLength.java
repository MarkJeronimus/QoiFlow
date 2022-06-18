package org.digitalmodular.qoiflow.instruction;

import java.nio.ByteBuffer;

import org.digitalmodular.qoiflow.QoiColor;
import org.digitalmodular.qoiflow.QoiPixelData;

/**
 * @author Mark Jeronimus
 */
// Created 2022-06-12
public class QoiInstructionRunLength extends QoiInstruction {
	private int repeatCount = 0;

	public QoiInstructionRunLength() {
		super(1, 1, 1, 0); // Bits are irrelevant
	}

	@Override
	public int getNumCodes() {
		return 0;
	}

	@Override
	public int getMaxSize() {
		// encode() cannot emit bytes. Only preEncode() can.
		return 0;
	}

	@Override
	public void reset() {
		repeatCount = 0;
	}

	@Override
	public void preEncode(QoiPixelData pixel, ByteBuffer dst) {
		if (!pixel.getColor().equals(pixel.getPrevious())) {
			postEncode(dst);
		}
	}

	@Override
	public int encode(QoiPixelData pixel, byte[] dst) {
		if (pixel.getColor().equals(pixel.getPrevious())) {
			repeatCount++;
			return 0;
		}

		return -1;
	}

	@Override
	public void postEncode(ByteBuffer dst) {
		// Encode the value (minus 1) using bijective notation (rather than the more common positional notation).
		// If there are 26 symbols to choose from, bijective notation is equal to spreadsheet column notation.
		// if there are 10 symbols to choose from, the first few values are encoded as:
		// 0 -> 0
		// 1 -> 1
		// ...
		// 9 -> 9
		// 10 -> 00
		// 11 -> 01
		// ...
		// 109 -> 99
		// This differs from base-10 (decimal) in that leading zeros are not redundant, so the encoding has a higher
		// entropy. This won't work in fixed-width encodings (which is most common in computers), but is perfect for
		// variable-width encodings.

		if (repeatCount > 1) {
			int remainingValue = repeatCount - 1;
			int len            = 0;

			do {
				int countMinusOne = remainingValue % calculatedCodeCount;
				int data          = codeOffset + countMinusOne;

				dst.put((byte)data);
				len++;

				remainingValue = ((remainingValue - countMinusOne) / calculatedCodeCount) - 1;
			} while (remainingValue >= 0);

			if (statistics != null) {
				statistics.record(this, dst.array(), dst.position() - len, len, repeatCount);
			}

			repeatCount = 1;
		}
	}

	@Override
	public void decode(ByteBuffer src, QoiColor color) {
	}

	@Override
	public String toString() {
		//noinspection StringConcatenationMissingWhitespace
		return "RLE" + calculatedCodeCount;
	}
}
