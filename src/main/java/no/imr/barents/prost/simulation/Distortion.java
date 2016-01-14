package no.imr.barents.prost.simulation;

import no.imr.barents.prost.io.InputReader;

public abstract class Distortion {
	// Distortion interface, must be implemented in subclasses

	public abstract double[] generate();

	public abstract void readFromFile(InputReader in);

	// Common implementation

	double[] expected;

	int size;

	public final static Distortion createFromFile(InputReader in, int size) {
		String type = in.expectString("distortion").toLowerCase();
		Distortion dist = null;
		if (type.equals("none"))
			dist = new NoDistortion();
		else if (type.equals("normal"))
			dist = new NormalDistortion();
		else if (type.equals("multivariate"))
			dist = new MultivariateDistortion();
		else
			in.error("No such Distortion type, " + type);
		dist.size = size;
		dist.readFromFile(in);
		return dist;
	}

	public void setExpected(double[] expected) {
		this.expected = expected;
	}
}

