package no.imr.barents.prost;

class FixedRecruitmentGenerator extends RecruitmentGenerator {
	int years;

	double[] recruits;

	int t = -1;

	void readInput(InputReader in) {
		years = in.expectInt("years");
		recruits = in.expectVector("numbers", years);
	}

	public double generate(StockModel s, int y) {
		t++;
		return addError(recruits[t]);
	}

	public void reset() {
		t = -1;
	}
}
