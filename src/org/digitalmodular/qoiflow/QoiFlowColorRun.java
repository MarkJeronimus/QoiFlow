package org.digitalmodular.qoiflow;

import java.util.Objects;

import static org.digitalmodular.util.Validators.requireAtLeast;

/**
 * @author Mark Jeronimus
 */
// Created 2022-06-05
public record QoiFlowColorRun(QoiFlowColor color,
                              int count) {
	public QoiFlowColorRun(QoiFlowColor color, int count) {
		this.color = Objects.requireNonNull(color, "'color' can't be null");
		this.count = requireAtLeast(1, count, "count");
	}
}
