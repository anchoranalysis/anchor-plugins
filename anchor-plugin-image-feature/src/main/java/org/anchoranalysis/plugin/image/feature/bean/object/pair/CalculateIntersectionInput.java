/* (C)2020 */
package org.anchoranalysis.plugin.image.feature.bean.object.pair;

import java.util.Optional;
import lombok.EqualsAndHashCode;
import org.anchoranalysis.feature.cache.calculation.ResolvedCalculation;
import org.anchoranalysis.image.feature.object.input.FeatureInputPairObjects;
import org.anchoranalysis.image.feature.object.input.FeatureInputSingleObject;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.plugin.image.feature.object.calculation.delegate.CalculateInputFromDelegateOption;

@EqualsAndHashCode(callSuper = true)
class CalculateIntersectionInput
        extends CalculateInputFromDelegateOption<
                FeatureInputSingleObject, FeatureInputPairObjects, Optional<ObjectMask>> {
    public CalculateIntersectionInput(
            ResolvedCalculation<Optional<ObjectMask>, FeatureInputPairObjects> ccIntersection) {
        super(ccIntersection);
    }

    @Override
    protected Optional<FeatureInputSingleObject> deriveFromDelegate(
            FeatureInputPairObjects input, Optional<ObjectMask> delegate) {
        if (!delegate.isPresent()) {
            return Optional.empty();
        }

        assert (delegate.get().hasPixelsGreaterThan(0));
        assert (delegate.get() != null);

        return Optional.of(
                new FeatureInputSingleObject(delegate.get(), input.getNrgStackOptional()));
    }
}
