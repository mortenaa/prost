package no.imr.barents.prost.io;

import java.io.PrintWriter;

public class OutputWriter extends PrintWriter {
	public OutputWriter(java.io.Writer out) {
		super(out);
	}

	public void printMatrix(double[][] m) {
		int rows = m.length - 1;
		int cols = m[0].length;
		for (int i = 0; i < cols; i++) {
			for (int j = 0; j < rows; j++)
				print(m[j][i] + ", ");
			println(m[rows][i]);
		}
	}

	public void printVector(double[] v) {
		int size = v.length - 1;
		for (int i = 0; i < size; i++)
			print(v[i] + ", ");
		println(v[size]);
	}

	public void printVector(double[] v, int firstyear) {
		int size = v.length;
		for (int i = 0; i < size; i++)
			print(", " + (firstyear + i));
		println();
		for (int i = 0; i < size; i++)
			print(", " + v[i]);
		println();
	}

	public void printIndexVector(double[] v, int firstyear, int index) {
		int size = v.length;
		print(index);
		for (int i = 0; i < size; i++)
			print(", " + v[i]);
		println();
	}

	public void printMatrix(double[][] m, int firstage, int firstyear) {
		int rows = m.length;
		int cols = m[0].length;
		for (int i = 0; i < rows; i++)
			print(", " + (firstyear + i));
		println();
		for (int i = 0; i < cols; i++) {
			print(firstage + i);
			for (int j = 0; j < rows; j++)
				print(", " + m[j][i]);
			println();
		}
	}

	public void printHead(int firstyear, int years, String title) {
		println(title);
		for (int i = 0; i < years; i++)
			print(", " + (firstyear + i));
		println();
	}
}
