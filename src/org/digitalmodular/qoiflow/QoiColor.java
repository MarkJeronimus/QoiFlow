package org.digitalmodular.qoiflow;

/**
 * @author Mark Jeronimus
 */
// Created 2022-06-05
public class QoiColor {
	private int previousR = 0;
	private int previousG = 0;
	private int previousB = 0;
	private int previousA = 0;

	private int currentR;
	private int currentG;
	private int currentB;
	private int currentA;

	public QoiColor(int currentR, int currentG, int currentB, int currentA) {
		this.currentR = currentR;
		this.currentG = currentG;
		this.currentB = currentB;
		this.currentA = currentA;
	}

	public void updatePrevious() {
		previousR = currentR;
		previousG = currentG;
		previousB = currentB;
		previousA = currentA;
	}

	public int getCurrentR() {
		return currentR;
	}

	public int getCurrentG() {
		return currentG;
	}

	public int getCurrentB() {
		return currentB;
	}

	public int getCurrentA() {
		return currentA;
	}
}
