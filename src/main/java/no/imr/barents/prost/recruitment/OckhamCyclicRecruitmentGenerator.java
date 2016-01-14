package no.imr.barents.prost.recruitment;

import no.imr.barents.prost.io.InputReader;
import no.imr.barents.prost.model.StockModel;

public class OckhamCyclicRecruitmentGenerator extends RecruitmentGenerator {
	double a;

	double b;

	double amp, t, f, k, w;

	String[] errortypes = { "normal", "lognormal" };

	void readInput(InputReader in) {
		a = in.expectDouble("a");
		b = in.expectDouble("b");
		amp = in.expectDouble("amplitude");
		t = in.expectDouble("period");
		f = in.expectDouble("phase");
		k = in.expectDouble("k");
		w = in.expectDouble("w");
	}

	public double generate(StockModel s, int y) {
		double ssb = s.getSSB(y);
		double rec = 0;
		int year = y + s.realYear();
		double mw = s.getSSMeanWeight(y);
		if (ssb >= b)
			rec = a;
		else
			rec = a * ssb / b;
		double sinpart = Math.sin(2 * Math.PI * (year - 1946 + f) / t);
		return addError(rec * Math.exp(amp * sinpart + k * (mw - w)));
	}

}
