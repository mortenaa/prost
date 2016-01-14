package no.imr.barents.prost.recruitment;

import no.imr.barents.prost.io.InputReader;
import no.imr.barents.prost.model.StockModel;

public class FixedRecruitmentGenerator extends RecruitmentGenerator {
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
