/* (C)2020 */
package org.anchoranalysis.plugin.image.feature.bean.stack.object;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.feature.bean.Feature;
import org.anchoranalysis.feature.cache.ChildCacheName;
import org.anchoranalysis.feature.cache.SessionInput;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.feature.bean.stack.FeatureStack;
import org.anchoranalysis.image.feature.object.input.FeatureInputSingleObject;
import org.anchoranalysis.image.feature.stack.FeatureInputStack;

/**
 * Treats a channel as an object-mask, assuming binary values of 0 and 255 and calls an object-mask
 * feature
 *
 * @author Owen Feehan
 */
public class AsObjectMask extends FeatureStack {

    // START BEAN PROPERTIES
    @BeanField private Feature<FeatureInputSingleObject> item;

    @BeanField
    /** The channel that that forms the binary mask */
    private int nrgIndex = 0;
    // END BEAN PROPERTIES

    @Override
    public double calc(SessionInput<FeatureInputStack> input) throws FeatureCalcException {

        return input.forChild()
                .calc(
                        item,
                        new CalculateDeriveObjectInput(nrgIndex),
                        new ChildCacheName(AsObjectMask.class, nrgIndex));
    }

    public Feature<FeatureInputSingleObject> getItem() {
        return item;
    }

    public void setItem(Feature<FeatureInputSingleObject> item) {
        this.item = item;
    }

    public int getNrgIndex() {
        return nrgIndex;
    }

    public void setNrgIndex(int nrgIndex) {
        this.nrgIndex = nrgIndex;
    }
}
