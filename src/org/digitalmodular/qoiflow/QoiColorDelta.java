package org.digitalmodular.qoiflow;

/**
 * Color difference encoding in RGB color space.
 *
 * @author Mark Jeronimus
 */
// Created 2022-06-06
public record QoiColorDelta(int deltaR,
                            int deltaG,
                            int deltaB,
                            int deltaA) {
	public QoiColorDelta(int deltaR, int deltaG, int deltaB, int deltaA) {
		this.deltaR = (byte)deltaR;
		this.deltaG = (byte)deltaG;
		this.deltaB = (byte)deltaB;
		this.deltaA = (byte)deltaA;
	}

	public static QoiColorDelta fromColors(QoiColor previous, QoiColor current) {
		int dr = current.r() - previous.r();
		int dg = current.g() - previous.g();
		int db = current.b() - previous.b();
		int da = current.a() - previous.a();

		return new QoiColorDelta(dr, dg, db, da);
	}

	public QoiColor applyTo(QoiColor color) {
		int r = color.r() + deltaR;
		int g = color.g() + deltaG;
		int b = color.b() + deltaB;
		int a = color.a() + deltaA;

		return new QoiColor(r, g, b, a);
	}
}
