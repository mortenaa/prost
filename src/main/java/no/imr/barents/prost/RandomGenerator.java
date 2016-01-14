package no.imr.barents.prost;

import java.util.Random;

class RandomGenerator {
	static Random rand;

	public RandomGenerator() {
	}

	public static void create() {
		rand = new Random();
	}

	public static void create(long seed) {
		rand = new Random(seed);
	}

	public static double nextGaussian() {
		return rand.nextGaussian();
	}

	public static double nextNormal(double mean, double sd) {
		return rand.nextGaussian() * sd + mean;
	}
	
	public static int nextInt(int n) {
		return rand.nextInt(n);
	}
    
    public static double nextDouble() {
        return rand.nextDouble();
    }

	/**
	 * Generates a multivariate random lognormal variable X
	 * 
	 * @param mu
	 *            array of means of log(X)
	 * @param cv
	 *            covariance matrix of log(X)
	 * @return the multivariate lognormal variable X
	 */
	public static double[] nextMultiLognormal(double[] mu, double[][] cv) {
		double[] r = new double[mu.length]; // to be returned
		r = nextMultivariate(mu, cv);
		for (int i = 0; i < mu.length; i++)
			r[i] = Math.exp(r[i]);
		return r;
	}

	/**
	 * Generates a multivariate random normal variable
	 * 
	 * @param mu
	 *            array of means
	 * @param cv
	 *            covariance matrix
	 * @return a multivariate gaussian variable with mean mu and cov matrix cv
	 */
	public static double[] nextMultivariate(double[] mu, double[][] cv) {
		int n = mu.length;
		double[] r = new double[n]; // to be returned
		double[][] inv = new double[n][n];
		double s1, u1;

		s1 = Math.sqrt(cv[0][0]);
		r[0] = nextNormal(mu[0], s1);
		for (int i = 2; i <= n; i++) {
			inv = invSymmMatr(cv, i);
			s1 = Math.sqrt(1.0 / inv[i - 1][i - 1]);
			u1 = mu[i - 1];
			for (int j = 1; j < i; j++)
				u1 -= inv[i - 1][j - 1] / inv[i - 1][i - 1]
						* (r[j - 1] - mu[j - 1]);
			r[i - 1] = nextNormal(u1, s1);
		}
		return r;
	}

	/**
	 * Inverts the n x n upper left part of the symmetrical matrix B; Used in
	 * nextMultivariate. Original source: J.C.Nash: Compact numerical methods
	 * for computers Adam Hilger, Bristol 1979. Copied from rB.F.J.Manly:
	 * Randomisation and Monte Carlo methods in biology. Chapman & Hall London
	 * 1994
	 * 
	 * @param B
	 *            a symmetric matrix
	 * @param n
	 *            the submatrix B[n][n] is inverted (upper left part of B)
	 * @return the inverted n x n matrix
	 */
	private static double[][] invSymmMatr(double[][] B, int n) {
		double[] wa = new double[n * (n + 1) / 2]; // auxilary array
		double[] wb = new double[n]; // auxilary array
		double[][] A = new double[n][n]; // to be returned
		int k, m, iq = 0;
		double s, t;
		if (n == 1) {
			if (B[0][0] == 0)
				System.out.println("Error: Singular matrix");
			A[0][0] = 1.0 / B[0][0];
			return A;
		}

		for (int i = 1; i <= n; i++) {
			for (int j = 1; j <= i; j++) {
				wa[i * (i - 1) / 2 + j - 1] = B[i - 1][j - 1];
			}
		}
		for (int ka = 1; ka <= n; ka++) {
			k = n + 1 - ka;
			s = wa[0];
			m = 1;
			for (int i = 2; i <= n; i++) {
				iq = m;
				m = m + i;
				t = wa[iq];
				if (s == 0.0)
					System.out.println("Error: Singular matrix");
				wb[i - 1] = -t / s;
				if (i > k)
					wb[i - 1] = -wb[i - 1];
				for (int j = iq + 2; j <= m; j++)
					wa[j - i - 1] = wa[j - 1] + t * wb[j - iq - 1];
			}
			iq = iq - 1;
			wa[m - 1] = 1.0 / s;
			for (int i = 2; i <= n; i++) {
				wa[iq + i - 1] = wb[i - 1];
			}
		}

		for (int i = 1; i <= n; i++) {
			for (int j = 1; j <= i; j++) {
				A[i - 1][j - 1] = wa[i * (i - 1) / 2 + j - 1];
				A[j - 1][i - 1] = A[i - 1][j - 1];
			}
		}
		return A;
	}
}
