package org.digitalmodular.qoiflow.instruction;

import java.nio.ByteBuffer;

import org.digitalmodular.qoiflow.QoiColor;
import org.digitalmodular.qoiflow.QoiColorRun;
import org.digitalmodular.qoiflow.QoiFlowStreamCodec;
import org.digitalmodular.qoiflow.QoiPixelData;
import org.digitalmodular.qoiflow.QoiStatistics;
import static org.digitalmodular.util.Validators.requireRange;

/**
 * @author Mark Jeronimus
 */
// Created 2022-06-05
public abstract class QoiInstruction {
	/**
	 * The number of bits to encode each component with.
	 * <p>
	 * Only bitsA may be 0.
	 */
	protected final int bitsR;
	protected final int bitsG;
	protected final int bitsB;
	protected final int bitsA;

	protected final int numBits;

	protected int codeOffset          = 0;
	protected int calculatedCodeCount = 1;

	protected QoiStatistics statistics = null;

	protected QoiInstruction(int bitsR, int bitsG, int bitsB, int bitsA) {
		this.bitsR = requireRange(1, 8, bitsR, "bitsR");
		this.bitsG = requireRange(1, 8, bitsG, "bitsG");
		this.bitsB = requireRange(1, 8, bitsB, "bitsB");
		this.bitsA = requireRange(0, 8, bitsA, "bitsA");

		numBits = bitsA + bitsB + bitsG + bitsR;
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
	 * This is only valid when {@link QoiFlowStreamCodec#reset()} has been called on the codec.
	 * <p>
	 * When this is not a variable-length instruction, the value will then equal {@link #getNumCodes()}.
	 */
	public int getCalculatedCodeCount() {
		return calculatedCodeCount;
	}

	public boolean hasAlpha() {
		return bitsA > 0;
	}

	/**
	 * Sets or clears the object to track statistics with.
	 * <p>
	 * Normally this should not be called by the user, as {@link QoiFlowStreamCodec#setStatistics(QoiStatistics)}
	 * should be called instead.
	 */
	public void setStatistics(QoiStatistics statistics) {
		this.statistics = statistics;
	}

	public QoiStatistics getStatistics() {
		return statistics;
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

	/**
	 * Decodes the pixel .
	 * <p>
	 * The first instruction that can encode the pixel will end the encoding phase.
	 *
	 * @return The amount of bytes this instruction stored in {@code dst} encode the color,
	 * or -1 if it could not encode.
	 */
	public abstract QoiColorRun decode(int code, ByteBuffer src, QoiColor lastColor);

	/**
	 * Give the instruction the opportunity to update it's internal state depending on the decoded color.
	 * <p>
	 * This is required, for example, for Color History, to record a color not decoded by itself.
	 * <p>
	 * Does nothing unless overridden.
	 */
	public void postDecode(QoiColor color) {
	}

	/**
	 * Clarifies whether this instruction must be capable of emitting identical bytes in sequence,
	 * either in one pass, or in multiple passes.
	 * <p>
	 * For example, if we have the sequence {@code 0x80 0x80},
	 * then this instruction can repeat bytes if either of these is true:
	 * <ul><li>This sequence encodes two complete datagrams of this instruction</li>
	 * <li>This sequence can be the start of a datagram, or the entire datagram, of this instruction</li></ul>
	 */
	public abstract boolean canRepeatBytes();
}
