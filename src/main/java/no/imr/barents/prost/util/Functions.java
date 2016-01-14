package no.imr.barents.prost.util;

public final class Functions {

	public static double[] applyMortality(double[] F, double[] N, double[] M,
			double Rec) {
		int ages = N.length;
		double[] newN = new double[ages];
		double plusgroup = N[ages - 1] * Math.exp(-F[ages - 1] - M[ages - 1]);
		newN[0] = Rec;
		for (int a = 0; a < ages - 1; a++)
			newN[a + 1] = N[a] * Math.exp(-F[a] - M[a]);
		newN[ages - 1] += plusgroup;
		return newN;
	}

	/**
	 * @param F
	 *            fishing mortality
	 * @param N
	 *            stock in numbers
	 * @param M
	 *            natural mortality
	 * @return catch in numbers
	 */
	public static double fToCatch(double F, double N, double M) {
		return F * N * (1 - Math.exp(-(F + M))) / (F + M);
	}

	/**
	 * The equation to be solved by catchToF
	 */
	private static double FToBeSolved(double F, double N, double M, double C) {
		return fToCatch(F, N, M) - C;
	}

	/**
	 * The derivative of FToBeSolved
	 */
	private static double FToBeSolvedDer(double F, double N, double M) {
		return ((F * N * (F + M) - N * (F + M) + F * N) * Math.exp(-(F + M))
				+ N * (F + M) - F * N)
				/ (F + M) / (F + M);
	}

	/**
	 * Uses Newton's method to calculate fishing mortality from catch in
	 * numbers.
	 * 
	 * @param N[i]
	 *            stock in numbers at age i
	 * @param M[i]
	 *            natural mortality at age i
	 * @param C[i]
	 *            catch in numbers at age i
	 * @return the fishing mortality F[i] that correspond to the catch C[i]
	 */
	public static double[] catchToF(double[] N, double[] M, double[] C,
			int years) {
		int maxiter = 20; // normally 5 iterations is enough
		final double eps = 0.0001; // convergens criterium
		double[] tmp = new double[maxiter];
		double[] estF = new double[years];
		for (int i = 0; i < years; i++) {
			if (C[i] == 0)
				estF[i] = 0;
			else {
				tmp[0] = C[i] / N[i]; // initial value
				for (int j = 1; j < maxiter; j++) {
					tmp[j] = tmp[j - 1]
							- FToBeSolved(tmp[j - 1], N[i], M[i], C[i])
							/ FToBeSolvedDer(tmp[j - 1], N[i], M[i]);
					if (Math.abs(tmp[j] - tmp[j - 1]) < eps) {
						estF[i] = tmp[j];
						break;
					}
				}
			}
		}
		return estF;
	}

	/*
     * 
	 public static double findFlevel(double[] N, double[] S, double[] M, double[] W, double TAC) {
		int MAXITER = 40;
        double epsilon=1e-6;
        double xL=0.0;
        double xR=30;
        double xM=(xR-xL)/2.0;
        double y;
        double stockbiomass=0.0;
        //System.out.println(":"+TAC);
        for( int a = 0; a < S.length; a++) stockbiomass+=(N[a]*W[a]);
        assert(TAC<stockbiomass);
        if(TAC<=0.0)
            return 0.0;
        while((xR-xL)>epsilon) {
            assert( (catchInTons(N, S, M, W, xL)>=0) );
            assert( (catchInTons(N, S, M, W, xR)>=0) );
            assert( (catchInTons(N, S, M, W, xM)>=0) );
            assert( (catchInTons(N, S, M, W, xL)<(catchInTons(N, S, M, W, xR))) );
            assert( (catchInTons(N, S, M, W, xL)-TAC)<0 );
            assert( (catchInTons(N, S, M, W, xR)-TAC)>0 );
            xM=(xR + xL)/2.0;
            y=catchInTons(N, S, M, W, xM)-TAC;
            //System.out.println( xM+"\t"+ (catchInTons(N, S, M, W, xL)-TAC)+"\t"+(catchInTons(N, S, M, W, xR)-TAC)+"\t"+y);
            if( y > 0.0)
                xR=xM;
            else
                xL=xM;
        }
        return xM;
	}
  */
  
  
    public static double findFlevel(double[] N, double[] S, double[] M,
            double[] W, double TAC) {
        int MAXITER = 20;    //40;
        double XACC = 0.0001; //0.0001;
        double rts = 0;
        double xl = 0.4;
        double fl = catchInTons(N, S, M, W, xl) - TAC;
        double f = -TAC;
        double dx=0;
        double stockbiomass=0.0;
        for( int a = 0; a < S.length; a++) stockbiomass+=(N[a]*W[a]);
        if(TAC>=stockbiomass) {
           System.out.println("Warning: trying to fish more than total stock size, using F=10.0");
           return 10;
        }
        for (int j = 0; j < MAXITER; j++) {
            assert (f-fl)!=0.0 : "divide by zero";
            assert (!Double.isInfinite(f+fl+xl+rts+dx));
            assert (!Double.isNaN(f+fl+xl+rts+dx));
            dx = (xl - rts) * f / (f - fl);
            xl = rts;
            fl = f;
            rts += dx;
            //System.out.println(j+": "+rts+"\t"+f);
            if (rts<0.0) rts=0; // 2006-04-23: If rts is < 0 the following call to catchInTons will not work
            f = catchInTons(N, S, M, W, rts) - TAC;
            if (Math.abs(dx) < XACC || f == 0.0)
                return rts;
        }
        assert (Math.abs(f)<1) : "not even close: "+f;
        return rts;
    }
    
	public static double catchInTons(double[] N, double[] S, double[] M,
			double[] W, double flevel) {
		double tons = 0;
		double F, Z;
		for (int a = 0; a < S.length; a++) {
			F = S[a] * flevel;
			Z = F + M[a];
            assert Z>0 : "Total mortality must be positive" ;
			tons += W[a] * F * N[a] * (1 - Math.exp(-Z)) / Z;
		}
        assert(tons>=0.0);
		return tons;
	}

}
