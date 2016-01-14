package no.imr.barents.prost;

interface AdjustF {
	public double adjust(double F, double SSB);
}

class LinearAdjust implements AdjustF {
	private double bpa;

	private double bzero;

	public LinearAdjust(double bpa, double bzero) {
		this.bpa = bpa;
		this.bzero = bzero;
	}

	public double adjust(double F, double SSB) {
		double f = (SSB - bzero) * F / (bpa - bzero);
		if (f < 0)
			return 0;
		else
			return f;
	}
}

class LowAdjust implements AdjustF {
	private double fpa;

	public LowAdjust(double fpa) {
		this.fpa = fpa;
	}

	public double adjust(double F, double SSB) {
		return fpa;
	}
}

class NoAdjust implements AdjustF {
	public double adjust(double F, double SSB) {
		return F;
	}
}
