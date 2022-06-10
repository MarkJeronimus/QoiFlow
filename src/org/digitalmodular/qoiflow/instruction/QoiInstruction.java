package org.digitalmodular.qoiflow.instruction;

import java.nio.ByteBuffer;

import org.digitalmodular.qoiflow.QoiColor;
import static org.digitalmodular.util.Validators.requireAtLeast;

/**
 * @author Mark Jeronimus
 */
// Created 2022-06-05
public abstract class QoiInstruction {
	protected int bitsR;
	protected int bitsG;
	protected int bitsB;
	protected int bitsA;

	protected int codeOffset = 0;

	protected QoiInstruction(int bitsR, int bitsG, int bitsB, int bitsA) {
		this.bitsR = requireAtLeast(1, bitsR, "bitsR");
		this.bitsG = requireAtLeast(1, bitsG, "bitsG");
		this.bitsB = requireAtLeast(1, bitsB, "bitsB");
		this.bitsA = requireAtLeast(0, bitsA, "bitsA");
	}

	/**
	 * Returns the total number of bytes this instruction uses.
	 */
	public int getNumBytes() {
		int numBits = bitsR + bitsG + bitsB + bitsA;
		return (numBits + 8) / 8;
	}

	/**
	 * Returns the total number of codes (values) of the first byte of this instruction,
	 * or 0 if this instruction is variable.
	 */
	public int getNumCodes() {
		int numBits      = bitsR + bitsG + bitsB + bitsA;
		int numBytes     = (numBits + 8) / 8;
		int numRemaining = numBytes * 8 - numBits;
		return 1 << (8 - numRemaining);
	}

	public int getCodeOffset() {
		return codeOffset;
	}

	public void setCodeOffset(int codeOffset) {
		this.codeOffset = codeOffset;
	}

	public abstract void reset();

	public abstract boolean canEncode(QoiColor color);

	public abstract void encode(QoiColor color, ByteBuffer dst);

	public abstract void decode(ByteBuffer src, QoiColor color);
}
