package no.imr.barents.prost;

class BevertonHoltRecruitmentGenerator extends RecruitmentGenerator {
	double a;

	double b;

	void readInput(InputReader in) {
		a = in.expectDouble("a");
		b = in.expectDouble("b");
	}

	public double generate(StockModel s, int y) {
		double ssb = s.getSSB(y);
		return addError((a * ssb) / (b + ssb));
	}
}

class BevertonHoltCyclicRecruitmentGenerator extends RecruitmentGenerator {
	double a;
	double b;
	double amp, t, f, k, w;

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
	    rec = (a * ssb) / (b + ssb);
		double sinpart = Math.sin(2 * Math.PI * (year - 1946 + f) / t);
		return addError(rec * Math.exp(amp * sinpart + k * (mw - w)));
	}

}