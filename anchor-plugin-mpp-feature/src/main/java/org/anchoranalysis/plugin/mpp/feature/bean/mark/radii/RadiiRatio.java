/* (C)2020 */
package org.anchoranalysis.plugin.mpp.feature.bean.mark.radii;

import org.anchoranalysis.anchor.mpp.feature.bean.mark.FeatureInputMark;
import org.anchoranalysis.anchor.mpp.feature.bean.mark.FeatureMark;
import org.anchoranalysis.anchor.mpp.mark.MarkConic;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.feature.cache.SessionInput;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.extent.ImageResolution;

public class RadiiRatio extends FeatureMark {

    // START BEAN PROPERTIES
    @BeanField private boolean highestTwoRadiiOnly = false;

    @BeanField private boolean suppressRes = false;
    // END BEAN PROPERTIES

    private ImageResolution uniformRes = new ImageResolution();

    @Override
    public double calc(SessionInput<FeatureInputMark> input) throws FeatureCalcException {

        MarkConic markCast = (MarkConic) input.get().getMark();

        ImageResolution sr = suppressRes ? uniformRes : input.get().getResRequired();
        double[] radiiOrdered = markCast.radiiOrderedResolved(sr);

        int len = radiiOrdered.length;
        assert (len >= 2);

        if (len == 2) {
            return radiiOrdered[1] / radiiOrdered[0];
        } else {
            if (highestTwoRadiiOnly) {
                return radiiOrdered[2] / radiiOrdered[1];
            } else {
                return radiiOrdered[2] / radiiOrdered[0];
            }
        }
    }

    public boolean isHighestTwoRadiiOnly() {
        return highestTwoRadiiOnly;
    }

    public void setHighestTwoRadiiOnly(boolean highestTwoRadiiOnly) {
        this.highestTwoRadiiOnly = highestTwoRadiiOnly;
    }

    public boolean isSuppressRes() {
        return suppressRes;
    }

    public void setSuppressRes(boolean suppressRes) {
        this.suppressRes = suppressRes;
    }
}
