package no.imr.barents.prost.model;

import no.imr.barents.prost.io.InputReader;
import no.imr.barents.prost.io.OutputWriter;
import no.imr.barents.prost.Prost;
import no.imr.barents.prost.management.ConstantFRule;
import no.imr.barents.prost.management.ManagementRule;
import no.imr.barents.prost.management.ThreeYearRule;
import no.imr.barents.prost.recruitment.RecruitmentGenerator;
import no.imr.barents.prost.simulation.Distortion;
import no.imr.barents.prost.simulation.RandomGenerator;
import no.imr.barents.prost.util.Functions;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;

public class StockModel implements Stock {
	private int minage; // the real age of ageindex 0

	private int ages; // number of ages (last age is plusgroup)

	private int years; // number of years including pre/post years

	private int realyear; // the real year of yearindex 0

	private int firstyear; // index of assesment year

	private int lastyear; // index of first year after simulation

	private int favemin;

	private int favemax;

	private int faveages;

	private String stockname;

	private int lastrecruitedyear;

	private static int NO_RULE = 0;

	private static int MAXINC_RULE = 1;

	private static int MAXDEC_RULE = 2;

	private static int BPA_RULE = 3;

	private double[] percentiles = { 0.05, 0.25, 0.50, 0.75, 0.95 };

	private double[][] Numbers;

	private double[][] MortF;

	private double[][] MortM;

	private double[][] StockWeight;

	private double[][] CatchWeight;

	private double[][] Maturity;

	private double[][] Catch;

	private double[] SSB;

	private double[] TSB;

	private double[] TCB;

	private double[] FAV;
    
    private double[] TAC;
    
    private double tac,prevTac;

	private Distortion[] numGenerators;

	private Distortion[] mortfGenerators;

	private Distortion[] mortmGenerators;

	private Distortion[] stockwGenerators;

	private Distortion[] catchwGenerators;

	private Distortion[] matGenerators;

	private Distortion numberDistortion;

	private Distortion fishingDistortion;

	private Distortion implementationError;

	private Distortion recruitmentDistortion;

	private double[][] summary;

	private double[] sumMean;

	private int sumStart;

	private int sumEnd;

	private static int SUM_F = 0;

	private static int SUM_FD = 1;

	private static int SUM_CATCH = 2;

	private static int SUM_SSB = 3;

	private static int SUM_TSB = 4;

	private static int SUM_REC = 5;

	private static int SUM_CHANGE = 6;

	private static int SUM_BLIM = 7;

	private static int SUM_BPA = 8;
    
    private static int SUM_FLIM = 9;
        
    private static int SUM_MAXTHRESHOLD = 10;
        
    private static int SUM_MINTHRESHOLD = 11;

	private static int SUM_EXACT = 12;

	private static int SUM_INC = 13;

	private static int SUM_DEC = 14;

	private static int SUM_FLOW = 15;
    
 
	private static int SUMS = 16;

	private static int MEAN_SUMS = SUM_CHANGE + 1;

	private double bpa;

	private double blim;

	private double fmax;

	private int wmType;

	private boolean wmStockWeight, wmCatchWeight, wmMaturity, wmCannibalism;

	private double[] wmStockWeightAlpha;

	private double[] wmStockWeightBeta;

	private double[] wmCatchWeightAlpha;

	private double[] wmCatchWeightBeta;

	private double wmMaturityAlpha, wmMaturityKappa, wmMaturityGamma;

	private int wmMaturityMinage, wmMaturityMaxage, wmMaturityFunction;

	private int wmStockWeightMinage, wmStockWeightMaxage;

	private int wmCatchWeightMinage, wmCatchWeightMaxage;

	private int wmMaturityAges, wmStockWeightAges, wmCatchWeightAges;

	private int wmCatchWeightLastUpdate;
	private double[] wmMaturityLambda, wmMaturityWeight50;
	
	private int wmCannibalismMinage, wmCannibalismMaxage, wmCannibalismAges;
	private int wmCannibalismFunction;
	private double[] wmCannibalismAlpha, wmCannibalismBeta;

	private double[][] histStockWeights, histCatchWeights, histMaturity;
	private int historicyears, maxhistoric;

	private static int INITIAL = 0;

	private static int DENSITY = 1;

	private static int HISTORIC = 2;

	private RecruitmentGenerator recruitsGenerator;

	private RecruitmentGenerator[] recruitmentGenerators;

	private ManagementRule fishingGenerator;

	private OutputWriter ruleOutput;

	private boolean doSocket = false;

	private int socket;

	private ServerSocket myService;

	private Socket serviceSocket;

	private BufferedReader socketInput;

	private PrintStream socketOutput;

  private boolean wmStockWeightLimit;

  private double[] wmStockWeightMin;

  private double[] wmStockWeightMax;

  private boolean wmCatchWeightLimit;

  private double[] wmCatchWeightMin;

  private double[] wmCatchWeightMax;

  private boolean wmMaturityLimit;

  private double[] wmMaturityMin;

  private double[] wmMaturityMax;

  private boolean wmCannibalismLimit;

  private double[] wmCannibalismMin;

  private double[] wmCannibalismMax;

private double flim;

private double minthreshold;

private double maxthreshold;

private boolean verbose = false;

	public double[] getN(int year) {
		numberDistortion.setExpected(Numbers[year]);
		return numberDistortion.generate();
	}
	
	public void setVerbose(boolean verbose) {
		this.verbose=verbose;
	}

	public double[] getF(int year) {
		fishingDistortion.setExpected(MortF[year]);
		return fishingDistortion.generate();
	}

	public double getRec(int year) {
		/* ugly hack to use distortion with single value */
		double[] r = new double[1];
		r[0] = Numbers[year][0];
		/* NOTE: should make sure is only generated once? */
		recruitmentDistortion.setExpected(r);
		return recruitmentDistortion.generate()[0]; /* Ugly! */
	}

	public double[] getM(int year) {
		return MortM[year];
	}

	public double[] getSw(int year) {
		return StockWeight[year];
	}

	public double[] getCw(int year) {
		if (wmType==DENSITY && wmCatchWeight && year > wmCatchWeightLastUpdate)
			return CatchWeight[wmCatchWeightLastUpdate];
		else
			return CatchWeight[year];
	}

	public double[] getMat(int year) {
		return Maturity[year];
	}

	public int getFbarMin() {
		return favemin;
	};

	public int getFbarMax() {
		return favemax;
	};

	public int getMinAge() {
		return minage;
	};

	public int getMaxage() {
		return minage + ages - 1;
	};

	public int minage() {
		return minage;
	}

	public int ages() {
		return ages;
	}

	public int years() {
		return years;
	}

	public int realYear() {
		return realyear;
	}

	public int firstyear() {
		return firstyear;
	}

	public int recruitedYears(int year) {
		return lastrecruitedyear - year + 1;
	}

	public double getBpa() {
		return bpa;
	}

	public double getBlim() {
		return blim;
	}

	public double getSSB(int y) {
		return SSB[y];
	};

	public double getSSMeanWeight(int y) {
		// return mean weight in spawning stock (for recruitment functions)
		double Num = 0;
		for (int a = 0; a < ages; a++) {
			Num += Numbers[y][a] * Maturity[y][a];
		}
		return SSB[y] / Num;
	}

	public void printInfo(OutputWriter out) {
//		out.println("Stockname, " + stockname);
//		out.println("years, " + years);
//		out.println("ages, " + ages);
//		out.println("minage, " + minage);
//		out.println("realyear, " + realyear);
//		out.println("firstyear, " + firstyear);
//		out.println("lastyear, " + lastyear);
		
		out.println("Stockname, " + stockname);
		out.println("minage, " + minage);
		out.println("maxage, " + (minage + ages - 1));
		out.println("firstyear, " + (realyear + firstyear));
		out.println("lastyear, " + (realyear + lastyear - 1));

	}

	public void printStock(OutputWriter out) {
		out.println("Stock Numbers");
		out.printMatrix(Numbers, minage, realyear);
		out.println("MortF");
		out.printMatrix(MortF, minage, realyear);
		out.println("MortM");
		out.printMatrix(MortM, minage, realyear);
		out.println("StockW");
		out.printMatrix(StockWeight, minage, realyear);
		out.println("CatchW");
		out.printMatrix(CatchWeight, minage, realyear);
		out.println("Maturity");
		out.printMatrix(Maturity, minage, realyear);
		out.println("Catch Numbers");
		out.printMatrix(Catch, minage, realyear);
		out.println("SSB");
		out.printVector(SSB, realyear);
		out.println("TSB");
		out.printVector(TSB, realyear);
		out.println("Total Catch Biomass");
		out.printVector(TCB, realyear);
		out.println("F " + (0 + favemin + minage) + "-"
				+ (0 + favemax + minage));
		out.printVector(FAV, realyear);
        out.println("TAC");
        out.printVector(TAC, realyear);
	}

	public void initSummary(int iterations, OutputWriter out) {
		summary = new double[SUMS][iterations];
		sumMean = new double[SUMS];
		out.println("Prost summary table\n");
		out.println("Model start year, " + (firstyear + realyear) + ", Bpa, "
				+ bpa);
		out.println("Model end year, " + (lastyear + realyear - 1) + ", Blim, "
				+ blim);
		out.println("Summary start year, " + (sumStart + realyear));
		out.println("Summary end year, " + (sumEnd + realyear));
		out.println(",,,MEAN VALUES\n");
		out.println("Simulation no.,F,F distorted,Catch,SSB,TSB,Recruits,"
				+ "%annual change,No. Years where,No. Years where,"
                + "No. Years where,No. Years where quota is,,"
				+ "No. Years where various parts of HCR decide TAC");
		out.println(",,,,,,,In TAC,SSB < Blim,SSB < Bpa,F > Flim,"
                + "Inreased more than,Decreased more than,,SSB above Bpa,,SSB below Bpa");
		out.println(",,,,,,,(absolute value),,,,maxthreshold %,minthreshold %,exactly,%increase,%decrease");

	}

	public void initF(OutputWriter out) {
		out.printHead(realyear, years, "F assumed");
	}

	public void printF(OutputWriter out, int iteration) {
		double[] f = fishingGenerator.getF();
		out.printIndexVector(f, realyear, iteration);
	}

	public void initFd(OutputWriter out) {
		out.printHead(realyear, years, "F distorted");
	}

	public void printFd(OutputWriter out, int iteration) {
		out.printIndexVector(FAV, realyear, iteration);
	}

	public void initCatch(OutputWriter out) {
		out.printHead(realyear, years, "Catch");
	}

	public void printCatch(OutputWriter out, int iteration) {
		out.printIndexVector(TCB, realyear, iteration);
	}

	public void initSSB(OutputWriter out) {
		out.printHead(realyear, years, "SSB");
	}

	public void printSSB(OutputWriter out, int iteration) {
		out.printIndexVector(SSB, realyear, iteration);
	}

	public void initTSB(OutputWriter out) {
		out.printHead(realyear, years, "TSB");
	}

	public void printTSB(OutputWriter out, int iteration) {
		out.printIndexVector(TSB, realyear, iteration);
	}

	public void initRecruit(OutputWriter out) {
		out.printHead(realyear, years, "Recruits");
	}

	public void printRecruit(OutputWriter out, int iteration) {
		double[] r = new double[Numbers.length];
		for (int i = 0; i < r.length; i++)
			r[i] = Numbers[i][0];
		out.printIndexVector(r, realyear, iteration);
	}

	public void doSummary(int iteration) {
        /* Do printing of summary tables to summary.csv */
		double[] f = fishingGenerator.getF();
        //prevTac = tac;
        //tac = fishingGenerator.getTAC();
		int[] rules = fishingGenerator.getRules();
		for (int y = sumStart; y <= sumEnd; y++) {
			summary[SUM_F][iteration] += f[y];
			summary[SUM_FD][iteration] += FAV[y];
			summary[SUM_CATCH][iteration] += TCB[y];
			summary[SUM_SSB][iteration] += SSB[y];
			summary[SUM_TSB][iteration] += TSB[y];
			summary[SUM_REC][iteration] += Numbers[y][0];
			if (SSB[y] < blim)
				summary[SUM_BLIM][iteration]++;
			if (SSB[y] < bpa)
				summary[SUM_BPA][iteration]++;
			if (rules[y] == NO_RULE)
				summary[SUM_EXACT][iteration]++;
			if (rules[y] == MAXINC_RULE)
				summary[SUM_INC][iteration]++;
			if (rules[y] == MAXDEC_RULE)
				summary[SUM_DEC][iteration]++;
			if (rules[y] == BPA_RULE)
				summary[SUM_FLOW][iteration]++;
			if (y < sumEnd) {
				summary[SUM_CHANGE][iteration] += Math
						.abs((TCB[y + 1] - TCB[y]) / TCB[y] * 100.0);
            }
			if (y>firstyear+1) {
			    double diff=(TAC[y]-TAC[y-1])/TAC[y-1]*100.0;
				if (diff>0 && diff>maxthreshold) 
				    summary[SUM_MAXTHRESHOLD][iteration]++;
				if(diff<0 && -diff>minthreshold)
				    summary[SUM_MINTHRESHOLD][iteration]++;
			}
            if (FAV[y]>flim)
                summary[SUM_FLIM][iteration]++;
		}
		int n = sumEnd - sumStart + 1;
		for (int t = 0; t < MEAN_SUMS; t++)
			summary[t][iteration] /= n; 
	}

	public void printSummary(OutputWriter out) {
		for (int i = 0; i < summary[0].length; i++)
			out.println((i + 1) + ", " + summary[SUM_F][i] + ", "
					+ summary[SUM_FD][i] + ", " + summary[SUM_CATCH][i] + ", "
					+ summary[SUM_SSB][i] + ", " + summary[SUM_TSB][i] + ", "
					+ summary[SUM_REC][i] + ", " + summary[SUM_CHANGE][i]
					+ ", " + summary[SUM_BLIM][i] + ", " + summary[SUM_BPA][i]
                    + ", " + summary[SUM_FLIM][i]
                    + ", " + summary[SUM_MAXTHRESHOLD][i] 
                    + ", " + summary[SUM_MINTHRESHOLD][i]
					+ ", " + summary[SUM_EXACT][i] + ", " + summary[SUM_INC][i]
					+ ", " + summary[SUM_DEC][i] + ", " + summary[SUM_FLOW][i]);

		for (int i = 0; i < summary[0].length; i++)
			for (int t = 0; t < SUMS; t++)
				sumMean[t] += summary[t][i];
		for (int t = 0; t < SUMS; t++)
			sumMean[t] /= summary[0].length;
		out.print("\nMean");
        /* Warning, the following loop assumes the SUM_ constants are 
         * numbered in the same order as they are printed! */
		for (int t = 0; t < SUMS; t++)
			out.print(", " + sumMean[t]);
		out.println("\nPercentiles");
		for (int t = 0; t < SUMS; t++)
			Arrays.sort(summary[t]);
		int index;
		for (int i = 0; i < percentiles.length; i++) {
			index = (int) (percentiles[i] * (summary[SUM_F].length - 1));
			out.println(percentiles[i] * 100 + "%, " + summary[SUM_F][index]
					+ ", " + summary[SUM_FD][index] + ", "
					+ summary[SUM_CATCH][index] + ", "
					+ summary[SUM_SSB][index] + ", " + summary[SUM_TSB][index]
					+ ", " + summary[SUM_REC][index] + ", "
					+ summary[SUM_CHANGE][index] + ", "
					+ summary[SUM_BLIM][index] + ", " + summary[SUM_BPA][index]
                    + ", " + summary[SUM_FLIM][index]
                    + ", " + summary[SUM_MAXTHRESHOLD][index]
                    + ", " + summary[SUM_MINTHRESHOLD][index]                                                                         
					+ ", " + summary[SUM_EXACT][index] + ", "
					+ summary[SUM_INC][index] + ", " + summary[SUM_DEC][index]
					+ ", " + summary[SUM_FLOW][index]);
		}

	}

	public void flushOutput() {
		ruleOutput.flush();
	}

	private void initArrays() {
		Numbers = new double[years][ages];
		MortF = new double[years][ages];
		MortM = new double[years][ages];
		StockWeight = new double[years][ages];
		CatchWeight = new double[years][ages];
		Maturity = new double[years][ages];
		Catch = new double[years][ages];
		SSB = new double[years];
		TSB = new double[years];
		TCB = new double[years];
		FAV = new double[years];
        TAC = new double[years];
	}

    public void setFAboveBpa(double f) {
        if (fishingGenerator instanceof ThreeYearRule)
            ((ThreeYearRule)fishingGenerator).setFAboveBpa(f);
        else if (fishingGenerator instanceof ConstantFRule)
             ((ConstantFRule) fishingGenerator).setFAboveBpa(f);
        else
            throw new ClassCastException();
    }
    
	public void readFromFile(InputReader in) {
		stockname = in.expectString("name");
		int y0 = in.expectInt("firstyear");
		int y1 = in.expectInt("lastyear");
		int ye = in.expectOptionalInt("extrayears", 0);
		int a0 = in.expectInt("minage");
		int a1 = in.expectInt("maxage");
		//if (ye<a0) ye=a0;
		minage = a0;
		ages = a1 - a0 + 1;
		realyear = y0 - minage;
		firstyear = minage;
		;years = (y1 - y0 + 1) + minage * 2;
		if (ye<a0)
			years = (y1 - y0 + 1) + minage + a0;
		else
			years = (y1 - y0 + 1) + minage + ye;
		lastyear = y1 - y0 + 1 + minage;

		favemin = in.expectInt("fbarmin") - minage;
		favemax = in.expectInt("fbarmax") - minage;
		faveages = favemax - favemin + 1;
		bpa = in.expectDouble("Bpa");
		blim = in.expectDouble("Blim");
        flim = in.expectDouble("Flim");
        maxthreshold = in.expectDouble("MaxThreshold");
        minthreshold = in.expectDouble("MinThreshold");
		fmax = in.expectDouble("MaxF");
		sumStart = in.expectInt("summarystart") - realyear;
		sumEnd = in.expectInt("summaryend") - realyear;

		InputReader popreader = new InputReader(in.expectString("population"), verbose);
		InputReader recreader = new InputReader(in.expectString("recruitment"), verbose);
		InputReader management = new InputReader(in.expectString("management"), verbose);

		management.expectWord("[ManagementDistortions]");
		management.expectWord("ImplementationError");
		implementationError = Distortion.createFromFile(management, ages);
		management.expectWord("InputNumbers");
		numberDistortion = Distortion.createFromFile(management, ages);
		management.expectWord("InputFishing");
		fishingDistortion = Distortion.createFromFile(management, ages);
		management.expectWord("Recruitment");
		recruitmentDistortion = Distortion.createFromFile(management, 1);

		management.expectWord("[ManagementRule]");
		fishingGenerator = ManagementRule.createFromFile(management, this);

		int lookaheadYears = fishingGenerator.lookaheadYears();
		if (ye<lookaheadYears && a0<lookaheadYears) {
			//System.err.println("Error: Management function requires " + lookaheadYears + " years of data after last assessment year.");
			years = (y1 - y0 + 1) + minage + lookaheadYears;
		}

		String wmtype = in.expectString("weightandmaturity");
		if (wmtype.equalsIgnoreCase("density")) {
			wmType = DENSITY;
			readDensity(in.expectString("file"));
		} else if (wmtype.equalsIgnoreCase("historic")) {
			wmType = HISTORIC;
			readHistoric(in.expectString("file"));
		} else if (wmtype.equalsIgnoreCase("initial")) {
			wmType = INITIAL;
		} else {
			in.error("No such weight and maturity option, " + wmtype);
		}

		Prost.logMessage("");
		Prost.logMessage("Reading stock numbers for " + (firstyear + 1) + " years.");
		numGenerators = readInitial(popreader, "[numbers]", firstyear + 1);
		Prost.logMessage("Reading fishing mortality for " + (firstyear + 1) + " years.");
		mortfGenerators = readInitial(popreader, "[fishingmortality]",
				firstyear + 1);
		Prost.logMessage("");
		if (lookaheadYears>0)
			Prost.logMessage("The selected management function requires " + lookaheadYears + " years of extra data.");
		Prost.logMessage("The recruitment function requires " + minage + " years of extra data.");
		if (ye>0)
			Prost.logMessage("The keyword 'extrayears' was set to require " + ye + " years of extra data.");
		Prost.logMessage("Preparing to read input files with " + years + " years of data.");
		Prost.logMessage("\nReading natural mortality for " + years + " years.");
		mortmGenerators = readInitial(popreader, "[naturalmortality]", years);
		Prost.logMessage("Reading stock weight for " + years + " years.");
		stockwGenerators = readInitial(popreader, "[stockweight]", years);
		Prost.logMessage("Reading catch weight for " + years + " years.");
		catchwGenerators = readInitial(popreader, "[catchweight]", years);
		Prost.logMessage("Reading maturity for " + years + " years.");
		matGenerators = readInitial(popreader, "[maturity]", years);

		recreader.expectWord("[Recruitment]");
		int recs = recreader.expectInt("generators");
		recruitmentGenerators = new RecruitmentGenerator[years];
		for (int i = 0; i < recs; i++) {
			recreader.expectWord("[RecruitmentGenerator]");
			int y = recreader.expectInt("firstyear") - realyear - minage;
			recruitmentGenerators[y] = RecruitmentGenerator  // TODO: add errormessage if out of bounds
					.createFromFile(recreader);
		}
		RecruitmentGenerator rg = null;
		for (int i = 0; i < years; i++) {
			if (recruitmentGenerators[i] != null)
				rg = recruitmentGenerators[i];
			recruitmentGenerators[i] = rg;
			//System.out.println(rg);
		}
		//recruitsGenerator=RecruitmentGenerator.createFromFile(recreader);

		initArrays();
		String outfile = "rule.csv";
		java.io.FileWriter f = null;
		try {
			f = new java.io.FileWriter(outfile);
		} catch (java.io.IOException e) {
			in.error("Could not open " + outfile + " for writing.");
		}
		ruleOutput = new OutputWriter(new java.io.BufferedWriter(f));
		fishingGenerator.printHeader(ruleOutput);
	}

	private Distortion[] readInitial(InputReader in, String keyword, int size) {
		Distortion[] dist = new Distortion[size];
		in.expectWord(keyword);
		double[][] expected = in.expectMatrix("expected", size, ages);
		for (int y = 0; y < size; y++) {
			dist[y] = Distortion.createFromFile(in, ages);
			dist[y].setExpected(expected[y]);
		}
		return dist;
	}

	private void readDensity(String filename) {
		InputReader in = new InputReader(filename, verbose);

		String sw = in.expectString("stockweight");
		if (sw.equalsIgnoreCase("yes")) {
			wmStockWeight = true;
			wmStockWeightMinage = in.expectInt("minage") - minage;
			wmStockWeightMaxage = in.expectInt("maxage") - minage;
			wmStockWeightAges = wmStockWeightMaxage - wmStockWeightMinage + 1;
			wmStockWeightAlpha = in.expectVector("alpha", wmStockWeightAges);
			wmStockWeightBeta = in.expectVector("beta", wmStockWeightAges);
			wmStockWeightLimit = in.expectOptionalKeyword("limit");
			if (wmStockWeightLimit) {
			  wmStockWeightMin=in.expectVector("min", wmStockWeightAges);
			  wmStockWeightMax=in.expectVector("max", wmStockWeightAges);
			}
		} else if (sw.equalsIgnoreCase("no")) {
			wmStockWeight = false;
		} else {
			in.error("Keyword Stockweight should be followed by yes or no.");
		}

		String cw = in.expectString("catchweight");
		if (cw.equalsIgnoreCase("yes")) {
			wmCatchWeight = true;
			wmCatchWeightMinage = in.expectInt("minage") - minage;
			wmCatchWeightMaxage = in.expectInt("maxage") - minage;
			wmCatchWeightAges = wmCatchWeightMaxage - wmCatchWeightMinage + 1;
			wmCatchWeightAlpha = in.expectVector("alpha", wmCatchWeightAges);
			wmCatchWeightBeta = in.expectVector("beta", wmCatchWeightAges);
			wmCatchWeightLimit = in.expectOptionalKeyword("limit");
			if (wmCatchWeightLimit) {
			  wmCatchWeightMin=in.expectVector("min", wmCatchWeightAges);
			  wmCatchWeightMax=in.expectVector("max", wmCatchWeightAges);
			}
		} else if (cw.equalsIgnoreCase("no")) {
			wmCatchWeight = false;
		} else {
			in.error("Keyword Catchweight should be followed by yes or no.");
		}

		String ma = in.expectString("maturity");
		if (ma.equalsIgnoreCase("yes")) {
		  	String[] functions = new String[] {"densitydependent", "weightdependent"}; 
			wmMaturity = true;
			in.expectWord("function");
			wmMaturityFunction = in.expectWord(functions);
			wmMaturityMinage = in.expectInt("minage") - minage;
			wmMaturityMaxage = in.expectInt("maxage") - minage;
			wmMaturityAges = wmMaturityMaxage - wmMaturityMinage + 1;
			if (wmMaturityFunction == 0) {
			  wmMaturityAlpha = in.expectDouble("alpha");
			  wmMaturityKappa = in.expectDouble("kappa");
			  wmMaturityGamma = in.expectDouble("gamma");
			} else if (wmMaturityFunction == 1) {
			  wmMaturityLambda = in.expectVector("lambda", wmMaturityAges);
			  wmMaturityWeight50 = in.expectVector("w50", wmMaturityAges);
			}
			wmMaturityLimit = in.expectOptionalKeyword("limit");
			if (wmMaturityLimit) {
			  wmMaturityMin=in.expectVector("min", wmMaturityAges);
			  wmMaturityMax=in.expectVector("max", wmMaturityAges);
			}
		} else if (ma.equalsIgnoreCase("no")) {
			wmMaturity = false;
		} else {
			in.error("Keyword Maturity should be followed by yes or no.");
		}
		
		String m2 = in.expectString("cannibalism");
		if (m2.equalsIgnoreCase("yes")) {
		  	String[] functions = new String[] {"ssblag3", "biomass6and7"}; 
			wmCannibalism = true;
			in.expectWord("function");
			wmCannibalismFunction = in.expectWord(functions);
			wmCannibalismMinage = in.expectInt("minage") - minage;
			wmCannibalismMaxage = in.expectInt("maxage") - minage;
			wmCannibalismAges = wmCannibalismMaxage - wmCannibalismMinage + 1;
			wmCannibalismAlpha = in.expectVector("alpha", wmCannibalismAges);
			wmCannibalismBeta = in.expectVector("beta", wmCannibalismAges);
			wmCannibalismLimit = in.expectOptionalKeyword("limit");
			if (wmCannibalismLimit) {
			  wmCannibalismMin=in.expectVector("min", wmCannibalismAges);
			  wmCannibalismMax=in.expectVector("max", wmCannibalismAges);
			}
		} else if (m2.equalsIgnoreCase("no")) {
		  wmCannibalism=false;
		} else {
		  in.error("Keyword Cannibalism should be followed by yes or no.");
		}
	}

	private void updateDensityDependent(int y) {
		int aa = 0;
		double tmp;
		if (wmStockWeight)
			for (int a = 0; a < wmStockWeightAges; a++) {
				aa = a + wmStockWeightMinage;
				tmp = wmStockWeightAlpha[a] * TSB[y] / 1000000 + wmStockWeightBeta[a];
				if (wmStockWeightLimit) 
				  StockWeight[y + 1][aa] = limit(tmp, wmStockWeightMin[a], wmStockWeightMax[a]);
				else 
				  StockWeight[y + 1][aa] = tmp;
			}
		if (wmCatchWeight) {
			for (int a = 0; a < wmCatchWeightAges; a++) {
				aa = a + wmCatchWeightMinage;
				tmp = wmCatchWeightAlpha[a] * StockWeight[y + 1][aa] + wmCatchWeightBeta[a];
				if (wmCatchWeightLimit) 
				  CatchWeight[y+1][aa] = limit(tmp, wmCatchWeightMin[a], wmCatchWeightMax[a]);
				else 
				  CatchWeight[y + 1][aa]=tmp;
			}
			wmCatchWeightLastUpdate = y+1;
		}
		if (wmMaturity && wmMaturityFunction==0)
			for (int a = 0; a < wmMaturityAges; a++) {
			  	aa=a+wmMaturityMinage;
				tmp = 1 / (1 + Math.exp(-wmMaturityAlpha * (wmMaturityGamma * 
				    (minage + aa) - wmMaturityKappa - TSB[y] / 1000000)));
				if (wmMaturityLimit)
				  Maturity[y + 1][aa] = limit(tmp, wmMaturityMin[a], wmMaturityMax[a]);
				else  
				  Maturity[y + 1][aa] = tmp;
			}
		if (wmMaturity && wmMaturityFunction==1)
			for (int a = 0; a < wmMaturityAges; a++) {
			  	aa=a+wmMaturityMinage;
				tmp = 1 / (1 + Math.exp(-wmMaturityLambda[a]
								* (StockWeight[y+1][aa]-wmMaturityWeight50[a])));
				if (wmMaturityLimit)
				  Maturity[y + 1][aa] = limit(tmp, wmMaturityMin[a], wmMaturityMax[a]);
				else  
				  Maturity[y + 1][aa] = tmp;
			}
		if (wmCannibalism && wmCannibalismFunction==0)
		  for (int a=0; a<wmCannibalismAges; a++) {
		    aa=a+wmCannibalismMinage;
		    tmp=wmCannibalismAlpha[a]*SSB[y-2]+wmCannibalismBeta[a];
		    if (wmCannibalismLimit)
		      MortM[y+1][aa]+=limit(tmp, wmCannibalismMin[a], wmCannibalismMax[a]);
		    else
		      MortM[y+1][aa]+=tmp;
		  }
		
		if (wmCannibalism && wmCannibalismFunction==1)
		  for (int a=0; a<wmCannibalismAges; a++) {
		    aa=a+wmCannibalismMinage;
		    tmp=wmCannibalismAlpha[a]*(Numbers[y+1][6-minage]*StockWeight[y+1][6-minage]+
		                               Numbers[y+1][7-minage]*StockWeight[y+1][7-minage])
		                             +wmCannibalismBeta[a];
		    if (wmCannibalismLimit)
		      MortM[y+1][aa]+=limit(tmp, wmCannibalismMin[a], wmCannibalismMax[a]);
		    else
		      MortM[y+1][aa]+=tmp;
		  }
	}

	/**
   * @param tmp
   * @param d
   * @param e
   * @return
   */
  private double limit(double val, double min, double max) {
    if(val<min) return min;
    else if(val>max) return max;
    else return val;
  }

  private void readHistoric(String filename) {
		InputReader in = new InputReader(filename, verbose);
		historicyears=in.expectInt("numberofyears");
		maxhistoric=historicyears-(years-firstyear);
		if (maxhistoric < 1) {
			in.error("To few years in historic data.\n"
					+" Must have at least "+(years-firstyear+1)+"years,"
					+" had only "+historicyears+" years.");
		}
		String sw = in.expectString("stockweight");
		if (sw.equalsIgnoreCase("yes")) {
			wmStockWeight = true;
			histStockWeights = readHistoricData(new InputReader(in
					.expectString("file"), verbose));
		} else if (sw.equalsIgnoreCase("no")) {
			wmStockWeight = false;
		} else {
			in.error("Keyword Stockweight should be followed by yes or no.");
		}

		String cw = in.expectString("catchweight");
		if (cw.equalsIgnoreCase("yes")) {
			wmCatchWeight = true;
			histCatchWeights = readHistoricData(new InputReader(in
					.expectString("file"), verbose));
		} else if (cw.equalsIgnoreCase("no")) {
			wmCatchWeight = false;
		} else {
			in.error("Keyword Catchweight should be followed by yes or no.");
		}

		String ma = in.expectString("maturity");
		if (ma.equalsIgnoreCase("yes")) {
			wmMaturity = true;
			histMaturity = readHistoricData(new InputReader(in
					.expectString("file"), verbose));
		} else if (ma.equalsIgnoreCase("no")) {
			wmMaturity = false;
		} else {
			in.error("Keyword Maturity should be followed by yes or no.");
		}
	}

	public double[][] readHistoricData(InputReader in) {
		double[][] d=in.expectMatrix("historicdata",historicyears,ages);
		int maxy=historicyears-(years-firstyear);
		return d;
	}

	public void initHistoric() {
		int r= RandomGenerator.nextInt(maxhistoric+1);
		if (wmCatchWeight) {
			for (int y=firstyear+1; y<years; y++)
				CatchWeight[y]=histCatchWeights[y-firstyear+r];
		}
		if (wmStockWeight) {
			for (int y=firstyear+1; y<years; y++)
				StockWeight[y]=histStockWeights[y-firstyear+r];
		}
		if (wmMaturity) {
			for (int y=firstyear+1; y<years; y++)
				Maturity[y]=histMaturity[y-firstyear+r];
		}
	}
	public void generateInitial() {
		// setup pop
		for (int y = 0; y < years; y++) {
			MortM[y] = mortmGenerators[y].generate();
			StockWeight[y] = stockwGenerators[y].generate();
			CatchWeight[y] = catchwGenerators[y].generate();
			Maturity[y] = matGenerators[y].generate();
			if (recruitmentGenerators[y] != null)
				recruitmentGenerators[y].reset();
		}
		// setup initial pop upto (and including) assessment year
		for (int y = 0; y <= firstyear; y++) {
			Numbers[y] = numGenerators[y].generate();
			MortF[y] = mortfGenerators[y].generate();
			for (int a = 0; a < ages; a++)
				if (MortF[y][a] > 0.0)
					Catch[y][a] = Functions.fToCatch(MortF[y][a],
							Numbers[y][a], MortM[y][a]);
		}
		for (int y = 0; y < firstyear; y++) {
			SSB[y] = calcSSB(y);
			TSB[y] = calcTSB(y);
			TCB[y] = calcTCB(y);
			FAV[y] = calcFAV(y);
            TAC[y] = 0;
			updateRecruits(y);
		}
        TAC[firstyear]=0;

		fishingGenerator.reset();
		wmCatchWeightLastUpdate = 0;
		if (wmType==HISTORIC)
			initHistoric();
	}

	public void simulate() {
		generateInitial();
		
		// fishingGenerator.setInitial( MortF, CatchWeight, Maturity );
		for (int y = firstyear; y < lastyear; y++)
			simulateTimestep(y);
		/*
		 * SSB[lastyear]=calcSSB(lastyear); TSB[lastyear]=calcTSB(lastyear);
		 * TCB[lastyear]=calcTCB(lastyear); FAV[lastyear]=calcFAV(lastyear);
		 * applyMortality(lastyear);
		 */

	}

	/**
	 * 
	 */
	public void initSocket() {
		System.out.print("Waiting for connection on socket "+socket+": ");
		try {
		   myService = new ServerSocket(socket);
		   serviceSocket = myService.accept();
		   socketInput = new BufferedReader(new InputStreamReader(serviceSocket.getInputStream()));
		   socketOutput = new PrintStream(serviceSocket.getOutputStream());
		   
		   socketOutput.print("This is Prost.\n");
		   
/*		   String in=socketInput.readLine();
		   String[] ss=in.split(" ");
		   for(int i=0; i<ss.length; i++) {
		   	 	System.out.println(Double.parseDouble(ss[i]));
		   }
		   System.out.println(in);
*/
		}	
		catch (java.io.IOException e) {
		   System.out.println(e);
		}
		System.out.println("OK.");
	}
	public void updateFromSocket(int y) {
		socketOutput.println("Year: "+(y+realyear));
		StockWeight[y]=getValuesFromSocket("Waiting for Weight in stock",y);
		CatchWeight[y]=getValuesFromSocket("Waiting for Weight in catch",y);
		Maturity[y]=getValuesFromSocket("Waiting for Maturity",y);
		MortM[y]=getValuesFromSocket("Waiting for Natural Mortality",y);
	}
	public double[] getValuesFromSocket(String prompt, int year) {
		String in;
		String[] ss;
		double[] v=new double[ages];
		try {
			socketOutput.print(prompt+". ("+ages+" values). ");
			in=socketInput.readLine();
			ss=in.split(" ");
			if (ss.length != ages) {
				System.out.println("Error: Expected "+ages+" Values, but got "+ss.length);
				socketOutput.println("ERROR: Wrong number of values!");
				System.exit(1);
			}
			for(int i=0; i<ss.length; i++) {
				v[i]=Double.parseDouble(ss[i]);
			}	
			socketOutput.println("OK.");
		}
		catch (IOException e) {
			System.out.println(e);
		}
		return v;
	}
	public void outputResultsToSocket(int y) {
		socketOutput.print("Stock numbers at start of "+(y+realyear+1)+":");
		for (int a=0; a<ages; a++)
			socketOutput.print(" "+Numbers[y+1][a]);
		socketOutput.println();
	}
	public void disposeSocket() {
		System.out.print("Disposing of socket: ");
		socketOutput.print("Simulation finished.\n");
		try {
			socketOutput.close();
			socketInput.close();
			serviceSocket.close();
			myService.close();
		}	
		catch (java.io.IOException e) {
			System.out.println(e);
		}
		System.out.println( "OK.");
	}

	public void simulateTimestep(int y) {
		if (doSocket) {
			updateFromSocket(y);
		}
		/* calc ssb for this year */
		SSB[y] = calcSSB(y);
		TSB[y] = calcTSB(y);
		TCB[y] = calcTCB(y);
		FAV[y] = calcFAV(y);
		/* update density dependent growth and maturation for NEXT year */
		//if (wmType == DENSITY) {
		//	updateDensityDependent(y);
		//}
		/* update recruits in the future based on SSB this year */
		updateRecruits(y);
		
		/* update density dependent growth and maturation for NEXT year */
		//if (wmType == DENSITY) {
		//	updateDensityDependent(y);
		//}
		
		/* apply mortality this year */
		applyMortality(y); // Updates next years Numbers (except recruits)
		
		/* update density dependent growth and maturation for NEXT year */
		// Moved this down here, so the stock numbers will be updated for the M2 function two
		if (wmType == DENSITY) {
			updateDensityDependent(y);
		}
		
		/* generate catch for next year */
		updateFishing(y);
		if (doSocket) {
			outputResultsToSocket(y);
		}
	}

	private void updateRecruits(int y) {
		if (recruitmentGenerators[y] != null)
			//Numbers[y+minage][0]=recruitmentGenerators[y].generate(SSB[y]);
			Numbers[y + minage][0] = recruitmentGenerators[y].generate(this, y);
		lastrecruitedyear = y + minage;

	}

	private void applyMortality(int y) {
		double plusgroup = Numbers[y][ages - 1]
				* Math.exp(-MortF[y][ages - 1] - MortM[y][ages - 1]);
		for (int a = 0; a < ages - 1; a++)
			Numbers[y + 1][a + 1] = Numbers[y][a]
					* Math.exp(-MortF[y][a] - MortM[y][a]);
		Numbers[y + 1][ages - 1] += plusgroup;
		//Numbers[y+1][0]=0;
	}

	private double calcSSB(int y) {
		double ssb = 0;
		for (int a = 0; a < ages; a++)
			ssb += (Numbers[y][a] * StockWeight[y][a] * Maturity[y][a]);
		return ssb;
	}

	private double calcTSB(int y) {
		double tsb = 0;
		for (int a = 0; a < ages; a++)
			tsb += (Numbers[y][a] * StockWeight[y][a]);
		return tsb;
	}

	private double calcTCB(int y) {
		double tcb = 0;
		for (int a = 0; a < ages; a++)
			tcb += (Catch[y][a] * CatchWeight[y][a]);
		return tcb;
	}

	private double calcFAV(int y) {
		double fav = 0;
		for (int a = favemin; a <= favemax; a++)
			fav += (MortF[y][a]);
		fav = fav / faveages;
		return fav;
	}

	private void updateFishing(int y) {
		implementationError.setExpected(fishingGenerator.generate(this, y));
		//adjustFishing(y, implementationError.generate());
		Catch[y+1] = adjustFishing(y, implementationError.generate());
		//for (int a=0; a<ages; a++)
		//  System.out.print(CatchWeight[y+1][a]+"\t");
		//System.out.println();
		//System.out.println("\t"+CatchWeight[y+1][7]);
		//System.out.println("\t"+calcTCB(y+1));
		MortF[y + 1] = Functions.catchToF(Numbers[y + 1], MortM[y + 1],
				Catch[y + 1], ages);
		fishingGenerator.print(ruleOutput);
        TAC[y+1]=fishingGenerator.getTAC();
//		System.out.println("NewF, NewC");
//		for (int a = 0; a < ages; a++) {
//			System.out.print(MortF[y+1][a] + ", ");
//			System.out.println(Catch[y+1][a]);
//		}

	}

	private double[] adjustFishing(int y, double[] Cn) {
		// Check if F>Fmax
		double[] F = Functions.catchToF(Numbers[y + 1], MortM[y + 1], Cn, ages); // was
																				 // Catch[y
																				 // + 1]
																				 // ...

		double fav = 0;
		//double maxF = 0.4; // for testing maxF only!
		for (int a = favemin; a <= favemax; a++)
			fav += F[a];
		fav = fav / faveages;

		if (fav > fmax) {
			double[] NewCn = new double[Cn.length];
//			System.out.println("F was, F is");
			for (int a = 0; a < ages; a++) {
//				System.out.print(F[a] + ", ");
				F[a] = F[a] * fmax / fav;
//				System.out.println(F[a]);
			}

			for (int a = 0; a < ages; a++)
				NewCn[a] = Functions.fToCatch(F[a], Numbers[y + 1][a],
						MortM[y + 1][a]);
//			System.out.println("C was, C is");
//			for (int a = 0; a < ages; a++) {
//				System.out.print(Cn[a] + ", ");
//				System.out.println(NewCn[a]);
//			}
			return NewCn;
		} else {
			return Cn;
		}

	}

	public StockModel() {
	}

	/**
	 * @param socket
	 */
	public void doSocket(int socket) {
		// TODO Auto-generated method stub
		doSocket=true;
		this.socket=socket;
	}
}
