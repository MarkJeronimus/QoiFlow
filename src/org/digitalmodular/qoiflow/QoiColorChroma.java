package org.digitalmodular.qoiflow;

/**
 * Color difference encoding in Strutz' A2 color space.
 * <p>
 * From: <a href="https://zenodo.org/record/52607">
 * Adaptive Selection of Colour Transformations for Reversible Image Compression (Tilo Strutz)</a>
 * <pre>
 * [Y]    [0  1  0] [R]
 * [CB] = [1 -1  0] [G]
 * [CR]   [0 -1  1] [B]
 * </pre>
 * This can be efficiently sequenced as:
 * <pre>
 *     Y  = G
 *     CB = B - G
 *     CR = R - G
 * </pre>
 * This is the only non-trivial color space transform that I could find in literature that is not only lossless
 * and dynamic-range preserving (<em>i.e.</em> same bits out as bits in), but also <em>localized</em>.
 * The latter means that small differences in R, G and/or B are guaranteed
 * to result in small differences in GY, GR and/or GB.
 * For differences exceeding half of the encoding range (128 for 8-bits) this guarantee falls apart.
 * As a counter-example, YCoCb24-R doesn't have this property.
 * YCoCb-R does have this property, but instead Co and Cb both require
 * one extra bit to encode, so it isn't dynamic-range preserving.
 *
 * @author Mark Jeronimus
 */
// Created 2022-06-06
public record QoiColorChroma(int chromaY,
                             int chromaCB,
                             int chromaCR,
                             int chromaA) {
	public QoiColorChroma(int chromaY, int chromaCB, int chromaCR, int chromaA) {
		this.chromaY = (byte)chromaY;
		this.chromaCB = (byte)chromaCB;
		this.chromaCR = (byte)chromaCR;
		this.chromaA = (byte)chromaA;
	}

	public static QoiColorChroma fromColors(QoiColor previous, QoiColor current) {
		int dr = current.r() - previous.r();
		int dg = current.g() - previous.g();
		int db = current.b() - previous.b();
		int da = current.a() - previous.a();

		return new QoiColorChroma(dg, db - dg, dr - dg, da);
	}

	public QoiColor applyTo(QoiColor color) {
		int r = color.r() + chromaY + chromaCR;
		int g = color.g() + chromaY;
		int b = color.b() + chromaY + chromaCB;
		int a = color.a() + chromaA;

		return new QoiColor(r, g, b, a);
	}
}
