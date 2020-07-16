/* (C)2020 */
package org.anchoranalysis.plugin.image.feature.bean.stack.object;

import lombok.EqualsAndHashCode;
import org.anchoranalysis.feature.cache.calculation.ResolvedCalculation;
import org.anchoranalysis.image.feature.object.input.FeatureInputSingleObject;
import org.anchoranalysis.image.feature.stack.FeatureInputStack;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.plugin.image.feature.object.calculation.delegate.CalculateInputFromDelegate;

@EqualsAndHashCode(callSuper = true)
class CalculateDeriveObjFromCollection
        extends CalculateInputFromDelegate<
                FeatureInputSingleObject, FeatureInputStack, ObjectCollection> {

    private final int index;

    public CalculateDeriveObjFromCollection(
            ResolvedCalculation<ObjectCollection, FeatureInputStack> ccDelegate, int index) {
        super(ccDelegate);
        this.index = index;
    }

    @Override
    protected FeatureInputSingleObject deriveFromDelegate(
            FeatureInputStack input, ObjectCollection delegate) {
        return new FeatureInputSingleObject(delegate.get(index), input.getNrgStackOptional());
    }
}
