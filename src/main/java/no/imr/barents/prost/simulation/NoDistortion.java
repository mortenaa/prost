package no.imr.barents.prost.simulation;

import no.imr.barents.prost.io.InputReader;

public class NoDistortion extends Distortion {
	public NoDistortion() {
	};

	public void readFromFile(InputReader in) {
		return;
	}

	public double[] generate() {
		//This was a bug. return reference to array, which then was changed outside
		//this class.
		//return expected;
		//fix: return copy of the array
		double[] values = new double[expected.length];
		for (int i = 0; i < values.length; i++) {
			values[i]=expected[i];
		}
		return values;
	}
}
