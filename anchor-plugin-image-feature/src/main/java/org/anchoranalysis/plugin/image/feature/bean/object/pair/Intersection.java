/* (C)2020 */
package org.anchoranalysis.plugin.image.feature.bean.object.pair;

import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.Positive;
import org.anchoranalysis.feature.cache.ChildCacheName;
import org.anchoranalysis.feature.cache.SessionInput;
import org.anchoranalysis.feature.cache.calculation.FeatureCalculation;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.feature.bean.object.pair.FeatureDeriveFromPair;
import org.anchoranalysis.image.feature.object.input.FeatureInputPairObjects;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.plugin.image.feature.object.calculation.delegate.CalculateInputFromDelegateOption;

/**
 * Finds the intersection of two objects and calculates a feature on it
 *
 * @author Owen Feehan
 */
public class Intersection extends FeatureDeriveFromPair {

    // START BEAN PROPERTIES
    @BeanField @Positive @Getter @Setter private int iterationsDilation = 0;

    @BeanField @Getter @Setter private int iterationsErosion = 0;

    @BeanField @Getter @Setter private boolean do3D = true;

    @BeanField @Getter @Setter private double emptyValue = 255;
    // END BEAN PROPERTIES

    @Override
    public double calc(SessionInput<FeatureInputPairObjects> input) throws FeatureCalcException {

        return CalculateInputFromDelegateOption.calc(
                input,
                createCalculation(input),
                CalculateIntersectionInput::new,
                getItem(),
                cacheIntersectionName(),
                emptyValue);
    }

    /** A unique cache-name for the intersection of how we find a parameterization */
    private ChildCacheName cacheIntersectionName() {
        String id =
                String.format(
                        "intersection_%d_%d_%d",
                        iterationsDilation, iterationsErosion, do3D ? 1 : 0);
        return new ChildCacheName(Intersection.class, id);
    }

    private FeatureCalculation<Optional<ObjectMask>, FeatureInputPairObjects> createCalculation(
            SessionInput<FeatureInputPairObjects> input) {
        return CalculatePairIntersectionCommutative.of(
                input,
                CACHE_NAME_FIRST,
                CACHE_NAME_SECOND,
                iterationsDilation,
                iterationsErosion,
                do3D);
    }
}
