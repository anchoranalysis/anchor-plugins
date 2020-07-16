/* (C)2020 */
package org.anchoranalysis.plugin.image.feature.bean.pixelscore;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.shared.relation.GreaterThanEqualToBean;
import org.anchoranalysis.bean.shared.relation.LessThanBean;
import org.anchoranalysis.bean.shared.relation.threshold.RelationToConstant;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.image.histogram.Histogram;

// Same as PixelScoreDifference but calculates the width as the std deviation of the histogram
//  associated with the init params, and the level is calculated from the histogram
public class PixelScoreDifferenceCalculateLevelStdDev extends PixelScoreCalculateLevelBase {

    // START BEAN PROPERTIES
    @BeanField private int minDifference = 0;

    @BeanField private double widthFactor = 1.0;
    // END BEAN PROPERTIES

    private double widthLessThan;
    private double widthGreaterThan;

    @Override
    protected void beforeCalcSetup(Histogram hist, int level) throws OperationFailedException {

        Histogram lessThan = hist.threshold(new RelationToConstant(new LessThanBean(), level));
        Histogram greaterThan =
                hist.threshold(new RelationToConstant(new GreaterThanEqualToBean(), level));

        this.widthLessThan = lessThan.stdDev() * widthFactor;
        this.widthGreaterThan = greaterThan.stdDev() * widthFactor;
    }

    @Override
    protected double calcForPixel(int pxlValue, int level) {
        return PixelScoreDifference.calcDiffFromValue(
                pxlValue, level, widthGreaterThan, widthLessThan, minDifference);
    }

    public int getMinDifference() {
        return minDifference;
    }

    public void setMinDifference(int minDifference) {
        this.minDifference = minDifference;
    }

    public double getWidthFactor() {
        return widthFactor;
    }

    public void setWidthFactor(double widthFactor) {
        this.widthFactor = widthFactor;
    }
}
