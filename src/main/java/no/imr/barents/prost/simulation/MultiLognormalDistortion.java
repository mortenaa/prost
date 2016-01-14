package no.imr.barents.prost.simulation;

import no.imr.barents.prost.io.InputReader;

public class MultiLognormalDistortion extends Distortion {
	double[][] covariance;

	public void readFromFile(InputReader in) {
		covariance = in.expectMatrix("covariance", size, size);
	}

	public double[] generate() {
		return RandomGenerator.nextMultiLognormal(expected, covariance);
	}
}
