/* (C)2020 */
package org.anchoranalysis.plugin.mpp.feature.bean.mark.radii;

import org.anchoranalysis.anchor.mpp.feature.bean.mark.FeatureInputMark;
import org.anchoranalysis.anchor.mpp.feature.bean.mark.FeatureMark;
import org.anchoranalysis.anchor.mpp.mark.MarkConic;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.feature.cache.SessionInput;
import org.anchoranalysis.feature.calc.FeatureCalcException;

public class OrderedRadius extends FeatureMark {

    // START BEAN PROPERTIES
    @BeanField private int index;
    // END BEAN PROPERTIES

    @Override
    public double calc(SessionInput<FeatureInputMark> input) throws FeatureCalcException {

        if (input.get().getMark() instanceof MarkConic) {

            MarkConic markCast = (MarkConic) input.get().getMark();
            double[] radii = markCast.createRadiiArrayResolved(input.get().getResRequired());

            if (index >= radii.length) {
                throw new FeatureCalcException(
                        String.format(
                                "Feature index %d must be less than radii array length %d",
                                index, radii.length));
            }

            return radii[index];
        }
        return -1;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }
}
