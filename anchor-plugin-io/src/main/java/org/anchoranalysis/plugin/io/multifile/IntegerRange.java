/* (C)2020 */
package org.anchoranalysis.plugin.io.multifile;

public class IntegerRange {

    private int size;
    private int deduct = 0;

    public IntegerRange(int maxNum) {
        this.size = maxNum;
    }

    public IntegerRange(int minNum, int maxNum) {
        // We deduct the min
        deduct = minNum;
        this.size = maxNum - deduct + 1;
    }

    /**
     * Maps an original integer to it's index from (0..size]
     *
     * <p>Note that no error-checking occurs to ensure value is within range.
     *
     * @param value within range
     * @return the index of the value in the range
     */
    public int index(int val) {
        return val - deduct;
    }

    public int getSize() {
        return size;
    }
}
