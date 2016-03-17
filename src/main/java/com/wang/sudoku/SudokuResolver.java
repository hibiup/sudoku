package com.wang.sudoku;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * Hello world!
 */
public class SudokuResolver {
	static int recursionTimes = 0;
	static int maskTimes = 0;
	Logger logger = Logger.getLogger(this.getClass().toString());

	static final int unit_size = 3;
	static final int group_size = unit_size * unit_size;
	static final int amount = group_size * group_size;

	boolean modified = false;

	enum Direction {
		HORIZONTAL, VERTICAL
	};

	Integer[][][] hitOptions = new Integer[group_size][group_size][];
	int[][] cubeShadow = new int[group_size][];
	int matrix[] = null;

	public SudokuResolver(int matrix[]) {
		setSudoku(matrix);
	}

	public void setSudoku(int matrix[]) {
		this.matrix = matrix;
	}

	/**
	 * Convert position in line to global
	 * 
	 * @param pos
	 * @param line
	 * @param dir
	 * @return
	 */
	int getGlobalPositionForLine(int pos, int line, Direction dir) {
		if (Direction.HORIZONTAL == dir) {
			return group_size * line + pos;
		}
		else {
			return line + group_size * pos;
		}
	}

	/**
	 * Convert position in cube to global
	 * 
	 * @param pos
	 * @param cube
	 * @return
	 */
	int getGlobalPositionForCube(int pos, int cube) {
		int index = (unit_size * unit_size) * (cube / unit_size * unit_size) + (cube % unit_size * unit_size);
		int subIndex = (unit_size * unit_size) * (pos / unit_size) + (pos % unit_size);
		return index + subIndex;
	}

	/**
	 * Check a line to find out all possible place(s) for the number. The result is the index(es) for the place(s)
	 * 
	 * @param number
	 *            The number you need to check
	 * @param line
	 *            Which line you prefer to check
	 * @param dir
	 *            Which direction for the line.
	 * @return
	 */
	int[] checkLine(int number, int line, Direction dir) {
		int[] indexes = new int[group_size];
		int cal = 0;

		for (int pos = 0; pos < group_size; pos++) {
			int index = getGlobalPositionForLine(pos, line, dir);

			cal = number ^ matrix[index];

			if (0 == cal) {
				// If find same number
				indexes = new int[group_size];
				break;
			}
			else if (number == cal) {
				indexes[pos] = number;
			}
		}

		return indexes;
	}

	/**
	 * Make a mask for a line. As the line index is from 0 to #group_size, we need to find their actual position(s) in
	 * the matrix.
	 * 
	 * @param indexes
	 *            Line index(es)
	 * @param line
	 *            Which line, from 0 to #group_size
	 * @param dir
	 *            Which direction.
	 * @return Mask.
	 *         -1: indicate this position is possible
	 *         1: is impossible
	 *         0: not sure
	 */
	int[] prepareLineMask(final int[] indexes, int line, Direction dir) {
		int[] cells = new int[amount];

		for (int i = 0; i < indexes.length; i++) {
			int pos = getGlobalPositionForLine(i, line, dir);
			if (0 == indexes[i]) {
				// If unavailable
				cells[pos] = -1;
			}
			else {
				// If available
				cells[pos] = 1;
			}
		}

		return cells;
	}

	/**
	 * Check a cube to find out all possible place(s) for the number. The result is the index(es) for the place(s)
	 * 
	 * @param number
	 * @param cube
	 * @return
	 */
	int[] checkCube(int number, int cube) {
		int[] indexes = new int[group_size];
		int cal = 0;

		for (int j = 0; j < group_size; j++) {
			int index = getGlobalPositionForCube(j, cube);
			cal = number ^ matrix[index];

			if (0 == cal) {
				indexes = new int[group_size];
				break;
			}
			else if (number == cal)
				indexes[j] = number;
		}

		return indexes;
	}

	/**
	 * Make a mask for a cube. As the cube index is from 0 to #group_size, we need to find their actual position(s) in
	 * the matrix.
	 * 
	 * @param indexes
	 * @param cube
	 * @return Mask.
	 *         -1: indicate this position is possible
	 *         1: is impossible
	 *         0: not sure
	 */
	int[] prepareCubeMask(final int[] indexes, int cube) {
		int[] cells = new int[amount];

		for (int i = 0; i < indexes.length; i++) {
			int pos = getGlobalPositionForCube(i, cube);
			if (0 == indexes[i]) {
				cells[pos] = -1;
			}
			else {
				cells[pos] = 1;
			}
		}

		return cells;
	}

	/**
	 * Add two masks together. The place(s) has been indicated as "possible(1)" in both masks will be set as
	 * "possible(1)" in the new mask. "impossible(-1)" for any one will be addressed as "impossible(-1)". And
	 * "not sure(0)" for if both for "not sure(0)"
	 * 
	 * @param mask1
	 * @param mask2
	 * @return
	 */
	int[] applyMask(int mask1[], int mask2[]) {
		int[] mask = new int[amount];
		for (int i = 0; i < amount; i++) {
			mask[i] = mask1[i] + mask2[i];
			if (mask[i] > 0) {
				// If possible for both
				mask[i] = 1;
			}
			else if (mask[i] < 0) {
				// If impossible for both
				mask[i] = -1;
			}
			else {
				if (mask1[i] == mask2[i]) {
					// If not sure for both
					mask[i] = 0;
				}
				else {
					// impossible for any one
					mask[i] = -1;
				}
			}
		}

		return mask;
	}

	/**
	 * Crop a cube and apply the number in
	 * 
	 * @param mask
	 * @param cube
	 * @param number
	 * @return
	 */
	int[] cropCube(int mask[], int cube, int number) {
		int[] indexes = new int[amount];

		for (int j = 0; j < group_size; j++) {
			int index = getGlobalPositionForCube(j, cube);
			if (1 == mask[index]) {
				indexes[index] = number;
			}
		}

		return indexes;
	}

	/**
	 * Count a specific number or which big than 0
	 * 
	 * @param mask
	 * @param number
	 * @return
	 */
	Integer[] countAvailable(int[] mask, Integer number) {
		List<Integer> offset = new ArrayList<Integer>();
		int[] cells = (null == mask ? matrix : mask);

		for (int i = 0; i < cells.length; i++) {
			if (null != number) {
				if (number.intValue() == cells[i]) {
					offset.add(i);
				}
			}
			else if (1 <= cells[i]) {
				offset.add(i);
			}
		}

		return toArray(offset);
	}

	Integer[] toArray(List<Integer> list) {
		Integer[] arr = new Integer[list.size()];
		return list.toArray(arr);
	}

	/**
	 * Crop a line and apply the number in
	 * 
	 * @param mask
	 * @param line
	 * @param number
	 * @param dir
	 * @return
	 */
	int[] cropLine(int mask[], int line, int number, Direction dir) {
		int[] indexes = new int[group_size];

		for (int j = 0; j < group_size; j++) {
			int index = getGlobalPositionForLine(j, line, dir);
			if (1 == mask[index]) {
				indexes[index] = number;
			}
		}

		return indexes;
	}

	/**
	 * Add result to Matrix
	 * 
	 * @param cells
	 */
	void addToMatrix(int[] cells) {
		for (int i = 0; i < amount; i++) {
			if (0 != cells[i]) {
				modified = true;
				matrix[i] = matrix[i] + cells[i];
			}
		}
	}

	/**
	 * Reset the modified flag
	 */
	void resetModified() {
		modified = false;
	}

	/**
	 * Has the matrix been modified
	 * 
	 * @return
	 */
	boolean isModified() {
		if (modified) {
			return true;
		}
		return false;
	}

	/**
	 * Check have all fields been filled
	 * 
	 * @return
	 */
	public boolean isDone() {
		return amount == countAvailable(matrix, null).length;
	}

	/**
	 * @param mask
	 * @param number
	 * @param cube
	 * @return
	 */
	int[] checkHorizontalLines(int[] mask, final int number, final int cube) {
		int line_base = getGlobalPositionForCube(0, cube) / group_size;

		for (int line = 0 + line_base; line < unit_size + line_base; line++) {
			int[] mask2 = prepareLineMask(checkLine(number, line, SudokuResolver.Direction.HORIZONTAL), line,
					SudokuResolver.Direction.HORIZONTAL);
			mask = applyMask(mask, mask2);
		}

		return mask;
	}

	/**
	 * @param mask
	 * @param number
	 * @param cube
	 * @return
	 */
	int[] checkVerticalLines(int[] mask, final int number, final int cube) {
		int line_base = getGlobalPositionForCube(0, cube) % group_size;
		for (int line = 0 + line_base; line < unit_size + line_base; line++) {
			int[] mask2 = prepareLineMask(checkLine(number, line, SudokuResolver.Direction.VERTICAL), line,
					SudokuResolver.Direction.VERTICAL);
			mask = applyMask(mask, mask2);
		}

		return mask;
	}

	/**
	 * Check cubes from vertical direction
	 * 
	 * @param mask
	 * @param cube
	 * @return
	 */
	int[] checkVerticalCubes(int[] mask, int cube) {
		int shadowIndex = cube;
		while ((shadowIndex = shadowIndex - unit_size) >= 0 && cube != 0) {
			if (null != cubeShadow[shadowIndex]) {
				logger.debug("Check shadow cube: " + shadowIndex);
				int row = -1;
				for (int i = 0; i < amount; i++) {
					if (0 != cubeShadow[shadowIndex][i]) {
						if (row != -1 && row != i % group_size) {
							// If the number is not all in same line.
							return mask;
						}
						row = i % group_size;
					}
				}

				// If all the number is in same colum, we can believe this colum will be occupied, then we need mark it out from the mask.
				if (row != -1) {
					for (int i = row; i < amount; i += group_size) {
						mask[i] = -1;
					}
				}
			}
		}

		return mask;
	}

	/**
	 * Check cubes from horizontal direction
	 * 
	 * @param mask
	 * @param cube
	 * @return
	 */
	int[] checkHorizontalCubes(int[] mask, int cube) {
		int shadowIndex = cube - cube % unit_size;
		while (shadowIndex < cube) {
			if (null != cubeShadow[shadowIndex]) {
				logger.debug("Check shadow cube: " + shadowIndex);
				int colum = -1;
				for (int i = 0; i < amount; i++) {
					if (0 != cubeShadow[shadowIndex][i]) {
						if (colum != -1 && colum != i / group_size) {
							// If the number is not all in same line. we can't determine which line will be occupied.
							return mask;
						}
						colum = i / group_size;
					}
				}

				// If all the number is in same line, we can believe this line will be occupied, then we need mark it out from the mask.
				if (colum != -1) {
					for (int i = colum * group_size; i < (colum + 1) * group_size; i++) {
						mask[i] = -1;
					}
				}

				shadowIndex++;
			}
		}

		return mask;
	}

	/**
	 * Fill a number to the Matrix.
	 * 
	 * @param number
	 */
	void fillNumber(final int number) {
		++maskTimes;
		for (int cube = 0; cube < group_size; cube++) {
			// Get a cube
			int[] mask = prepareCubeMask(checkCube(number, cube), cube);

			if (countAvailable(mask, null).length > 0) {
				// Check horizontal
				mask = checkHorizontalLines(mask, number, cube);
				// Check vertical
				mask = checkVerticalLines(mask, number, cube);

				// Check cube shadow
				mask = checkHorizontalCubes(mask, cube);
				mask = checkVerticalCubes(mask, cube);
			}

			//printMatrix(mask1);
			cubeShadow[cube] = cropCube(mask, cube, number);
			hitOptions[number - 1][cube] = countAvailable(cubeShadow[cube], null);

			if (1 == hitOptions[number - 1][cube].length) {
				addToMatrix(cubeShadow[cube]);
			}
		}
	}

	protected void printMatrix(int[] matrix) {
		int[] cells = null == matrix ? this.matrix : matrix;

		System.out.println("------------------------------------------------------");
		for (int i = 1; i < amount + 1; i++) {
			System.out.format("%2d", cells[i - 1]);
			if (0 == i % group_size) {
				System.out.println(",");
			}
			else {
				System.out.print(",");
			}
		}
		System.out.println("------------------------------------------------------");
	}

	/**
	 * Generate a new sudoku based on the old one
	 * 
	 * @return
	 */
	int[] generateNewSudoku(int index) {
		int chosenNumber = 1;
		int chosenCube = 0;
		int minimal = 0;

		// To find a number with minimal option
		for (int number = 1; number <= group_size; number++) {
			for (int cube = 0; cube < group_size; cube++) {
				Integer[] offsets = hitOptions[number - 1][cube];
				if (null != offsets && offsets.length >= 2 && (minimal == 0 || offsets.length < minimal)) {
					minimal = offsets.length;
					chosenNumber = number;
					chosenCube = cube;
				}
			}
		}

		// If no number can be choose
		if (0 == minimal)
			return null;

		/*List<Integer> optionList = Arrays.asList(hitOptions[chosenNumber - 1][chosenCube]);
		optionList = new ArrayList<Integer>(optionList);
		int randomNum = 0 + (int) (Math.random() * optionList.size());
		int option = optionList.remove(randomNum);
		
		if (0 == optionList.size())
			hitOptions[chosenNumber - 1][chosenCube] = null;
		else
			hitOptions[chosenNumber - 1][chosenCube] = toArray(optionList);*/

		// If the option list is end
		if (hitOptions[chosenNumber - 1][chosenCube].length <= index)
			return null;

		// Fill the chosen number to the old sudoku
		Integer option = hitOptions[chosenNumber - 1][chosenCube][index];

		int[] newMatrix = matrix.clone();
		newMatrix[option] = chosenNumber;
		logger.debug("Generate a new sudoku!");
		if (logger.isDebugEnabled()) {
			printMatrix(newMatrix);
		}

		return newMatrix;
	}

	/**
	 * Game entrance
	 */
	boolean play() {
		++recursionTimes;
		do {
			resetModified();
			for (int number = 1; number < group_size + 1; number++) {
				if (9 == countAvailable(null, number).length) {
					// Skip, if the number is done.
					logger.debug("Number: " + number + " is skiped.");
					continue;
				}
				fillNumber(number);

				if (logger.isDebugEnabled()) {
					System.out.println(" Number: " + number);
					printMatrix(matrix);
					System.out.println(" Updated: " + isModified());
				}

				if (isDone())
					break;
			}

			if (!isModified()) {
				return doRecursion();
			}
		} while (!isDone());

		return isDone();
	}

	/**
	 * @return
	 */
	boolean doRecursion() {
		int index = 0;
		// If game is not end but no number is obvious, we have to try to fill a number randomly
		int[] newSudoku = generateNewSudoku(index++);
		while (null != newSudoku) {
			// If new Sudoku is able to generated, start recursion.
			SudokuResolver newResolver = new SudokuResolver(newSudoku);
			if (newResolver.play()) {
				// If the game is finished without failed.
				this.matrix = newResolver.matrix;
				break;
			}
			else {
				// try next randomly Sudoku
				newSudoku = generateNewSudoku(index++);
			}
		}

		if (null == newSudoku)
			// If no more Sudoku could been generated, let's go back to the up level of the recursion.
			return false;

		return true;
	}

	public int[] getMatrix() {
		return matrix;
	}
}
