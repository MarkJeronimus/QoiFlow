package org.digitalmodular.qoiflow.instruction;

import java.nio.ByteBuffer;

import org.digitalmodular.qoiflow.QoiColor;
import org.digitalmodular.qoiflow.QoiFlowCodec;
import org.digitalmodular.qoiflow.QoiPixelData;
import static org.digitalmodular.util.Validators.requireRange;

/**
 * @author Mark Jeronimus
 */
// Created 2022-06-05
public abstract class QoiInstruction {
	protected final int bitsR;
	protected final int bitsG;
	protected final int bitsB;
	protected final int bitsA;

	protected final int shiftR;
	protected final int shiftG;
	protected final int shiftB;

	protected final int msbShiftR;
	protected final int msbShiftG;
	protected final int msbShiftB;
	protected final int msbShiftA;

	protected final int maskR;
	protected final int maskG;
	protected final int maskB;
	protected final int maskA;

	protected final int numBits;

	protected int codeOffset          = 0;
	protected int calculatedCodeCount = 1;

	protected QoiInstruction(int bitsR, int bitsG, int bitsB, int bitsA) {
		this.bitsR = requireRange(1, 8, bitsR, "bitsR");
		this.bitsG = requireRange(1, 8, bitsG, "bitsG");
		this.bitsB = requireRange(1, 8, bitsB, "bitsB");
		this.bitsA = requireRange(0, 8, bitsA, "bitsA");

		shiftB = bitsA;
		shiftG = shiftB + bitsB;
		shiftR = shiftG + bitsG;
		numBits = shiftR + bitsR;

		msbShiftA = 32 - bitsA;
		msbShiftB = msbShiftA - bitsB;
		msbShiftG = msbShiftB - bitsG;
		msbShiftR = msbShiftG - bitsR;

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
	 * Returns the minimum number of bytes this instruction can use.
	 * <p>
	 * By default this is identical to {@link #getMaxSize()}.
	 */
	public int getMinSize() {
		return getMaxSize();
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

	public void setCodeOffsetAndCount(int codeOffset, int calculatedCodeCount) {
		this.codeOffset = codeOffset;
		this.calculatedCodeCount = calculatedCodeCount;
	}

	public int getCodeOffset() {
		return codeOffset;
	}

	/**
	 * Returns the total number of codes (values) of the first byte of this instruction, as calculated by the codec.
	 * <p>
	 * This is only valid when {@link QoiFlowCodec#reset()} has been called on the codec.
	 * <p>
	 * When this is not a variable-length instruction, the value will then equal {@link #getNumCodes()}.
	 */
	public int getCalculatedCodeCount() {
		return calculatedCodeCount;
	}

	/**
	 * Resets internal state of the encoder/decoder.
	 * <p>
	 * Does nothing unless overridden.
	 */
	public void reset() {
	}

	/**
	 * Give the instruction the opportunity to emit deferred data based on the new pixel.
	 * <p>
	 * Unlike {@link #encode(QoiPixelData, byte[])}, this will be called on every instruction always.
	 * <p>
	 * This is required, for example, for RLE, to emit instructions when the color is no longer equal to the previous.
	 * <p>
	 * Does nothing unless overridden.
	 */
	public void preEncode(QoiPixelData pixel, ByteBuffer dst) {
	}

	/**
	 * Attempts to encode the pixel.
	 * <p>
	 * The first instruction that can encode the pixel will end the encoding phase.
	 *
	 * @return The amount of bytes this instruction stored in {@code dst} encode the color,
	 * or -1 if it could not encode.
	 */
	public abstract int encode(QoiPixelData pixel, byte[] dst);

	/**
	 * Give the instruction the opportunity to emit deferred data at the end of the stream.
	 * <p>
	 * This marks the end of an encoding cycle.
	 * Continuing to use this instance without first calling {@link #reset()} results in unspecified behavior.
	 * <p>
	 * This is required, for example, for RLE, to emit instructions when the counter is {@code > 1}.
	 * <p>
	 * Does nothing unless overridden.
	 */
	public void postEncode(ByteBuffer dst) {
	}

	public abstract void decode(ByteBuffer src, QoiColor color);
}
