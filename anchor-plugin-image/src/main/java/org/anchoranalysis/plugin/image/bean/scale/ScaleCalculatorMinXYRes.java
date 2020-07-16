/* (C)2020 */
package org.anchoranalysis.plugin.image.bean.scale;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.image.bean.scale.ScaleCalculator;
import org.anchoranalysis.image.extent.ImageDimensions;
import org.anchoranalysis.image.scale.ScaleFactor;

public class ScaleCalculatorMinXYRes extends ScaleCalculator {

    // START BEAN PROPERTIES
    @BeanField private double minXYRes = 10e-9;
    // STOP BEAN PROPERTIES

    @Override
    public ScaleFactor calc(ImageDimensions srcDim) throws OperationFailedException {

        // If there is no resolution information we cannot scale
        if (srcDim.getRes().getX() == 0 || srcDim.getRes().getY() == 0) {
            throw new OperationFailedException(
                    "Channel has zero x or y resolution. Cannot scale to min res.");
        }

        int x = calcRatio(srcDim.getRes().getX(), minXYRes);
        int y = calcRatio(srcDim.getRes().getY(), minXYRes);

        if (x < 0) {
            throw new OperationFailedException(
                    String.format(
                            "Insufficient resolution (%E). %E is required",
                            srcDim.getRes().getX(), minXYRes));
        }

        if (y < 0) {
            throw new OperationFailedException(
                    String.format(
                            "Insufficient resolution (%E). %E is required",
                            srcDim.getRes().getY(), minXYRes));
        }

        double xScaleDownRatio = twoToMinusPower(x);
        double yScaleDownRatio = twoToMinusPower(y);

        getLogger()
                .messageLogger()
                .logFormatted(
                        "Downscaling by factor %d,%d (mult by %f,%f)",
                        x, y, xScaleDownRatio, yScaleDownRatio);

        return new ScaleFactor(xScaleDownRatio, yScaleDownRatio);
    }

    // Returns x i.e. how much to downside where the factor is 2^x
    private static int calcRatio(double cnrtRes, double minRes) {

        double ratio = minRes / cnrtRes;

        double ratioLog2 = Math.log10(ratio) / Math.log10(2);

        if (ratioLog2 > 0) {
            return (int) Math.floor(ratioLog2);
        } else {
            return (int) Math.ceil(ratioLog2);
        }
    }

    private static double twoToMinusPower(int power) {
        return Math.pow(2.0, -1.0 * power);
    }

    public double getMinXYRes() {
        return minXYRes;
    }

    public void setMinXYRes(double minXYRes) {
        this.minXYRes = minXYRes;
    }
}
