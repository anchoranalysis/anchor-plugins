/* (C)2020 */
package org.anchoranalysis.plugin.image.feature.bean.object.single.intensity;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.feature.cache.SessionInput;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.feature.object.input.FeatureInputSingleObject;

public class IntensityMeanMaxSlice extends FeatureNrgChnl {

    // START BEAN PROPERTIES
    @BeanField private boolean excludeZero = false;

    @BeanField private int emptyValue = 0;
    // END BEAN PROPERTIES

    @Override
    protected double calcForChnl(SessionInput<FeatureInputSingleObject> input, Channel chnl)
            throws FeatureCalcException {

        ValueAndIndex vai =
                StatsHelper.calcMaxSliceMean(chnl, input.get().getObject(), excludeZero);

        if (vai.getIndex() == -1) {
            return emptyValue;
        }

        return vai.getValue();
    }

    public boolean isExcludeZero() {
        return excludeZero;
    }

    public void setExcludeZero(boolean excludeZero) {
        this.excludeZero = excludeZero;
    }

    public int getEmptyValue() {
        return emptyValue;
    }

    public void setEmptyValue(int emptyValue) {
        this.emptyValue = emptyValue;
    }
}
