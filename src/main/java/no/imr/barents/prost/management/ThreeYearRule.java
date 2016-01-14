package no.imr.barents.prost.management;

import no.imr.barents.prost.io.InputReader;
import no.imr.barents.prost.io.OutputWriter;
import no.imr.barents.prost.model.Stock;
import no.imr.barents.prost.util.Functions;

public class ThreeYearRule extends ManagementRule {

  private int minage, ages, favemin, favemax, faveages, years = 4, realyear;
  private double fpa, fmin;
  private double[][] M, Sw, Cw, Mat, N, F, C;
  private double[] SSB, Rec, Tons, tmpC;
  private double bpa, blim;
  private double maxinc;
  private double maxdec;
  private double TAC;
  private double prevTAC, tmpTAC, maxTAC;
  private double inputTAC, flevel;
  private double prevSSB;
  private double[] qF, qN, S;
  private double qSSB, qFave;
  private AdjustF lowFRule;
  private boolean firstyear = true;
  private boolean doFirstYearMaxChange = false;
  private boolean maxChangeRuleVariant = false;
  private static int NO_RULE = 0;
  private static int MAXINC_RULE = 1;
  private static int MAXDEC_RULE = 2;
  private static int BPA_RULE = 3;
  private static String[] RULES = { " - ", "Inc", "Dec", "Bpa" };
  private int invokedRule;
  private int[] rules;
  private double[] fHistory;

  public void print(OutputWriter out) {
    out.print((realyear + 1) + ", " + TAC + ", " + SSB[1] + ", ");
    out.println(qFave + ", " + RULES[invokedRule]);
    // out.flush(); // Not here!
  }

  public void printHeader(OutputWriter out) {
    out.println("Year, Quota, SSB, Fave, rules");
  }
  
  public int lookaheadYears() {
	return years-1;
  }

  public void reset() {
    prevSSB = bpa * 2;
    firstyear = true;
    prevTAC = inputTAC;
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

  public double[] generate(Stock stock, int year) {
    double[] qC = new double[ages];
    flevel = fpa;
    invokedRule = 0;
    realyear = stock.realYear() + year;
    /* Init variables with values from stock */
    for (int i = 0; i < years; i++) {
      M[i] = stock.getM(year + i);
      Sw[i] = stock.getSw(year + i);
      Cw[i] = stock.getCw(year + i);
      Mat[i] = stock.getMat(year + i);
    }
    int y = Math.min(stock.recruitedYears(year), years);
    for (int i = 0; i < y; i++)
      Rec[i] = stock.getRec(i + year);
    /* Use geometric mean where we don't know recruitment */
    double meanrec = 1;
    int meanyears = 3; /* TODO: change to read from file! */
    assert (y - meanyears > 0);
    for (int i = y - meanyears; i < y; i++)
      meanrec *= Rec[i];
    meanrec = Math.pow(meanrec, 1 / meanyears);
    for (int i = y; i < years; i++)
      Rec[i] = meanrec;
    // N[0] = stock.getN(year);
    System.arraycopy(stock.getN(year), 0, N[0], 0, ages);
    // F[0] = stock.getF(year);
    System.arraycopy(stock.getF(year), 0, F[0], 0, ages);
    // if (year==4) System.out.println(F[0][0]);
    // Simulate 3 years with Ftarget
    setConstF(F, fpa);
    simulate();
    // low SSB next year?
    if (SSB[1] < bpa) {
      invokedRule = BPA_RULE;
      flevel = lowFRule.adjust(fpa, SSB[1]);
      setConstF(F, flevel);
      simulate(); // updates C,SSB,and Tons
      // Set TAC as mean catch of the 3 years
      TAC = 0;
      for (int i = 1; i < years; i++)
        TAC += Tons[i];
      TAC /= (years - 1);
    } else {
      // Set TAC as mean catch of the 3 years
      TAC = 0;
      for (int i = 1; i < years; i++)
        TAC += Tons[i];
      TAC /= (years - 1);
      if (doMaxChangeRule(SSB, bpa)) {
        //System.out.println(realyear+1);
        // Check if change from last year is too great
        double change = (TAC - prevTAC) / prevTAC * 100.0;
        if (change > maxinc) {
          TAC = prevTAC + prevTAC * maxinc / 100.0;
          invokedRule = MAXINC_RULE;
          // System.out.println(" Quota increased to much, new
          // Quota="+TAC);
        } else if (change < -maxdec) {
          TAC = prevTAC - prevTAC * maxdec / 100.0;
          invokedRule = MAXDEC_RULE;
          // System.out.println(" Quota decreased to much, new
          // Quota="+TAC);
        }
      }
      if (maxTAC > -1 && TAC > maxTAC) {
        TAC = maxTAC;
      }
    }
    flevel = Functions.findFlevel(N[1], S, M[1], Cw[1], TAC);
        
    double fave = 0;
    for (int i = favemin; i <= favemax; i++)
      fave += S[i] * flevel;
    fave /= faveages;
    for (int i = 0; i < ages; i++)
      qF[i] = S[i] * flevel;
    for (int i = 0; i < ages; i++)
      qC[i] = Functions.fToCatch(qF[i], N[1][i], M[1][i]);
    qFave = 0;
    for (int i = favemin; i <= favemax; i++)
      qFave += qF[i];
    qFave = qFave / faveages;
    
    if (qFave < fmin) {
    	qFave = fmin;
    }
    
    fHistory[year + 1] = qFave;
    rules[year + 1] = invokedRule;
    prevTAC = TAC;
    firstyear = false;
    prevSSB = SSB[1];
    //System.out.println((realyear+1)+"\t"+TAC);
    //double tt=0;
    //for (int i = 0; i < ages; i++)
    //  tt+=qC[i]*Cw[1][i];
    //System.out.print("\t"+tt+"\t"+Cw[1][7]);
    //System.out.print("\t"+tt);
    //for (int a=0; a<ages; a++)
    //  System.out.print(Cw[1][a]+"\t");
    //System.out.println();
    return qC; // could we avoid generating a new array here?
  }

  // Check if the maxchangerule should be applied
  private boolean doMaxChangeRule(double[] ssb, double bpa) {
    if (firstyear && !doFirstYearMaxChange) {
      //System.out.println("Its first year, ignore big change");
      return false;
    }
    if (!maxChangeRuleVariant) {
      //System.out.println("Original rule, is ssb<bpa? "+(realyear+1)+" "+(prevSSB>=bpa));
      return (prevSSB >= bpa);
    }
    /*System.out.print("new rule, is ssb[i]<bpa?");
    for (int i=0; i<years; i++) {
      System.out.print(" "+(SSB[i]<bpa));
    }
    System.out.println();*/
    for (int i=0; i<years; i++) {
      if (SSB[i] < bpa) return false;
    }
    return true;
  }

  private void setConstF(double[][] f, double level) {
    double fsum = 0;
    for (int i = favemin; i <= favemax; i++)
      fsum += S[i];
    double factor = level * faveages / fsum;
    for (int i = 0; i < ages; i++)
      f[3][i] = f[2][i] = f[1][i] = S[i] * factor;
  }

  private void simulate() {
    for (int i = 0; i < years - 1; i++) {
      N[i + 1] = Functions.applyMortality(F[i], N[i], M[i], Rec[i + 1]);
    }
    // Update C, Tons, and SSB
    for (int i = 0; i < years; i++) {
      for (int j = 0; j < ages; j++)
        C[i][j] = Functions.fToCatch(F[i][j], N[i][j], M[i][j]);
      Tons[i] = 0;
      SSB[i] = 0;
      for (int j = 0; j < ages; j++) {
        Tons[i] += (C[i][j] * Cw[i][j]);
        SSB[i] += (N[i][j] * Sw[i][j] * Mat[i][j]);
      }
    }
  }

  private void setQuota(double[] quota) {
  }
  
  public void setFAboveBpa(double f) {
      fpa=f;
  }
  
  public void readFromFile(InputReader in, Stock stock) {
    minage = stock.getMinAge();
    ages = stock.ages();
    favemin = stock.getFbarMin();
    favemax = stock.getFbarMax();
    S = in.expectVector("Selection", ages);
    fpa = in.expectDouble("FaboveBpa");
    fmin = in.expectOptionalDouble("Fmin", 0.0);
    bpa = stock.getBpa();
    blim = stock.getBlim();
    prevSSB = bpa * 2;
    maxinc = in.expectDouble("Maxinc");
    maxdec = in.expectDouble("Maxdec");
    maxTAC = in.expectDouble("MaxTAC");
    inputTAC = in.expectDouble("FirstYearTAC");
    maxChangeRuleVariant=in.expectOptionalKeyword("MaxChangeRuleVariant");
    //System.out.println(maxChangeRuleVariant);
    doFirstYearMaxChange = inputTAC > 0 ? true : false;
    String bparule = in.expectString("FbelowBpa").toLowerCase();
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
    faveages = favemax - favemin + 1;
    N = new double[years][ages];
    M = new double[years][ages];
    F = new double[years][ages];
    Sw = new double[years][ages];
    Cw = new double[years][ages];
    Mat = new double[years][ages];
    C = new double[years][ages];
    Tons = new double[years];
    SSB = new double[years];
    tmpC = new double[ages];
    Rec = new double[years];
    qF = new double[ages];
    qN = new double[ages];
    fHistory = new double[stock.years()];
    rules = new int[stock.years()];
  }
}
