/* (C)2020 */
package org.anchoranalysis.plugin.opencv.bean.object.provider.text;

import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.image.extent.Extent;
import org.anchoranalysis.image.scale.ScaleFactor;
import org.anchoranalysis.image.scale.ScaleFactorUtilities;

/**
 * Finds largest multiple of an Extent without being larger than another extent
 *
 * @author Owen Feehan
 */
class FindLargestMultipleWithin {

    private FindLargestMultipleWithin() {
        // NOTHING TO DO
    }

    /**
     * Scales an extent as much as possible in BOTH dimensions without growing larger than another
     * extent
     *
     * <p>Only integral scale-factors are considered e.g. twice, thrice, 4 times etc.
     *
     * <p>The X dimension and Y dimension are treated in unison i.e. both are scaled together
     *
     * @param small the extent to scale
     * @param stayWithin a maximum size not to scale beyond
     * @return the scaled-extent to use
     * @throws OperationFailedException
     */
    public static Extent apply(Extent small, Extent stayWithin, int maxScaleFactor)
            throws OperationFailedException {

        if (small.getX() > stayWithin.getX()) {
            throw new OperationFailedException(
                    "The extent of small in the X direction is already larger than stayWithin. This is not allowed");
        }

        if (small.getY() > stayWithin.getY()) {
            throw new OperationFailedException(
                    "The extent of small in the Y direction is already larger than stayWithin. This is not allowed");
        }

        // Non-integral scale factors
        ScaleFactor sf = ScaleFactorUtilities.calcRelativeScale(small, stayWithin);

        int minFactor = minScaleFactorUnder(sf, maxScaleFactor);

        // The integral floor of each
        ScaleFactorInt sfInt = new ScaleFactorInt(minFactor, minFactor);

        return sfInt.scale(small);
    }

    /**
     * The minimum scale factor from X and Y resolution, clipped at the a maximum of maxScaleFactor
     */
    private static int minScaleFactorUnder(ScaleFactor sf, int maxScaleFactor) {
        int min = minScaleFactor(sf);
        return Math.min(min, maxScaleFactor);
    }

    private static int minScaleFactor(ScaleFactor sf) {
        return (int) Math.floor(Math.min(sf.getX(), sf.getY()));
    }
}
