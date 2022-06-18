package org.digitalmodular.qoiflow.instruction;

import java.nio.ByteBuffer;

import org.digitalmodular.qoiflow.QoiColor;
import org.digitalmodular.qoiflow.QoiPixelData;

/**
 * @author Mark Jeronimus
 */
// Created 2022-06-05
public class QoiInstructionRGBA extends QoiInstruction {
	/**
	 * The amount to move the MSB bits from position 32 to the position in the datagram.
	 * <p>
	 * Examples for RGBA5654:
	 * <pre>
	 * shiftR = 12; // i.e. 0b11111000_00000000_00000000_00000000 -> 0b00000000_00001111_10000000_00000000
	 * shiftG = 17; // i.e. 0b11111100_00000000_00000000_00000000 -> 0b00000000_00000000_01111110_00000000
	 * shiftB = 23; // i.e. 0b11111000_00000000_00000000_00000000 -> 0b00000000_00000000_00000001_11110000
	 * shiftA = 28; // i.e. 0b11110000_00000000_00000000_00000000 -> 0b00000000_00000000_00000000_00001111
	 * </pre>
	 * Alternatively, think of this as the number of 0-bits to the left of the 1-bits in the corresponding mask.
	 */
	private final int shiftR;
	private final int shiftG;
	private final int shiftB;
	private final int shiftA;

	/**
	 * The occupied bits for each component in the datagram.
	 * <p>
	 * Examples for RGBA5654:
	 * <pre>
	 * maskR = 0b00000000_00001111_10000000_00000000;
	 * maskG = 0b00000000_00000000_01111110_00000000;
	 * maskB = 0b00000000_00000000_00000001_11110000;
	 * maskA = 0b00000000_00000000_00000000_00001111;
	 * </pre>
	 * Alternatively, think of this as the number of 0-bits to the right of the 1-bits in the corresponding mask.
	 */
	protected final int maskR;
	protected final int maskG;
	protected final int maskB;
	protected final int maskA;

	private final int numBytes;

	public QoiInstructionRGBA(int bitsR, int bitsG, int bitsB, int bitsA) {
		super(bitsR, bitsG, bitsB, bitsA);

		shiftA = 32 - bitsA;
		shiftB = shiftA - bitsB;
		shiftG = shiftB - bitsG;
		shiftR = shiftG - bitsR;

		maskR = (1 << (32 - shiftR)) - (1 << (32 - shiftG));
		maskG = (1 << (32 - shiftG)) - (1 << (32 - shiftB));
		maskB = (1 << (32 - shiftB)) - (1 << (32 - shiftA));
		maskA = (1 << (32 - shiftA)) - 1;

		//noinspection OverridableMethodCallDuringObjectConstruction
		numBytes = getMaxSize();
	}

	@Override
	public int encode(QoiPixelData pixel, byte[] dst) {
		if ((bitsA == 0) && pixel.getDelta().da() != 0) {
			return -1;
		}

		QoiColor color = pixel.getColor();

		int r = ((color.r() << 24) >>> shiftR) & maskR;
		int g = ((color.g() << 24) >>> shiftG) & maskG;
		int b = ((color.b() << 24) >>> shiftB) & maskB;
		int a = ((color.a() << 24) >>> shiftA) & maskA;

		int recoveredR = r << shiftR >>> 24;
		int recoveredG = g << shiftG >>> 24;
		int recoveredB = b << shiftB >>> 24;
		int recoveredA = a << shiftA >>> 24;
		if (bitsA > 0) {
			if (recoveredR != color.r() ||
			    recoveredG != color.g() ||
			    recoveredB != color.b() ||
			    recoveredA != color.a()) {
				return -1;
			}
		} else {
			if (recoveredR != color.r() ||
			    recoveredG != color.g() ||
			    recoveredB != color.b()) {
				return -1;
			}
		}

		int rgba = r | g | b | a;

		for (int i = 0; i < numBytes; i++) {
			int shift = 8 * (numBytes - i - 1);
			if (shift < 32)
				dst[i] = (byte)(rgba >> shift);
			else
				dst[i] = 0;
		}

		dst[0] += codeOffset;

		if (statistics != null) {
			if (bitsA > 0) {
				statistics.record(this, dst, 0, numBytes, recoveredR, recoveredG, recoveredB, recoveredA);
			} else {
				statistics.record(this, dst, 0, numBytes, recoveredR, recoveredG, recoveredB);
			}
		}

		return numBytes;
	}

	@Override
	public void decode(ByteBuffer src, QoiColor color) {
	}

	@Override
	public boolean canRepeatBytes() {
		return true;
	}

	@SuppressWarnings("StringConcatenationMissingWhitespace")
	@Override
	public String toString() {
		if (bitsA > 0) {
			return "RGBA" + bitsR + bitsR + bitsR + bitsA;
		} else {
			return "RGB" + bitsR + bitsR + bitsR;
		}
	}
}
