package no.imr.barents.prost.recruitment;

import no.imr.barents.prost.io.InputReader;
import no.imr.barents.prost.model.StockModel;

public class BevertonHoltRecruitmentGenerator extends RecruitmentGenerator {
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

