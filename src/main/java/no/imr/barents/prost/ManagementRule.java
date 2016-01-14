package no.imr.barents.prost;

abstract class ManagementRule {

	public abstract double[] generate(Stock s, int year);

	public abstract void readFromFile(InputReader in, Stock s);

	public abstract void print(OutputWriter out);

	public abstract void printHeader(OutputWriter out);

	public double[] getF() {
		return null;
	}
    public abstract double getTAC();
	public int[] getRules() {
		return null;
	};

	public void reset() {
	};
	
	public int lookaheadYears() {
		return 0;
	}

	public final static ManagementRule createFromFile(InputReader in, Stock s) {
		String type = in.expectString("type").toLowerCase();
		ManagementRule m = null;
		if (type.equals("constantf"))
			m = new ConstantFRule();
		else if (type.equals("3year"))
			m = new LookaheadRule();
		else if (type.equals("lookahead"))
			m = new LookaheadRule();
		else if (type.equals("tac"))
			m = new TacRule();
		else
			in.error("No such Management rule type, " + type);

		m.readFromFile(in, s);
		return m;
	}

}
