package org.digitalmodular.qoiflow.instruction;

import java.nio.ByteBuffer;

import org.digitalmodular.qoiflow.QoiFlowColor;
import org.digitalmodular.qoiflow.QoiFlowColorDelta;
import org.digitalmodular.qoiflow.QoiFlowColorRun;
import org.digitalmodular.qoiflow.QoiFlowPixelData;

/**
 * @author Mark Jeronimus
 */
// Created 2022-06-15
public class QoiFlowInstructionMaskRGBA extends QoiFlowInstruction {
	public QoiFlowInstructionMaskRGBA(boolean hasAlpha) {
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
	public int encode(QoiFlowPixelData pixel, byte[] dst) {
		QoiFlowColorDelta delta = pixel.getDelta();
		if ((bitsA == 0) && delta.da() != 0) {
			return -1;
		}

		QoiFlowColor color = pixel.getColor();

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
			logStatistics(dst, mask, numBytes, color);
		}

		return numBytes;
	}

	@Override
	public QoiFlowColorRun decode(int code, ByteBuffer src, QoiFlowColor lastColor) {
		int mask;
		if (bitsA > 0) {
			mask = (code - codeOffset) & 0b1111;
		} else {
			mask = ((code - codeOffset) & 0b111) << 1;
		}

		int r;
		int g;
		int b;
		int a;

		if ((mask & 0b1000) != 0) {
			r = src.get() & 0xFF;
		} else {
			r = lastColor.r();
		}
		if ((mask & 0b0100) != 0) {
			g = src.get() & 0xFF;
		} else {
			g = lastColor.g();
		}
		if ((mask & 0b0010) != 0) {
			b = src.get() & 0xFF;
		} else {
			b = lastColor.b();
		}
		if ((mask & 0b0001) != 0) {
			a = src.get() & 0xFF;
		} else {
			a = lastColor.a();
		}

		QoiFlowColor color = new QoiFlowColor(r, g, b, a);

		if (statistics != null) {
			int numBytes = Integer.bitCount(mask) + 1;
			logStatistics(src, mask, numBytes, color);
		}

		return new QoiFlowColorRun(color, 1);
	}

	@Override
	public boolean canRepeatBytes() {
		return true;
	}

	@Override
	public String toString() {
		return "MASK";
	}

	private void logStatistics(ByteBuffer src, int mask, int numBytes, QoiFlowColor color) {
		logStatistics(src.array(), mask, src.position() - numBytes, numBytes, color);
	}

	private void logStatistics(byte[] dst, int mask, int numBytes, QoiFlowColor color) {
		logStatistics(dst, mask, 0, numBytes, color);
	}

	private void logStatistics(byte[] dst, int mask, int start, int numBytes, QoiFlowColor color) {
		switch (numBytes) {
			case 2:
				statistics.recordMask(this,
				                      dst,
				                      start,
				                      2,
				                      mask,
				                      color,
				                      dst[start + 1] & 0xFF);
				break;
			case 3:
				statistics.recordMask(this,
				                      dst,
				                      start,
				                      3,
				                      mask,
				                      color,
				                      dst[start + 1] & 0xFF,
				                      dst[start + 2] & 0xFF);
				break;
			case 4:
				statistics.recordMask(this,
				                      dst,
				                      start,
				                      4,
				                      mask,
				                      color,
				                      dst[start + 1] & 0xFF,
				                      dst[start + 2] & 0xFF,
				                      dst[start + 3] & 0xFF);
				break;
			case 5:
				statistics.recordMask(this,
				                      dst,
				                      start,
				                      5,
				                      mask,
				                      color,
				                      dst[start + 1] & 0xFF,
				                      dst[start + 2] & 0xFF,
				                      dst[start + 3] & 0xFF,
				                      dst[start + 4] & 0xFF);
				break;
			default:
				throw new AssertionError("Invalid numBytes: " + numBytes);
		}
	}
}
