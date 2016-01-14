package no.imr.barents.prost;

class TacRule extends ManagementRule {
	private static int NO_RULE = 0;

	private static int MAXINC_RULE = 1;

	private static int MAXDEC_RULE = 2;

	private static int BPA_RULE = 3;

	private static String[] RULES = { " - ", "Inc", "Dec", "Bpa" };

	private double[] inTAC, C, S, fHistory;

	private int[] rules;

	private int firstyear, minage, ages, favemin, favemax, faveages, years;

	private int realyear, invokedRule;

	private double fmax, flevel, TAC, SSB, fave;

	public double[] generate(Stock s, int year) {
		C = new double[ages];
		TAC = inTAC[year + 1];
		realyear = s.realYear() + year;
		invokedRule = NO_RULE;
		double[] M = s.getM(year + 1);
		double[] Sw = s.getSw(year + 1);
		double[] Cw = s.getCw(year + 1);
		double[] Mat = s.getMat(year + 1);
		double[] N = Functions.applyMortality(s.getF(year), s.getN(year), s
				.getM(year), s.getRec(year + 1));
		SSB = 0.0;
		for (int a = 0; a < ages; a++)
			SSB += N[a] * Sw[a] * Mat[a];
		double tmpf = Functions.findFlevel(N, S, M, Cw, TAC);
		double fsum = 0;
		for (int a = favemin; a <= favemax; a++)
			fsum += tmpf * S[a];
		flevel = fsum / faveages;
		if (flevel > fmax) {
			System.out.println("Warning: high F");
			fsum = 0;
			for (int i = favemin; i <= favemax; i++)
				fsum += S[i];
			tmpf = fmax * faveages / fsum;
		}
		calcCatch(S, tmpf, N, M, Cw);
		fHistory[year + 1] = fave;
		rules[year + 1] = invokedRule;
		System.out.println(C[4]);
		return C;
	}

	public void calcCatch(double[] S, double f, double[] N, double[] M,
			double[] Cw) {
		for (int i = 0; i < ages; i++)
			C[i] = Functions.fToCatch(S[i] * f, N[i], M[i]);
		TAC = 0;
		for (int a = 0; a < ages; a++)
			TAC += C[a] * Cw[a];
		fave = 0;
		for (int a = favemin; a <= favemax; a++)
			fave += (S[a] * f);
		fave /= faveages;
	}

	public int[] getRules() {
		return rules;
	}

	public double[] getF() {
		return fHistory;
	}
    
    public double getTAC() {
        return TAC;
    }

	public void print(OutputWriter out) {
		out.print((realyear + 1) + ", " + TAC + ", " + SSB + ", ");
		out.println(fave + ", " + RULES[invokedRule]);
	}

	public void printHeader(OutputWriter out) {
		out.println("Year, Quota, SSB, Fave, rules");
	}

	public void readFromFile(InputReader in, Stock s) {
		minage = s.getMinAge();
		ages = s.ages();
		favemin = s.getFbarMin();
		favemax = s.getFbarMax();
		years = s.years();
		int realyear = s.realYear();
		firstyear = s.firstyear();
		S = in.expectVector("Selection", ages);
		fmax = in.expectDouble("Fmax");
		faveages = favemax - favemin + 1;
		fHistory = new double[years];
		rules = new int[years];
		inTAC = new double[years];
		in.expectWord("Tac");
		for (int y = firstyear + 1; y < years - minage; y++) {
			inTAC[y] = in.expectDouble(Integer.toString(y + realyear));
		}
	}

}
