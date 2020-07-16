/* (C)2020 */
package org.anchoranalysis.plugin.mpp.feature.bean.mark.radii;

import org.anchoranalysis.anchor.mpp.feature.bean.mark.FeatureInputMark;
import org.anchoranalysis.anchor.mpp.feature.bean.mark.FeatureMark;
import org.anchoranalysis.anchor.mpp.mark.conic.MarkEllipse;
import org.anchoranalysis.feature.cache.SessionInput;
import org.anchoranalysis.feature.calc.FeatureCalcException;

public class MaxRatioRadii extends FeatureMark {

    @Override
    public double calc(SessionInput<FeatureInputMark> input) throws FeatureCalcException {

        if (!(input.get().getMark() instanceof MarkEllipse)) {
            throw new FeatureCalcException("Mark must be of type " + MarkEllipse.class.getName());
        }

        MarkEllipse mark = (MarkEllipse) input.get().getMark();

        double rad1 = mark.getRadii().getX();
        double rad2 = mark.getRadii().getY();

        assert (!Double.isNaN(rad1));
        assert (!Double.isNaN(rad2));

        if (rad1 == 0 || rad2 == 0) {
            return 0.0;
        }

        return Math.max(rad1 / rad2, rad2 / rad1);
    }
}
