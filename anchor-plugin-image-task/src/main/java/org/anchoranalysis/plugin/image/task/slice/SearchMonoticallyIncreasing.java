package org.anchoranalysis.plugin.image.task.slice;

import lombok.RequiredArgsConstructor;

/**
 * Search for an optimal value between two bounds.
 *
 * <p>It assumes a <a
 * href="https://opencurriculum.org/5512/monotonically-increasing-and-decreasing-functions-an-algebraic-approach/">monotonically
 * increasing</a> value-function.
 *
 * <p>It begins at the minimum-bound, and in a similar manner to <a
 * href="https://en.wikipedia.org/wiki/Exponential_search">exponential search</a>, successively
 * doubles, until a lower and upper bound are found.
 *
 * <p>It then proceeds with a <a
 * href="https://en.wikipedia.org/wiki/Binary_search_algorithm">binary-search</a>-style recursive
 * evaluation to find the integer value that produces the exact closest value to {@code target}.
 *
 * @author Owen Feehan
 */
@RequiredArgsConstructor
class SearchMonoticallyIncreasing {

    /**
     * The function that calculates the value for a particular input {@code int}.
     *
     * <p>It must be <a
     * href="https://opencurriculum.org/5512/monotonically-increasing-and-decreasing-functions-an-algebraic-approach/">monotonically
     * increasing</a>.
     */
    @FunctionalInterface
    public interface ValueFunction {

        /**
         * Calculate the value.
         *
         * @param input the input integer.
         * @return the calculated value.
         */
        public double calculate(int input);
    }

    /**
     * The target value to try and be closest to when evaluating {@code calculateValue} on differing
     * inputs.
     */
    private final double target;

    /** Calculates a value for a particular input integer. */
    private final ValueFunction calculateValue;

    /**
     * Finds the input value that produces a calculated-value that is closest to {@code target}.
     *
     * @param boundLower a lower bound on the input range, inclusive.
     * @return the input-value that is optimal to produce the closest value to {@code target} when
     *     evaluated using {@code calculateValue}.
     */
    public int findOptimalInput(int boundLower) {
        int boundPrevious = boundLower;
        int bound = boundLower * 2;
        double height = calculateValue.calculate(bound);
        while (height < target) {
            boundPrevious = bound;
            bound *= 2;
            height = calculateValue.calculate(bound);
        }
        // The best input-value lies between boundPrev and bound, and can be found with binary
        // search in this range.
        return binarySearch(boundPrevious, bound);
    }

    /**
     * A binary-search style recursive algorithm to successively narrow the range of values until
     * the closest is found.
     */
    private int binarySearch(int boundLower, int boundUpper) {
        if (boundLower == boundUpper || boundLower == (boundUpper - 1)) {
            return boundLower;
        }

        // Find the mean betweeo the two
        int mean = (boundUpper + boundLower) / 2;
        double valueMean = calculateValue.calculate(mean);

        if (valueMean > target) {
            return binarySearch(boundLower, mean);
        } else {
            return binarySearch(mean, boundUpper);
        }
    }
}
