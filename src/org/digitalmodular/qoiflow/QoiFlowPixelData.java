package org.digitalmodular.qoiflow;

/**
 * @author Mark Jeronimus
 */
// Created 2022-06-09
public class QoiFlowPixelData {
	private final QoiFlowColor       previous;
	private final QoiFlowColor       color;
	private final QoiFlowColorDelta  delta;
	private final QoiFlowColorChroma chroma;

	public QoiFlowPixelData(QoiFlowColor previous, QoiFlowColor color) {
		this.previous = previous;
		this.color = color;
		delta = QoiFlowColorDelta.fromColors(previous, color);
		chroma = QoiFlowColorChroma.fromColors(previous, color);
	}

	public QoiFlowColor getPrevious() {
		return previous;
	}

	public QoiFlowColor getColor() {
		return color;
	}

	public QoiFlowColorDelta getDelta() {
		return delta;
	}

	public QoiFlowColorChroma getChroma() {
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
