package org.digitalmodular.qoiflow.instruction;

import java.nio.ByteBuffer;

import org.digitalmodular.qoiflow.QoiColor;

/**
 * @author Mark Jeronimus
 */
// Created 2022-06-05
public class QoiInstructionRGBA extends QoiInstruction {
	public QoiInstructionRGBA(int bitsR, int bitsG, int bitsB, int bitsA) {
		super(bitsR, bitsG, bitsB, bitsA);
	}

	@Override
	public void reset() {
	}

	@Override
	public boolean canEncode(QoiColor color) {
		return false;
	}

	@Override
	public void encode(QoiColor color, ByteBuffer dst) {
	}

	@Override
	public void decode(ByteBuffer src, QoiColor color) {
	}
}
