package com.wang.sudoku;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for simple App.
 */
public class AppTest extends TestCase {
	/*
	  0, 5, 0, 2, 0, 0, 0, 1, 4,
	  0, 1, 8, 0, 6, 3, 0, 9, 0,
	  0, 2, 0, 0, 0, 4, 8, 5, 0,
	  2, 3, 4, 0, 9, 0, 0, 0, 1,
	  0, 0, 0, 0, 0, 0, 0, 0, 0,
	  8, 0, 0, 0, 7, 0, 4, 2, 9,
	  0, 8, 5, 1, 0, 0, 0, 3, 0,
	  0, 4, 0, 9, 3, 0, 7, 8, 0,
	  3, 9, 0, 0, 0, 2, 0, 4, 0
	 */
	int cell[] = new int[] { 0, 5, 0, 2, 0, 0, 0, 1, 4, 0, 1, 8, 0, 6, 3, 0, 9, 0, 0, 2, 0, 0, 0, 4, 8, 5, 0, 2, 3, 4,
			0, 9, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 8, 0, 0, 0, 7, 0, 4, 2, 9, 0, 8, 5, 1, 0, 0, 0, 3, 0, 0, 4, 0,
			9, 3, 0, 7, 8, 0, 3, 9, 0, 0, 0, 2, 0, 4, 0 };

	/**
	 * Create the test case
	 *
	 * @param testName
	 *            name of the test case
	 */
	public AppTest(String testName) {
		super(testName);
	}

	/**
	 * @return the suite of tests being tested
	 */
	public static Test suite() {
		return new TestSuite(AppTest.class);
	}

	/**
	 * Rigourous Test :-)
	 */
	public void testCheckStraight() {
		SudokuResolver app = new SudokuResolver(cell);
		int[] indexes = app.checkLine(1, 0, SudokuResolver.Direction.HORIZONTAL);
		app.prepareLineMask(indexes, 0, SudokuResolver.Direction.HORIZONTAL);
	}

	public void testCheckCube() {
		SudokuResolver app = new SudokuResolver(cell);
		int[] indexes = app.checkCube(1, 1);
		app.prepareCubeMask(indexes, 1);
	}

	public void testApplyMask() {
		SudokuResolver app = new SudokuResolver(cell);
		int[] mask1 = app.prepareCubeMask(app.checkCube(1, 1), 1);
		int[] mask2 = app.prepareLineMask(app.checkLine(1, 0, SudokuResolver.Direction.HORIZONTAL), 0,
				SudokuResolver.Direction.HORIZONTAL);
		mask1 = app.applyMask(mask1, mask2);
		app.printMatrix(mask1);

		mask2 = app.prepareLineMask(app.checkLine(1, 1, SudokuResolver.Direction.HORIZONTAL), 1,
				SudokuResolver.Direction.HORIZONTAL);
		mask1 = app.applyMask(mask1, mask2);
		app.printMatrix(mask1);

		mask2 = app.prepareLineMask(app.checkLine(1, 2, SudokuResolver.Direction.HORIZONTAL), 2,
				SudokuResolver.Direction.HORIZONTAL);
		mask1 = app.applyMask(mask1, mask2);
		app.printMatrix(mask1);

		mask2 = app.prepareLineMask(app.checkLine(1, 3, SudokuResolver.Direction.VERTICAL), 3,
				SudokuResolver.Direction.VERTICAL);
		mask1 = app.applyMask(mask1, mask2);
		app.printMatrix(mask1);

		mask2 = app.prepareLineMask(app.checkLine(1, 4, SudokuResolver.Direction.VERTICAL), 4,
				SudokuResolver.Direction.VERTICAL);
		mask1 = app.applyMask(mask1, mask2);
		app.printMatrix(mask1);

		mask2 = app.prepareLineMask(app.checkLine(1, 5, SudokuResolver.Direction.VERTICAL), 5,
				SudokuResolver.Direction.VERTICAL);
		mask1 = app.applyMask(mask1, mask2);
		app.printMatrix(mask1);

		app.printMatrix(app.cropCube(mask1, 1, 1));
	}

	//Easy
	public void testEasy() {
		/*
		  0, 0, 0, 2, 0, 3, 7, 0, 1,
		  0, 0, 0, 0, 0, 1, 9, 6, 0,
		  0, 4, 0, 7, 8, 0, 0, 0, 3,
		  1, 0, 0, 0, 9, 0, 3, 0, 0,
		  5, 0, 0, 1, 3, 2, 0, 0, 4,
		  0, 0, 3, 0, 7, 0, 0, 0, 5,
		  3, 0, 0, 0, 1, 8, 0, 5, 0,
		  0, 1, 9, 3, 0, 0, 0, 0, 0,
		  7, 0, 5, 6, 0, 4, 0, 0, 0
		 */
		SudokuResolver app = new SudokuResolver(new int[] { 0, 0, 0, 2, 0, 3, 7, 0, 1, 0, 0, 0, 0, 0, 1, 9, 6, 0, 0, 4,
				0, 7, 8, 0, 0, 0, 3, 1, 0, 0, 0, 9, 0, 3, 0, 0, 5, 0, 0, 1, 3, 2, 0, 0, 4, 0, 0, 3, 0, 7, 0, 0, 0, 5, 3,
				0, 0, 0, 1, 8, 0, 5, 0, 0, 1, 9, 3, 0, 0, 0, 0, 0, 7, 0, 5, 6, 0, 4, 0, 0, 0 });
		app.play();
	}

	// Evil
	public void testEvil() {
		/*
		  1, 0, 0, 0, 0, 5, 0, 0, 0,
		  0, 0, 0, 8, 0, 0, 1, 0, 0,
		  0, 9, 7, 0, 3, 4, 0, 0, 0,
		  0, 0, 9, 0, 0, 0, 0, 1, 3,
		  6, 0, 4, 0, 0, 0, 9, 0, 7,
		  7, 8, 0, 0, 0, 0, 4, 0, 0,
		  0, 0, 0, 2, 4, 0, 8, 6, 0,
		  0, 0, 6, 0, 0, 1, 0, 0, 0,
		  0, 0, 0, 9, 0, 0, 0, 0, 4
		 */
		SudokuResolver app = new SudokuResolver(new int[] { 1, 0, 0, 0, 0, 5, 0, 0, 0, 0, 0, 0, 8, 0, 0, 1, 0, 0, 0, 9,
				7, 0, 3, 4, 0, 0, 0, 0, 0, 9, 0, 0, 0, 0, 1, 3, 6, 0, 4, 0, 0, 0, 9, 0, 7, 7, 8, 0, 0, 0, 0, 4, 0, 0, 0,
				0, 0, 2, 4, 0, 8, 6, 0, 0, 0, 6, 0, 0, 1, 0, 0, 0, 0, 0, 0, 9, 0, 0, 0, 0, 4 });
		app.play();
	}
}
