package org.digitalmodular.qoiflow;

/**
 * @author Mark Jeronimus
 */
// Created 2022-06-05
public record QoiColor(int r,
                       int g,
                       int b,
                       int a) {
	public QoiColor(int r, int g, int b, int a) {
		this.r = r & 0xFF;
		this.g = g & 0xFF;
		this.b = b & 0xFF;
		this.a = a & 0xFF;
	}
}
