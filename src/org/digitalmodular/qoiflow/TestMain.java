package org.digitalmodular.qoiflow;

import java.util.concurrent.ThreadLocalRandom;

/**
 * @author Mark Jeronimus
 */
// Created 2022-06-05
public class TestMain {
	public static void main(String... args) {
//		QoiInstruction rgba = new QoiInstructionRGBA(8, 8, 8, 0);
//		QoiInstruction hist1 = new QoiInstructionHistory(1, 1, 1, 0);
//		QoiInstruction hist2 = new QoiInstructionHistory(1, 1, 1, 0);
//		QoiInstruction hist3 = new QoiInstructionHistory(1, 1, 1, 0);
//		QoiInstruction hist4 = new QoiInstructionHistory(1, 1, 1, 0);
//
//		QoiFlowCodec enc = new QoiFlowCodec(Arrays.asList(hist1, hist2, rgba, hist3, hist4));
//		enc.setVariableLength(0, 6);
//		enc.setVariableLength(1, 10);
//		enc.setVariableLength(2, 100);
//
//		enc.reset();

		for (int db = -128; db < 128; db++) {
			for (int dg = -128; dg < 128; dg++) {
				for (int dr = -128; dr < 128; dr++) {
					QoiColor color1 = new QoiColor(ThreadLocalRandom.current().nextInt(256),
					                               ThreadLocalRandom.current().nextInt(256),
					                               ThreadLocalRandom.current().nextInt(256),
					                               0);
					QoiColor       color2 = new QoiColor(color1.r() + dr, color1.r() + dg, color1.r() + db, 0);

					QoiColorDelta  delta  = QoiColorDelta.fromColors(color1, color2);
					QoiColorChroma chroma = QoiColorChroma.fromColors(color1, color2);

					QoiColor color2d = delta.applyTo(color1);
					QoiColor color2c = chroma.applyTo(color1);

					if (!color2.equals(color2d)) {
						System.out.printf(
								"Delta error | %4d %4d %4d | %4d %4d %4d | %4d %4d %4d | %4d %4d %4d\n",
								(color1.r() + dr) & 0xFF, (color1.g() + dg) & 0xFF, (color1.b() + db) & 0xFF,
								(color2.r() + dr) & 0xFF, (color2.g() + dg) & 0xFF, (color2.b() + db) & 0xFF,
								delta.deltaR(), delta.deltaG(), delta.deltaB(),
								(color2d.r() + dr) & 0xFF, (color2d.g() + dg) & 0xFF, (color2d.b() + db) & 0xFF);
					}
					if (!color2.equals(color2c)) {
						System.out.printf(
								"Delta error | %4d %4d %4d | %4d %4d %4d | %4d %4d %4d | %4d %4d %4d\n",
								(color1.r() + dr) & 0xFF, (color1.g() + dg) & 0xFF, (color1.b() + db) & 0xFF,
								(color2.r() + dr) & 0xFF, (color2.g() + dg) & 0xFF, (color2.b() + db) & 0xFF,
								chroma.chromaY(), chroma.chromaCB(), chroma.chromaCR(),
								(color2c.r() + dr) & 0xFF, (color2c.g() + dg) & 0xFF, (color2c.b() + db) & 0xFF);
					}
					Thread.yield();
				}
			}
		}
	}
}
