package no.imr.barents.prost;

abstract class Distortion {
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

class NormalDistortion extends Distortion {
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
				eps=RandomGenerator.nextNormal(0.0, sd[i]);
			values[i]=expected[i]+eps*expected[i]+bias[i]*expected[i];
		}
		return values;
	}
}

class MultivariateDistortion extends Distortion {
	double[][] covariance;

	public void readFromFile(InputReader in) {
		covariance = in.expectMatrix("covariance", size, size);
	}

	public double[] generate() {
		return RandomGenerator.nextMultivariate(expected, covariance);
	}
}

class MultiLognormalDistortion extends Distortion {
	double[][] covariance;

	public void readFromFile(InputReader in) {
		covariance = in.expectMatrix("covariance", size, size);
	}

	public double[] generate() {
		return RandomGenerator.nextMultiLognormal(expected, covariance);
	}
}

class NoDistortion extends Distortion {
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
