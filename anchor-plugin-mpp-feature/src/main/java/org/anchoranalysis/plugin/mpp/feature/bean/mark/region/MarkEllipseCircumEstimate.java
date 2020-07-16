/* (C)2020 */
package org.anchoranalysis.plugin.mpp.feature.bean.mark.region;

import org.anchoranalysis.anchor.mpp.feature.bean.mark.FeatureInputMark;
import org.anchoranalysis.anchor.mpp.mark.Mark;
import org.anchoranalysis.anchor.mpp.mark.conic.MarkEllipse;
import org.anchoranalysis.feature.cache.SessionInput;
import org.anchoranalysis.feature.calc.FeatureCalcException;

// Estimates the circumference of an ellipse based upon finding the area of the shell, and dividing
//  by the ShellRad
public class MarkEllipseCircumEstimate extends FeatureMarkRegion {

    @Override
    public double calc(SessionInput<FeatureInputMark> input) throws FeatureCalcException {

        Mark m = input.get().getMark();

        if (!(m instanceof MarkEllipse)) {
            throw new FeatureCalcException("Only MarkEllipses are supported");
        }

        MarkEllipse mark = (MarkEllipse) m;

        return mark.circumference(getRegionID());
    }
}
