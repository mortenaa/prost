package no.imr.barents.prost.simulation;

import no.imr.barents.prost.io.InputReader;

public class MultivariateDistortion extends Distortion {
	double[][] covariance;

	public void readFromFile(InputReader in) {
		covariance = in.expectMatrix("covariance", size, size);
	}

	public double[] generate() {
		return RandomGenerator.nextMultivariate(expected, covariance);
	}
}
