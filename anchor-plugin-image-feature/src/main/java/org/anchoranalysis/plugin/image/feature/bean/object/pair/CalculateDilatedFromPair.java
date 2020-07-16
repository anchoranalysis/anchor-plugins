/* (C)2020 */
package org.anchoranalysis.plugin.image.feature.bean.object.pair;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import org.anchoranalysis.feature.cache.ChildCacheName;
import org.anchoranalysis.feature.cache.calculation.CalcForChild;
import org.anchoranalysis.feature.cache.calculation.CalculationResolver;
import org.anchoranalysis.feature.cache.calculation.FeatureCalculation;
import org.anchoranalysis.feature.cache.calculation.ResolvedCalculation;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.feature.object.calculation.CalculateInputFromPair;
import org.anchoranalysis.image.feature.object.calculation.CalculateInputFromPair.Extract;
import org.anchoranalysis.image.feature.object.input.FeatureInputPairObjects;
import org.anchoranalysis.image.feature.object.input.FeatureInputSingleObject;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.plugin.image.feature.object.calculation.single.morphological.CalculateDilation;

/** Calculates a dilated-object from a pair */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode(callSuper = false)
class CalculateDilatedFromPair extends FeatureCalculation<ObjectMask, FeatureInputPairObjects> {

    // Not included in hash-coding as its assumed to be singular
    @EqualsAndHashCode.Exclude private CalcForChild<FeatureInputPairObjects> resolverForChild;

    private ResolvedCalculation<FeatureInputSingleObject, FeatureInputPairObjects> calcInput;
    private Extract extract;
    private ChildCacheName childCacheName;
    private int iterations;
    private boolean do3D;

    public static FeatureCalculation<ObjectMask, FeatureInputPairObjects> of(
            CalculationResolver<FeatureInputPairObjects> resolver,
            CalcForChild<FeatureInputPairObjects> calcForChild,
            Extract extract,
            ChildCacheName childCacheName,
            int iterations,
            boolean do3D) {
        return new CalculateDilatedFromPair(
                calcForChild,
                resolver.search(new CalculateInputFromPair(extract)),
                extract,
                childCacheName,
                iterations,
                do3D);
    }

    @Override
    protected ObjectMask execute(FeatureInputPairObjects input) throws FeatureCalcException {
        return resolverForChild.calc(
                childCacheName,
                calcInput.getOrCalculate(input),
                resolver -> CalculateDilation.of(resolver, iterations, do3D));
    }
}
