package org.digitalmodular.qoiflow.instruction;

import java.nio.ByteBuffer;

import org.digitalmodular.qoiflow.QoiColor;
import static org.digitalmodular.util.Validators.requireAtLeast;

/**
 * @author Mark Jeronimus
 */
// Created 2022-06-05
public abstract class QoiInstruction {
	protected final int bitsR;
	protected final int bitsG;
	protected final int bitsB;
	protected final int bitsA;

	protected final int numBits;
	protected final int shiftR;
	protected final int shiftG;
	protected final int shiftB;

	protected final int maskR;
	protected final int maskG;
	protected final int maskB;
	protected final int maskA;

	protected int codeOffset = 0;

	protected QoiInstruction(int bitsR, int bitsG, int bitsB, int bitsA) {
		this.bitsR = requireAtLeast(1, bitsR, "bitsR");
		this.bitsG = requireAtLeast(1, bitsG, "bitsG");
		this.bitsB = requireAtLeast(1, bitsB, "bitsB");
		this.bitsA = requireAtLeast(0, bitsA, "bitsA");

		shiftB = bitsA;
		shiftG = shiftB + bitsB;
		shiftR = shiftG + bitsG;
		numBits = shiftR + bitsR;

		maskR = (1 << numBits) - (1 << shiftR);
		maskG = (1 << shiftR) - (1 << shiftG);
		maskB = (1 << shiftG) - (1 << shiftB);
		maskA = (1 << shiftB) - 1;
	}

	/**
	 * Returns the total number of codes (values) of the first byte of this instruction,
	 * or 0 if this instruction is variable.
	 */
	public int getNumCodes() {
		int numBytes     = (numBits + 8) / 8;
		int numRemaining = numBytes * 8 - numBits;
		return 1 << (8 - numRemaining);
	}

	/**
	 * Returns the maximum number of bytes this instruction can use.
	 * <p>
	 * By default this is determined by the sum of the number of bits of the four components.
	 * This can be overridden by subclasses that don't encode components in this way.
	 */
	public int getMaxSize() {
		return (numBits + 8) / 8;
	}

	/**
	 * Returns the minimum number of bytes this instruction can use.
	 * <p>
	 * By default this is identical to {@link #getMaxSize()}.
	 */
	public int getMinSize() {
		return getMaxSize();
	}

	public int getCodeOffset() {
		return codeOffset;
	}

	public void setCodeOffset(int codeOffset) {
		this.codeOffset = codeOffset;
	}

	/**
	 * Resets internal state of the encoder/decoder.
	 * <p>
	 * Does nothing unless overridden.
	 */
	public void reset() {
	}

	public abstract int encode(QoiColor color, byte[] dst);

	/**
	 * Notifies to to this encoder that the provided color has been encoded (not necessarily by this instruction).
	 * <p>
	 * Useful for <em>e.g.</em> RLE encoders to determine if the next color matches the last.
	 * <p>
	 * Does nothing unless overridden.
	 */
	public void colorEncoded(QoiColor color) {
	}

	public abstract void decode(ByteBuffer src, QoiColor color);
}
