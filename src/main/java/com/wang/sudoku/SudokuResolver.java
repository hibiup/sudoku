package com.wang.sudoku;

import org.apache.log4j.Logger;

/**
 * Hello world!
 */
public class SudokuResolver {
	Logger logger = Logger.getLogger(this.getClass().toString());

	static final int unit_size = 3;
	static final int group_size = unit_size * unit_size;
	static final int amount = group_size * group_size;

	boolean modified = false;

	enum Direction {
		HORIZONTAL, VERTICAL
	};

	int matrix[] = null;

	public SudokuResolver(int matrix[]) {
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
	 * Count for number big than 0
	 * 
	 * @param mask
	 * @return
	 */
	int countAvailable(int[] mask) {
		int available = 0;
		for (int i = 0; i < mask.length; i++) {
			if (1 <= mask[i]) {
				available++;
			}
		}
		return available;
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
		return amount == countAvailable(matrix);
	}

	/**
	 * Fill a number to the Matrix.
	 * 
	 * @param number
	 */
	public void fillNumber(final int number) {
		for (int cube = 0; cube < group_size; cube++) {
			// Get a cube
			int[] mask1 = prepareCubeMask(checkCube(number, cube), cube);

			if (countAvailable(mask1) > 0) {
				// Check horizontal
				int line_base = getGlobalPositionForCube(0, cube) / group_size;

				for (int line = 0 + line_base; line < unit_size + line_base; line++) {
					int[] mask2 = prepareLineMask(checkLine(number, line, SudokuResolver.Direction.HORIZONTAL), line,
							SudokuResolver.Direction.HORIZONTAL);
					mask1 = applyMask(mask1, mask2);
				}

				// Check vertical
				line_base = getGlobalPositionForCube(0, cube) % group_size;
				for (int line = 0 + line_base; line < unit_size + line_base; line++) {
					int[] mask2 = prepareLineMask(checkLine(number, line, SudokuResolver.Direction.VERTICAL), line,
							SudokuResolver.Direction.VERTICAL);
					mask1 = applyMask(mask1, mask2);
				}
			}

			//printMatrix(mask1);
			mask1 = cropCube(mask1, cube, number);

			if (1 == countAvailable(mask1)) {
				addToMatrix(mask1);
			}
		}
	}

	protected void printMatrix(int[] matrix) {
		System.out.println("------------------------------------------------------");
		for (int i = 1; i < amount + 1; i++) {
			System.out.format("%2d", matrix[i - 1]);
			if (0 == i % group_size) {
				System.out.println();
			}
			else {
				System.out.print("|");
			}
		}
		System.out.println("------------------------------------------------------");
	}

	/**
	 * Game entrance
	 */
	public void play() {
		do {
			resetModified();
			for (int i = 1; i <= group_size; i++) {
				fillNumber(i);
				if (logger.isDebugEnabled())
					printMatrix(matrix);
				if (isDone())
					break;
			}
			// End loop if game finished or no number could be signed in
		} while (!isDone() && isModified());
	}
}