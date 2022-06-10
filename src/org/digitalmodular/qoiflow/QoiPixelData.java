package org.digitalmodular.qoiflow;

/**
 * @author Mark Jeronimus
 */
// Created 2022-06-09
public class QoiPixelData {
	private final QoiColor       color;
	private final QoiColorDelta  delta;
	private final QoiColorChroma chroma;

	public QoiPixelData(QoiColor previous, QoiColor current) {
		color = current;
		delta = QoiColorDelta.fromColors(previous, current);
		chroma = QoiColorChroma.fromColors(previous, current);
	}

	public QoiColor getColor() {
		return color;
	}

	public QoiColorDelta getDelta() {
		return delta;
	}

	public QoiColorChroma getChroma() {
		return chroma;
	}
}
