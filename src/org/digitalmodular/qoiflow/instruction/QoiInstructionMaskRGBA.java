package org.digitalmodular.qoiflow.instruction;

import java.nio.ByteBuffer;

import org.digitalmodular.qoiflow.QoiColor;
import org.digitalmodular.qoiflow.QoiColorDelta;
import org.digitalmodular.qoiflow.QoiPixelData;

/**
 * @author Mark Jeronimus
 */
// Created 2022-06-15
public class QoiInstructionMaskRGBA extends QoiInstruction {
	public QoiInstructionMaskRGBA(boolean hasAlpha) {
		super(8, 8, 8, hasAlpha ? 8 : 0);
	}

	@Override
	public int getNumCodes() {
		return bitsA > 0 ? 16 : 8;
	}

	@Override
	public int getMinSize() {
		return 2;
	}

	@Override
	public int getMaxSize() {
		return bitsA > 0 ? 5 : 4;
	}

	@Override
	public int encode(QoiPixelData pixel, byte[] dst) {
		QoiColorDelta delta = pixel.getDelta();
		if ((bitsA == 0) && delta.da() != 0) {
			return -1;
		}

		QoiColor color = pixel.getColor();

		int p        = 1;
		int mask     = 0b0000;
		int numBytes = 1;
		if (delta.dr() != 0) {
			dst[p++] = (byte)color.r();
			mask |= 0b1000;
			numBytes++;
		}
		if (delta.dg() != 0) {
			dst[p++] = (byte)color.g();
			mask |= 0b0100;
			numBytes++;
		}
		if (delta.db() != 0) {
			dst[p++] = (byte)color.b();
			mask |= 0b0010;
			numBytes++;
		}
		if (bitsA > 0) {
			if (delta.da() != 0) {
				dst[p] = (byte)color.a();
				mask |= 0b0001;
				numBytes++;
			}
		}

		if (mask == 0) {
			return -1;
		}

		if (bitsA > 0) {
			dst[0] = (byte)(codeOffset + mask);
		} else {
			dst[0] = (byte)(codeOffset + (mask >> 1));
		}

		if (statistics != null) {
			logStatistics(dst, mask, numBytes);
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

	@Override
	public String toString() {
		return "MASK";
	}

	private void logStatistics(byte[] dst, int mask, int numBytes) {
		switch (numBytes) {
			case 2:
				statistics.recordMask(this, dst, 0, 2, mask, dst[1] & 0xFF);
				break;
			case 3:
				statistics.recordMask(this, dst, 0, 3, mask, dst[1] & 0xFF, dst[2] & 0xFF);
				break;
			case 4:
				statistics.recordMask(this, dst, 0, 4, mask, dst[1] & 0xFF, dst[2] & 0xFF, dst[3] & 0xFF);
				break;
			case 5:
				statistics.recordMask(
						this, dst, 0, 5, mask, dst[1] & 0xFF, dst[2] & 0xFF, dst[3] & 0xFF, dst[4] & 0xFF);
				break;
			default:
				throw new AssertionError("Invalid numBytes: " + numBytes);
		}
	}
}
