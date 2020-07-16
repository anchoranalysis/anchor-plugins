/* (C)2020 */
package org.anchoranalysis.plugin.annotation.comparison;

public class RangeSum {

    private double sum = 0.0;
    private double min = Double.MAX_VALUE;
    private double max = Double.MIN_VALUE;
    private int cnt = 0;

    public void add(double val) {
        if (!Double.isNaN(val)) {
            sum += val;
            if (val < min) {
                min = val;
            }
            if (val > max) {
                max = val;
            }
            cnt++;
        }
    }

    public double mean() {
        return sum / cnt;
    }

    public double max() {
        return max;
    }

    public double min() {
        return min;
    }
}
