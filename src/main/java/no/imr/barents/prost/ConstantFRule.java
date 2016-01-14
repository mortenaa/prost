package no.imr.barents.prost;

class ConstantFRule extends ManagementRule {

	private int minage;

	private int ages;

	private int favemin;

	private int favemax;

	private int faveages;

	private double[] S;

	private double bpa;

	private double blim;

	private double maxinc;

	private double maxdec;

	private double inputTAC,maxTAC;

	private double prevTAC;

	private double fave;

	private double TAC;

	private boolean doFirstYearMaxChange = false;

	private boolean firstyear = true;

	private AdjustF lowFRule;

	private int invokedRule;

	private double flevel, targetf;

	private int years;

	private double SSB;

	private double prevSSB;

	private double[] C;

	private double[] fHistory;

	private int[] rules;

	private int realyear;

	private static int NO_RULE = 0;

	private static int MAXINC_RULE = 1;

	private static int MAXDEC_RULE = 2;

	private static int BPA_RULE = 3;

	private static String[] RULES = { " - ", "Inc", "Dec", "Bpa" };
	
	  private double lowrec=0;
	  private int lowYears=0;
	  private double fReduction=0;
	  private double[] historicRec;
	  private boolean doLowRec=false;
	  private double[] lowRec;

	private double fmin;

	public void print(OutputWriter out) {
		out.print((realyear + 1) + ", " + TAC + ", " + SSB + ", ");
		out.println(fave + ", " + RULES[invokedRule]);
	}

	public void printHeader(OutputWriter out) {
		out.println("Year, Quota, SSB, Fave, rules");
	}
    
    public double getTAC() {
        return TAC;
    }

	public double[] generate(Stock s, int year) {
		C = new double[ages];
		realyear = s.realYear() + year;
		invokedRule = NO_RULE;
		double[] M = s.getM(year + 1);
		double[] Sw = s.getSw(year + 1);
		double[] Cw = s.getCw(year + 1);
		double[] Mat = s.getMat(year + 1);
		flevel = targetf;
		double[] N0 = new double[ages];
	    System.arraycopy(s.getN(year), 0, N0, 0, ages);
		//double[] N = Functions.applyMortality(s.getF(year), s.getN(year), s
		//		.getM(year), s.getRec(year + 1));
		double[] N = Functions.applyMortality(s.getF(year), N0, s
				.getM(year), s.getRec(year + 1));	
	    if (doLowRec) {
	    	lowRec[year-minage+lowYears-1]=N0[0];
	    }
		SSB = 0;
		for (int a = 0; a < ages; a++)
			SSB += N[a] * Sw[a] * Mat[a];
		if (SSB < bpa) {
			invokedRule = BPA_RULE;
			flevel = lowFRule.adjust(flevel, SSB);
			calcCatch(S, flevel, N, M, Cw);
		} else if ((!firstyear || doFirstYearMaxChange) && prevSSB > bpa) {

			calcCatch(S, flevel, N, M, Cw);
			double change = (TAC - prevTAC) / prevTAC * 100.0;
			if (change > maxinc) {
				TAC = prevTAC + prevTAC * maxinc / 100.0;
				double tmpf = Functions.findFlevel(N, S, M, Cw, TAC);
				double fsum = 0;
				for (int a = favemin; a <= favemax; a++)
					fsum += tmpf * S[a];
				flevel = fsum / faveages;
				calcCatch(S, flevel, N, M, Cw);
				invokedRule = MAXINC_RULE;
			} else if (change < -maxdec) {
				TAC = prevTAC - prevTAC * maxdec / 100.0;
				double tmpf = Functions.findFlevel(N, S, M, Cw, TAC);
				double fsum = 0;
				for (int a = favemin; a <= favemax; a++)
					fsum += tmpf * S[a];
				flevel = fsum / faveages;
				calcCatch(S, flevel, N, M, Cw);
				invokedRule = MAXDEC_RULE;
			} else {
				calcCatch(S, flevel, N, M, Cw);
			}
		} else {
			calcCatch(S, flevel, N, M, Cw);
		}
		
	    if (doLowRec && calcLowRec(year)<lowrec) {
	    	flevel=flevel*fReduction;
			calcCatch(S, flevel, N, M, Cw);
	    }
		
		if (maxTAC>-1 && TAC>maxTAC) {
			TAC=maxTAC;
			double tmpf = Functions.findFlevel(N, S, M, Cw, TAC);
			double fsum = 0;
			for (int a = favemin; a <= favemax; a++)
				fsum += tmpf * S[a];
			flevel = fsum / faveages;
			calcCatch(S, flevel, N, M, Cw);
		}
		
	    if (invokedRule!=BPA_RULE && flevel < fmin) {
	    	flevel = fmin;
			calcCatch(S, flevel, N, M, Cw);
	    }
		
		fHistory[year + 1] = fave;
		rules[year + 1] = invokedRule;
		prevTAC = TAC;
		firstyear = false;
		prevSSB = SSB;
		return C;
	}

	public void calcCatch(double[] S, double f, double[] N, double[] M,
			double[] Cw) {
		double fsum = 0;
		for (int i = favemin; i <= favemax; i++)
			fsum += S[i];
		double factor = f * faveages / fsum;
		for (int i = 0; i < ages; i++)
			C[i] = Functions.fToCatch(S[i] * factor, N[i], M[i]);
		TAC = 0;
		for (int a = 0; a < ages; a++)
			TAC += C[a] * Cw[a];
		fave = 0;
		for (int a = favemin; a <= favemax; a++)
			fave += (S[a] * factor);
		fave /= faveages;
	}

	public void reset() {
		prevSSB = bpa * 2;
		firstyear = true;
	}

	public int[] getRules() {
		return rules;
	}

	public double[] getF() {
		return fHistory;
	}
    
    public void setFAboveBpa(double f) {
        targetf=f;
    }

    private double calcLowRec(int y) {
  	  double rec=0.0;
  	  int i=y-minage+lowYears-1, n=0;
  	  while (n<lowYears) {
  		  rec+=lowRec[i];
  		  n++; i--;
  	  }
  	  rec=rec/lowYears;
  	  return rec;
    }
    
	public void readFromFile(InputReader in, Stock s) {
		minage = s.getMinAge();
		ages = s.ages();
		favemin = s.getFbarMin();
		favemax = s.getFbarMax();
		faveages = favemax - favemin + 1;
		S = in.expectVector("Selection", ages);
		targetf = in.expectDouble("FaboveBpa");
	    fmin = in.expectOptionalDouble("Fmin", 0.0);
		
	    doLowRec = in.expectOptionalKeyword("FlowRec");
	    if (doLowRec) {
	        lowrec = in.expectOptionalDouble("LowRec", 0);
	    	lowYears=in.expectInt("LowYears");
	    	fReduction=in.expectDouble("Freduction");
	    	historicRec=in.expectVector("HistoricRec", lowYears-1);
	        lowRec = new double[s.years()+lowYears];
	        for (int i=0; i<historicRec.length; i++) {
	        	lowRec[i]=historicRec[i];
	        }
	    }
		
		bpa = s.getBpa();
		blim = s.getBlim();
		prevSSB = bpa * 2;
		maxinc = in.expectDouble("Maxinc");
		maxdec = in.expectDouble("Maxdec");
		maxTAC = in.expectDouble("MaxTAC");
		inputTAC = in.expectDouble("FirstYearTAC");
		doFirstYearMaxChange = inputTAC > 0 ? true : false;
		String bparule = in.expectString("FbelowBpa").toLowerCase();
		years = s.years();
		if (bparule.equals("linear")) {
			double bzero = in.expectDouble("Bzero");
			if (bzero >= bpa)
				in.error("Error: Bzero must be less than Bpa");
			lowFRule = new LinearAdjust(bpa, bzero);
		} else if (bparule.equals("flat"))
			lowFRule = new NoAdjust();
		else if (bparule.equals("low")) {
			double lowf = in.expectDouble("Flow");
			lowFRule = new LowAdjust(lowf);
		} else
			in.error("No such bpa rule, " + bparule);
		fHistory = new double[years];
		rules = new int[years];
		
	    if (fmin > 0 && doLowRec) {
	    	System.out.println("\nWarning: You are using the Fmin option while also specifying a");
	    	System.out.println("         reduction in F at low recruitment.");
	    }
	}
}
