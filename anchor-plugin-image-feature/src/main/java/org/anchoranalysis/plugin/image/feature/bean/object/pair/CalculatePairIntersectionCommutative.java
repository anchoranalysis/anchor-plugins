/* (C)2020 */
package org.anchoranalysis.plugin.image.feature.bean.object.pair;

import java.util.Optional;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import org.anchoranalysis.feature.cache.ChildCacheName;
import org.anchoranalysis.feature.cache.SessionInput;
import org.anchoranalysis.feature.cache.calculation.FeatureCalculation;
import org.anchoranalysis.feature.cache.calculation.ResolvedCalculation;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.feature.object.input.FeatureInputPairObjects;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.image.object.ops.ObjectMaskMerger;

/**
 * Finds the intersection between the dilated versions of two objects (and then performs some
 * erosion)
 * <li>Each object-mask is dilated by (determined by iterationsDilation)
 * <li>The intersection is found
 * <li>Then erosion occurs (determined by iterationsErosion)
 *
 *     <p>This is commutative: {@code f(a,b)==f(b,a)}
 *
 *     <p>
 *
 * @author Owen Feehan
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode(callSuper = false)
class CalculatePairIntersectionCommutative
        extends FeatureCalculation<Optional<ObjectMask>, FeatureInputPairObjects> {

    private final ResolvedCalculation<Optional<ObjectMask>, FeatureInputPairObjects>
            ccFirstToSecond;
    private final ResolvedCalculation<Optional<ObjectMask>, FeatureInputPairObjects>
            ccSecondToFirst;

    public static FeatureCalculation<Optional<ObjectMask>, FeatureInputPairObjects> of(
            SessionInput<FeatureInputPairObjects> cache,
            ChildCacheName childDilation1,
            ChildCacheName childDilation2,
            int iterationsDilation,
            int iterationsErosion,
            boolean do3D) {

        // We use two additional caches, for the calculations involving the single objects, as these
        // can be expensive, and we want
        //  them also cached
        ResolvedCalculation<Optional<ObjectMask>, FeatureInputPairObjects> ccFirstToSecond =
                CalculatePairIntersection.of(
                        cache,
                        childDilation1,
                        childDilation2,
                        iterationsDilation,
                        0,
                        do3D,
                        iterationsErosion);
        ResolvedCalculation<Optional<ObjectMask>, FeatureInputPairObjects> ccSecondToFirst =
                CalculatePairIntersection.of(
                        cache,
                        childDilation1,
                        childDilation2,
                        0,
                        iterationsDilation,
                        do3D,
                        iterationsErosion);
        return new CalculatePairIntersectionCommutative(ccFirstToSecond, ccSecondToFirst);
    }

    @Override
    protected Optional<ObjectMask> execute(FeatureInputPairObjects input)
            throws FeatureCalcException {

        Optional<ObjectMask> omIntersection1 = ccFirstToSecond.getOrCalculate(input);
        Optional<ObjectMask> omIntersection2 = ccSecondToFirst.getOrCalculate(input);

        if (!omIntersection1.isPresent()) {
            return omIntersection2;
        }

        if (!omIntersection2.isPresent()) {
            return omIntersection1;
        }

        return Optional.of(ObjectMaskMerger.merge(omIntersection1.get(), omIntersection2.get()));
    }
}
