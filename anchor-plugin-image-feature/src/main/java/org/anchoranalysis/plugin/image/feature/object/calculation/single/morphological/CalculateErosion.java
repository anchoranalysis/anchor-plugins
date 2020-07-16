/* (C)2020 */
package org.anchoranalysis.plugin.image.feature.object.calculation.single.morphological;

import lombok.EqualsAndHashCode;
import org.anchoranalysis.feature.cache.calculation.CalculationResolver;
import org.anchoranalysis.feature.cache.calculation.FeatureCalculation;
import org.anchoranalysis.feature.cache.calculation.ResolvedCalculation;
import org.anchoranalysis.feature.cache.calculation.ResolvedCalculationMap;
import org.anchoranalysis.image.feature.object.input.FeatureInputSingleObject;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.plugin.image.feature.object.calculation.single.CalculateObjectMask;

@EqualsAndHashCode(callSuper = true)
public class CalculateErosion extends CalculateObjectMask {

    public static FeatureCalculation<ObjectMask, FeatureInputSingleObject> of(
            CalculationResolver<FeatureInputSingleObject> cache, int iterations, boolean do3D) {
        ResolvedCalculationMap<ObjectMask, FeatureInputSingleObject, Integer> map =
                cache.search(new CalculateErosionMap(do3D));

        return new CalculateErosion(iterations, map);
    }

    public static ResolvedCalculation<ObjectMask, FeatureInputSingleObject> ofResolved(
            CalculationResolver<FeatureInputSingleObject> cache, int iterations, boolean do3D) {
        return cache.search(of(cache, iterations, do3D));
    }

    private CalculateErosion(
            int iterations,
            ResolvedCalculationMap<ObjectMask, FeatureInputSingleObject, Integer> map) {
        super(iterations, map);
    }
}
