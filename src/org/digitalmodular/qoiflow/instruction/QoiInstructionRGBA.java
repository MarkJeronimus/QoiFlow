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
	public int encode(QoiColor color, byte[] dst) {
		return 0;
	}

	@Override
	public void decode(ByteBuffer src, QoiColor color) {
	}
}
