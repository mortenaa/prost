package no.imr.barents.prost;

import java.io.BufferedWriter;
import java.io.FileWriter;

public class Prost {
	
	public final static String VERSION = "0.4.1  (2014-01-14)";
	
	private static boolean verbose = false;

	public static void printUsage() {
		System.out
				.println("Usage: {java-invocation} prost.jar [-i simulations] [-h] [-o] [-r seed][-f val]");
	}

	public static void main(String[] args) {

		boolean doFullPrinting = false;
		String outfile = "out.csv";
		int iterations = 100;
		int socket = -1;
        long seed=-1;
		boolean doSocket = false;
        boolean doSeed  = false;
        boolean doFVal = false;
        
        double fVal=0;
        String filePrefix="";
		int argind = 0;
		while (argind < args.length) {
			if (args[argind].equals("-i") && argind < args.length - 1) {
				iterations = Integer.parseInt(args[argind + 1]);
				argind += 2;
			} else if (args[argind].equals("-h")) {
				printUsage();
				System.exit(1);
			} else if (args[argind].equals("-o")) {
				doFullPrinting = true;
				argind++;
			} else if (args[argind].equals("-v")) {
				Prost.verbose = true;
				argind++;				
			} else if (args[argind].equals("-s") && argind < args.length - 1) {
				socket = Integer.parseInt(args[argind + 1]);
				doSocket = true;
				argind += 2;
            } else if (args[argind].equals("-r") && argind < args.length - 1) {
                seed = Long.parseLong(args[argind + 1]);
                doSeed=true;
                argind += 2;			
            } else if (args[argind].equals("-f") && argind < args.length - 1) {
                fVal=Double.parseDouble(args[argind+1]);
                doFVal=true;
                filePrefix="F"+args[argind+1]+"-";
                argind+=2;
            } else {
				System.out.println("Error: Unkown or wrong number of arguments");
				printUsage();
				System.exit(1);
			}
		}

		System.out.println("Prost: Version "+VERSION);

        if (doSeed) {
		  RandomGenerator.create(seed);
        } else {
          RandomGenerator.create();
        }

		InputReader in = new InputReader("stock.dat", verbose);
		StockModel s = new StockModel();
		s.setVerbose(verbose);
		s.readFromFile(in);
		System.out.println("\n  **********   Read input files.   **********");
		
		if (doSocket) {
			s.doSocket(socket);
			s.initSocket();
		}
        
        if (doFVal) {
            s.setFAboveBpa(fVal);
        }

		FileWriter f = null;
		OutputWriter output = null;
		if (doFullPrinting) {
			try {
				f = new FileWriter(filePrefix+outfile);
			} catch (java.io.IOException e) {
				in.error("Could not open " + outfile + " for writing.");
			}
			output = new OutputWriter(new BufferedWriter(f));
			s.printInfo(output);
		}

		FileWriter f2 = null;
		FileWriter ff = null;
		FileWriter ffd = null;
		FileWriter fcatch = null;
		FileWriter fssb = null;
		FileWriter ftsb = null;
		FileWriter frecruit = null;
		try {
			f2 = new FileWriter(filePrefix+"summary.csv");
			ff = new FileWriter(filePrefix+"fishing.csv");
			ffd = new FileWriter(filePrefix+"distortedfishing.csv");
			fcatch = new FileWriter(filePrefix+"catch.csv");
			fssb = new FileWriter(filePrefix+"ssb.csv");
			ftsb = new FileWriter(filePrefix+"tsb.csv");
			frecruit = new FileWriter(filePrefix+"recruit.csv");
		} catch (java.io.IOException e) {
			in.error("Could not open file for writing.");
		}
		OutputWriter sumout = new OutputWriter(new BufferedWriter(f2));
		OutputWriter fout = new OutputWriter(new BufferedWriter(ff));
		OutputWriter fdout = new OutputWriter(new BufferedWriter(ffd));
		OutputWriter catchout = new OutputWriter(new BufferedWriter(fcatch));
		OutputWriter ssbout = new OutputWriter(new BufferedWriter(fssb));
		OutputWriter tsbout = new OutputWriter(new BufferedWriter(ftsb));
		OutputWriter recruitout = new OutputWriter(new BufferedWriter(frecruit));

		s.initSummary(iterations, sumout);
		s.initF(fout);
		s.initFd(fdout);
		s.initCatch(catchout);
		s.initSSB(ssbout);
		s.initTSB(tsbout);
		s.initRecruit(recruitout);
		System.out.println("  Starting simulations.");
		for (int i = 0; i < iterations; i++) {
			s.simulate();
			if (doFullPrinting) {
				output.println();
				output.println("Stock output, simulation, " + (i + 1));
				s.printStock(output);
			}
			s.doSummary(i);
			s.printF(fout, i + 1);
			s.printFd(fdout, i + 1);
			s.printCatch(catchout, i + 1);
			s.printSSB(ssbout, i + 1);
			s.printTSB(tsbout, i + 1);
			s.printRecruit(recruitout, i + 1);
		}
		s.printSummary(sumout);
		sumout.flush();
		if (doFullPrinting)
			output.flush();
		fout.flush();
		fdout.flush();
		catchout.flush();
		ssbout.flush();
		tsbout.flush();
		recruitout.flush();
		s.flushOutput();

		if (doSocket) {
			s.disposeSocket();
		}

		System.out.println("  Done, after " + iterations + " simulations.");

	}
	
	public static void logMessage(String s) {
		if (verbose) {
			System.out.println(s);
		}
	}
}
