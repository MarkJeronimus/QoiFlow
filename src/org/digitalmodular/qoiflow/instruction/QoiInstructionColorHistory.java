package org.digitalmodular.qoiflow.instruction;

import java.nio.ByteBuffer;
import java.util.Arrays;

import org.digitalmodular.qoiflow.QoiColor;
import org.digitalmodular.qoiflow.QoiPixelData;

/**
 * @author Mark Jeronimus
 */
// Created 2022-06-05
public class QoiInstructionColorHistory extends QoiInstruction {
	public static final QoiColor INITIAL_COLOR = new QoiColor(0, 0, 0, 0);

	private QoiColor[] recentColorsList = new QoiColor[1];
	private int        recentColorIndex = 0;

	private boolean recentColorFound = false;

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

	public void setNumValues(int numValues) {
		recentColorsList = new QoiColor[numValues];
	}

	@Override
	public void reset() {
		Arrays.fill(recentColorsList, INITIAL_COLOR);
		recentColorIndex = 0;
		recentColorFound = false;
	}

	@Override
	public int encode(QoiPixelData pixel, byte[] dst) {
		QoiColor color = pixel.getColor();

		recentColorFound = false;
		for (int i = 0; i < recentColorsList.length; i++) {
			if (recentColorsList[i].equals(color)) {
				recentColorFound = true;
				dst[0] = (byte)(i + codeOffset);
				return 1;
			}
		}

		return 0;
	}

	@Override
	public void colorEncoded(QoiColor color) {
		if (!recentColorFound) {
			recentColorsList[recentColorIndex] = color;
			recentColorIndex = (recentColorIndex + 1) % recentColorsList.length;
		}
	}

	@Override
	public void decode(ByteBuffer src, QoiColor color) {
	}
}
