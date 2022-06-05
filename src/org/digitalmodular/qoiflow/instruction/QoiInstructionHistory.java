package org.digitalmodular.qoiflow.instruction;

import java.nio.ByteBuffer;
import java.util.Arrays;

import org.digitalmodular.qoiflow.QoiColor;

/**
 * @author Mark Jeronimus
 */
// Created 2022-06-05
public class QoiInstructionHistory extends QoiInstruction {
	private int[][] recentColorsList = new int[0][];
	private int     recentColorIndex = 0;

	private int recentColorFound = -1;

	public QoiInstructionHistory(int bitsR, int bitsG, int bitsB, int bitsA) {
		super(bitsR, bitsG, bitsB, bitsA);
	}

	@Override
	public int getNumBytes() {
		return 1;
	}

	@Override
	public int getNumCodes() {
		return 0;
	}

	public void setNumValues(int numValues) {
		recentColorsList = new int[numValues][4];
	}

	@Override
	public void reset() {
		for (int[] recentColor : recentColorsList) {
			Arrays.fill(recentColor, 0);
		}

		recentColorIndex = 0;
		recentColorFound = -1;
	}

	@Override
	public boolean canEncode(QoiColor color) {
		recentColorFound = -1;
		for (int i = 0; i < recentColorsList.length; i++) {
			if (recentColorsList[i][0] == color.getCurrentR() &&
			    recentColorsList[i][1] == color.getCurrentG() &&
			    recentColorsList[i][2] == color.getCurrentB() &&
			    recentColorsList[i][3] == color.getCurrentA()) {
				recentColorFound = i;
				break;
			}
		}

		return recentColorFound >= 0;
	}

	@Override
	public void encode(QoiColor color, int startCode, ByteBuffer dst) {
	}

	@Override
	public void decode(ByteBuffer src, int startCode, QoiColor color) {
	}
}
