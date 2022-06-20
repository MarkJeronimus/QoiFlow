package org.digitalmodular.qoiflow.instruction;

import java.nio.ByteBuffer;
import java.util.Arrays;

import org.digitalmodular.qoiflow.QoiColor;
import org.digitalmodular.qoiflow.QoiColorRun;
import org.digitalmodular.qoiflow.QoiPixelData;

/**
 * @author Mark Jeronimus
 */
// Created 2022-06-05
public class QoiInstructionColorHistory extends QoiInstruction {
	public static final QoiColor INITIAL_COLOR = new QoiColor(0, 0, 0, 0);

	private QoiColor[] recentColorsList = new QoiColor[1];
	private int        recentColorIndex = 0;

	// Decoder state
	private boolean pixelDecoded = false;

	public QoiInstructionColorHistory() {
		super(1, 1, 1, 0); // Bits are irrelevant
	}

	@Override
	public int getNumCodes() {
		return 0;
	}

	@Override
	public int getMaxSize() {
		return 1;
	}

	@Override
	public void setCodeOffsetAndCount(int codeOffset, int calculatedCodeCount) {
		super.setCodeOffsetAndCount(codeOffset, calculatedCodeCount);

		recentColorsList = new QoiColor[calculatedCodeCount];
	}

	@Override
	public void reset() {
		Arrays.fill(recentColorsList, INITIAL_COLOR);
		recentColorIndex = 0;
		pixelDecoded = false;
	}

	@Override
	public int encode(QoiPixelData pixel, byte[] dst) {
		QoiColor color = pixel.getColor();

		for (int i = 0; i < recentColorsList.length; i++) {
			if (recentColorsList[i].equals(color)) {
				dst[0] = (byte)(codeOffset + i);

				if (statistics != null) {
					statistics.record(this, dst, 0, 1, i);
				}

				return 1;
			}
		}

		// Unknown colors get added.
		recentColorsList[recentColorIndex] = color;
		recentColorIndex = (recentColorIndex + 1) % recentColorsList.length;

		return -1;
	}

	@Override
	public QoiColorRun decode(int code, ByteBuffer src, QoiColor lastColor) {
		pixelDecoded = true;

		int index = code - codeOffset;

		if (statistics != null) {
			statistics.record(this, src, 1, index);
		}

		return new QoiColorRun(recentColorsList[index], 1);
	}

	@Override
	public void postDecode(QoiColor color) {
		if (pixelDecoded) {
			pixelDecoded = false;
		} else {
			for (QoiColor storedColor : recentColorsList) {
				if (storedColor.equals(color)) {
					return;
				}
			}

			// Unknown colors get added.
			recentColorsList[recentColorIndex] = color;
			recentColorIndex = (recentColorIndex + 1) % recentColorsList.length;
		}
	}

	@Override
	public boolean canRepeatBytes() {
		return false;
	}

	@Override
	public String toString() {
		//noinspection StringConcatenationMissingWhitespace
		return "HIST" + calculatedCodeCount;
	}
}
