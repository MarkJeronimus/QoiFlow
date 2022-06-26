package org.digitalmodular.qoiflow;

/**
 * @author Mark Jeronimus
 */
// Created 2022-06-24
public record QoiFlowComponentFormat(int bitsR,
                                     int bitsG,
                                     int bitsB,
                                     int bitsA) {
	@Override
	public String toString() {
		return "bits=" + bitsR + '/' + bitsG + '/' + bitsB + '/' + bitsA;
	}
}
