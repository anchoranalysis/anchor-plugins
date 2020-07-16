/* (C)2020 */
package org.anchoranalysis.plugin.image.feature.bean.object.single.morphological;

import java.util.Optional;
import lombok.EqualsAndHashCode;
import org.anchoranalysis.feature.cache.calculation.ResolvedCalculation;
import org.anchoranalysis.image.feature.object.input.FeatureInputSingleObject;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.plugin.image.feature.object.calculation.delegate.CalculateInputFromDelegateOption;

@EqualsAndHashCode(callSuper = true)
class CalculateObjForDerived
        extends CalculateInputFromDelegateOption<
                FeatureInputSingleObject, FeatureInputSingleObject, ObjectMask> {
    public CalculateObjForDerived(
            ResolvedCalculation<ObjectMask, FeatureInputSingleObject> ccDerived) {
        super(ccDerived);
    }

    @Override
    protected Optional<FeatureInputSingleObject> deriveFromDelegate(
            FeatureInputSingleObject input, ObjectMask delegate) {

        if (delegate == null || !delegate.hasPixelsGreaterThan(0)) {
            return Optional.empty();
        }

        return Optional.of(new FeatureInputSingleObject(delegate, input.getNrgStackOptional()));
    }
}
