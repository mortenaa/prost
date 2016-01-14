package no.imr.barents.prost.recruitment;

import no.imr.barents.prost.io.InputReader;
import no.imr.barents.prost.simulation.RandomGenerator;
import no.imr.barents.prost.model.StockModel;

public abstract class RecruitmentGenerator {
	private double trunk;

	private double sd;
	
	private double bias;

	private int errortype;

	// Static factory method
	public static RecruitmentGenerator createFromFile(InputReader in) {
		String s;
		RecruitmentGenerator rec = null;
		//in.expectWord("[RecruitmentGenerator]");
		String type = in.expectString("type").toLowerCase();
		if (type.equals("fixed"))
			rec = new FixedRecruitmentGenerator();
		else if (type.equals("bevertonholt"))
			rec = new BevertonHoltRecruitmentGenerator();
		else if (type.equals("bevertonholt-cyclic"))
		  	rec = new BevertonHoltCyclicRecruitmentGenerator();
		else if (type.equals("ricker"))
			rec = new RickerRecruitmentGenerator();
		else if (type.equals("ockham"))
			rec = new OckhamRecruitmentGenerator();
		else if (type.equals("ockham-cyclic"))
			rec = new OckhamCyclicRecruitmentGenerator();
        else if (type.equals("haddock-cyclic"))
            rec = new HaddockRecruitmentGenerator();
		else
			in.error("No such RecruitmentGenerator type, " + type);
		rec.readInput(in);
        if (!type.equals("haddock-cyclic"))
            rec.readError(in);
		return rec;
	}

	public void readError(InputReader in) {
		String[] errortypes = { "normal", "lognormal", "none" };
		in.expectWord("error");
		errortype = in.expectWord(errortypes);
		if (errortype == 2)
			return;
		sd = in.expectDouble("cv");
		bias = in.expectOptionalDouble("bias", 0.0);
		trunk = in.expectDouble("trunk");
	}

	public double addError(double value) {
		if (errortype == 2)
			return value;
		//value*=(1+bias);
		double eps = trunk * sd * 2; // abs(eps) > trunk initially
		while (Math.abs(eps) > trunk * sd)
			eps = RandomGenerator.nextNormal(0.0, sd);
		if (errortype == 0)
			return value + eps * value + bias * value;
		else if (errortype == 1)
			return value * Math.exp(eps)+value*bias;
		return 0;
	} // values[i]=expected[i]+eps*expected[i]+bias[i]*expected[i];

	public abstract double generate(StockModel s, int y);

	abstract void readInput(InputReader in);

	public void reset() {
	};
}
