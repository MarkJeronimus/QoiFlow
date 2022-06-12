package org.digitalmodular.qoiflow;

/**
 * @author Mark Jeronimus
 */
// Created 2022-06-09
public class QoiPixelData {
	private final QoiColor       previous;
	private final QoiColor       color;
	private final QoiColorDelta  delta;
	private final QoiColorChroma chroma;

	public QoiPixelData(QoiColor previous, QoiColor color) {
		this.previous = previous;
		this.color = color;
		delta = QoiColorDelta.fromColors(previous, color);
		chroma = QoiColorChroma.fromColors(previous, color);
	}

	public QoiColor getPrevious() {
		return previous;
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

	@Override
	public String toString() {
		return "QoiPixelData(previous=" + previous +
		       ", color=" + color +
		       ", delta=" + delta +
		       ", chroma=" + chroma + ')';
	}
}
