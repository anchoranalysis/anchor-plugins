/* (C)2020 */
package org.anchoranalysis.plugin.image.feature.bean.object.single.shared.intersecting;

import lombok.EqualsAndHashCode;
import org.anchoranalysis.feature.cache.calculation.ResolvedCalculation;
import org.anchoranalysis.image.feature.object.input.FeatureInputPairObjects;
import org.anchoranalysis.image.feature.object.input.FeatureInputSingleObject;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.plugin.image.feature.object.calculation.delegate.CalculateInputFromDelegate;

@EqualsAndHashCode(callSuper = true)
public class CalculateIntersecting
        extends CalculateInputFromDelegate<
                FeatureInputPairObjects, FeatureInputSingleObject, ObjectCollection> {

    private int index;

    public CalculateIntersecting(
            ResolvedCalculation<ObjectCollection, FeatureInputSingleObject> intersecting,
            int index) {
        super(intersecting);
        this.index = index;
    }

    @Override
    protected FeatureInputPairObjects deriveFromDelegate(
            FeatureInputSingleObject input, ObjectCollection delegate) {
        return new FeatureInputPairObjects(
                input.getObject(), delegate.get(index), input.getNrgStackOptional());
    }
}
