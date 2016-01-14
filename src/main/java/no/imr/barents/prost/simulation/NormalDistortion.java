package no.imr.barents.prost.simulation;

import no.imr.barents.prost.io.InputReader;

public class NormalDistortion extends Distortion {
	double[] sd;
	double[] bias;
	double trunk;

	public void readFromFile(InputReader in) {
		sd = in.expectVector("cv", size);
		bias = in.expectOptionalVector("bias", size, 0.0);
		trunk = in.expectDouble("trunk");
	}

/*	public double[] generate() {
		double[] values = new double[expected.length];
		for (int i = 0; i < values.length; i++)
				values[i] = RandomGenerator.nextNormal(expected[i], sd[i]
															* expected[i]);
		return values;
	} */

	/*double eps = trunk * sd * 2; // abs(eps) > trunk initially
	while (Math.abs(eps) > trunk * sd)
		eps = RandomGenerator.nextNormal(0.0, sd);
	if (errortype == 0)
		return value + eps * value; */

	public double[] generate() {
		double[] values = new double[expected.length];
		double eps;
		for (int i = 0; i < values.length; i++) {
			eps = trunk*sd[i]*2;
			while (Math.abs(eps)>trunk*sd[i])
				eps= RandomGenerator.nextNormal(0.0, sd[i]);
			values[i]=expected[i]+eps*expected[i]+bias[i]*expected[i];
		}
		return values;
	}
}
