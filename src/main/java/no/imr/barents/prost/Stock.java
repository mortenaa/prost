package no.imr.barents.prost;

// version 26 aug 2003

interface Stock {
	public double[] getN(int year);

	public double[] getF(int year);

	public double[] getM(int year);

	public double[] getSw(int year);

	public double[] getCw(int year);

	public double[] getMat(int year);

	public int getFbarMin();

	public int getFbarMax();

	public int getMinAge();

	public int getMaxage();

	public double getRec(int year);

	public int minage();

	public int ages();

	public int years();

	public int firstyear();

	public int realYear();

	public int recruitedYears(int year);

	public double getBpa();

	public double getBlim();

}
