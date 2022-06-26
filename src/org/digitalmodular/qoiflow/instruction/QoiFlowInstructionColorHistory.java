package org.digitalmodular.qoiflow.instruction;

import java.nio.ByteBuffer;
import java.util.Arrays;

import org.digitalmodular.qoiflow.QoiFlowColor;
import org.digitalmodular.qoiflow.QoiFlowColorRun;
import org.digitalmodular.qoiflow.QoiFlowPixelData;

/**
 * @author Mark Jeronimus
 */
// Created 2022-06-05
public class QoiFlowInstructionColorHistory extends QoiFlowInstruction {
	public static final QoiFlowColor INITIAL_COLOR = new QoiFlowColor(0, 0, 0, 0);

	private QoiFlowColor[] recentColorsList = new QoiFlowColor[1];
	private int            recentColorIndex = 0;

	// Encoder state
	private QoiFlowColor lastColor = null;

	// Decoder state
	private boolean pixelDecoded = false;

	public QoiFlowInstructionColorHistory() {
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

		recentColorsList = new QoiFlowColor[calculatedCodeCount];
	}

	@Override
	public void reset() {
		Arrays.fill(recentColorsList, INITIAL_COLOR);
		recentColorIndex = 0;
		lastColor = null;
		pixelDecoded = false;
	}

	@Override
	public int encode(QoiFlowPixelData pixel, byte[] dst) {
		QoiFlowColor color = pixel.getColor();

		boolean repeatColor = color.equals(lastColor);
		lastColor = null;

		if (repeatColor) {
			return -1;
		}

		for (int i = 0; i < recentColorsList.length; i++) {
			if (recentColorsList[i].equals(color)) {
				dst[0] = (byte)(codeOffset + i);

				if (statistics != null) {
					statistics.record(this, dst, 0, 1, color, i);
				}

				lastColor = color;
				return 1;
			}
		}

		// Unknown colors get added.
		recentColorsList[recentColorIndex] = color;
		recentColorIndex = (recentColorIndex + 1) % recentColorsList.length;

		return -1;
	}

	@Override
	public QoiFlowColorRun decode(int code, ByteBuffer src, QoiFlowColor lastColor) {
		pixelDecoded = true;

		int          index = code - codeOffset;
		QoiFlowColor color = recentColorsList[index];

		if (statistics != null) {
			statistics.record(this, src, 1, color, index);
		}

		return new QoiFlowColorRun(color, 1);
	}

	@Override
	public void postDecode(QoiFlowColor color) {
		if (pixelDecoded) {
			pixelDecoded = false;
		} else {
			for (QoiFlowColor storedColor : recentColorsList) {
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
