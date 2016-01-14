package no.imr.barents.prost.recruitment;

import no.imr.barents.prost.io.InputReader;
import no.imr.barents.prost.model.StockModel;

public class RickerRecruitmentGenerator extends RecruitmentGenerator {
	double a;
	double b;
    double cutoff;

	void readInput(InputReader in) {
		a = in.expectDouble("a");
		b = in.expectDouble("b");
        cutoff = in.expectOptionalDouble("ssb-cutoff",-1);
	}

	public double generate(StockModel s, int y) {
		double ssb = s.getSSB(y);
        if(cutoff>=0) {
            ssb=Math.min(cutoff,ssb);
        }
		return addError((a * ssb) * Math.exp(-b * ssb));
	}
}

