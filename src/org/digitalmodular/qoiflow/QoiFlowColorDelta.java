package org.digitalmodular.qoiflow;

/**
 * Color difference encoding in RGB color space.
 *
 * @author Mark Jeronimus
 */
// Created 2022-06-06
public record QoiFlowColorDelta(int dr,
                                int dg,
                                int db,
                                int da) {
	public QoiFlowColorDelta(int dr, int dg, int db, int da) {
		this.dr = (byte)dr;
		this.dg = (byte)dg;
		this.db = (byte)db;
		this.da = (byte)da;
	}

	public static QoiFlowColorDelta fromColors(QoiFlowColor previous, QoiFlowColor current) {
		int dr = current.r() - previous.r();
		int dg = current.g() - previous.g();
		int db = current.b() - previous.b();
		int da = current.a() - previous.a();

		return new QoiFlowColorDelta(dr, dg, db, da);
	}

	public QoiFlowColor applyTo(QoiFlowColor color) {
		int r = color.r() + dr;
		int g = color.g() + dg;
		int b = color.b() + db;
		int a = color.a() + da;

		return new QoiFlowColor(r, g, b, a);
	}
}
