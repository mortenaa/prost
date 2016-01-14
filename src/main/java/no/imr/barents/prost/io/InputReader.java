package no.imr.barents.prost.io;

import java.io.*;
import java.util.LinkedList;

public class InputReader {

	private String filename;
	private boolean verbose=false;

	BufferedReader in;

	LinkedList buffer;

	public void error(String error) {
		System.err.println("Error in file ["+filename+"]: " + error);
		System.exit(1);
	}

	public InputReader(String inputfilename, boolean verbose) {
		filename = inputfilename;
		try {
			in = new BufferedReader(new FileReader(new File(filename)));
		} catch (FileNotFoundException e) {
			error("no such file, " + inputfilename);
		}
		buffer = new LinkedList();
	}

	public boolean expectWord(String name) {
		String s = nextWord();
		if (s.equalsIgnoreCase(name))
			return true;
		else {
			error("Expected to find " + name + " but found " + s);
		}
		return false;
	}

	public int expectWord(String[] names) {
		String s = nextWord();
		for (int i = 0; i < names.length; i++)
			if (s.equalsIgnoreCase(names[i]))
				return i;
		String ss = "";
		for (int i = 0; i < names.length; i++)
			ss = ss + names[i] + " ";

		error("Expected to find one of ( " + ss + ") but found " + s);
		return -1;
	}

	public int expectInt(String name) {
		String s = nextWord();
		if (s.equalsIgnoreCase(name))
			return new Integer(nextWord()).intValue();
		else {
			error("Expected to find " + name + " but found " + s);
		}
		return -1;
	}

	public double expectDouble(String name) {
		String s = nextWord();
		if (s.equalsIgnoreCase(name))
			return new Double(nextWord()).doubleValue();
		else {
			error("Expected to find " + name + " but found " + s);
		}
		return -1;
	}
	
	public double expectOptionalDouble(String name, double defaultVal) {
		String s = nextWord();
		if (s.equalsIgnoreCase(name))
			return new Double(nextWord()).doubleValue();
		else {
		  	pushBackWord(s);
			return defaultVal;
		}
	}
	
	public int expectOptionalInt(String name, int defaultVal) {
		String s = nextWord();
		if (s.equalsIgnoreCase(name))
			return new Integer(nextWord()).intValue();
		else {
		  	pushBackWord(s);
			return defaultVal;
		}
	}
	
	public boolean expectOptionalKeyword(String name) {
		String s = nextWord();
		if (s.equalsIgnoreCase(name))
			return true;
		else {
			pushBackWord(s);
			return false;
		}
	}

	// needs expect quoted string aswell!!!!

	public String expectString(String name) {
		String s = nextWord();
		if (s.equalsIgnoreCase(name))
			return nextWord();
		else {
			error("Expected to find " + name + " but found " + s);
		}
		return null;
	}

	public double[][] expectMatrix(String name, int rows, int columns) {
		String s = nextWord();
		if (!s.equalsIgnoreCase(name))
			error("Expected to find " + name + " but found " + s);
		double[][] m = new double[rows][columns];
		for (int y = 0; y < rows; y++)
			for (int a = 0; a < columns; a++)
				try {
					m[y][a] = new Double(nextWord()).doubleValue();
				} catch (Exception e) {
					error("When reading matrix, " + e.toString());
				}
		return m;
	}

	public double[] expectVector(String name, int length) {
		String s = nextWord();
		if (!s.equalsIgnoreCase(name))
			error("Expected to find " + name + " but found " + s);
		double[] v = new double[length];
		for (int i = 0; i < length; i++)
			try {
				v[i] = new Double(nextWord()).doubleValue();
			} catch (Exception e) {
				error("When reading matrix, " + e.toString());
			}
		return v;
	}
	
	public double[] expectOptionalVector(String name, int length, double defaultVal) {
		double[] v = new double[length];
		String s = nextWord();
		if (!s.equalsIgnoreCase(name)) {
		  for (int i = 0; i < length; i++)
		    v[i]=defaultVal;
		  pushBackWord(s);
		  return v;
		}
		for (int i = 0; i < length; i++)
			try {
				v[i] = new Double(nextWord()).doubleValue();
			} catch (Exception e) {
				error("When reading matrix, " + e.toString());
			}
		return v;
	}

	public String nextWord() {
		String s = null;
		while (buffer.size() == 0) {
			try {
				s = in.readLine();
			} catch (IOException e) {
				error(e.toString());
			}
			if (s == null)
				return null;
			String[] tmp = s.trim().toLowerCase().split(";");
			if (tmp.length == 0 || tmp[0].length() == 0)
				continue;
			String[] words = tmp[0].split("[ \t]+");
			for (int i = 0; i < words.length; i++) {
				buffer.addLast(words[i]);
			} //buffer.addLast("\n");
			if (buffer.size() > 0)
				return (String) buffer.removeFirst();
		}
		return (String) buffer.removeFirst();
	}
	
	public void pushBackWord(String word) {
	  buffer.addFirst(word);
	}

	public String[] nextLine() {
		String[] words = {};
		if (buffer.size() > 0) {
			buffer.toArray(words);
			buffer.clear();
			return words;
		}
		String s = null;
		while (words.length == 0) {
			try {
				s = in.readLine();
			} catch (IOException e) {
				error(e.getMessage());
			}
			if (s == null)
				return null;
			String[] tmp = s.trim().toLowerCase().split(";");
			if (tmp[0].length() == 0)
				continue;
			words = tmp[0].split("[ \t]+");
			return words;
		}
		return null;
	}
}
