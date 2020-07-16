/* (C)2020 */
package org.anchoranalysis.plugin.image.feature.object.calculation.single.morphological;

import lombok.EqualsAndHashCode;
import org.anchoranalysis.feature.cache.calculation.CalculationResolver;
import org.anchoranalysis.feature.cache.calculation.ResolvedCalculation;
import org.anchoranalysis.feature.cache.calculation.ResolvedCalculationMap;
import org.anchoranalysis.image.feature.object.input.FeatureInputSingleObject;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.plugin.image.feature.object.calculation.single.CalculateObjectMask;

@EqualsAndHashCode(callSuper = true)
public class CalculateDilation extends CalculateObjectMask {

    public static ResolvedCalculation<ObjectMask, FeatureInputSingleObject> of(
            CalculationResolver<FeatureInputSingleObject> cache, int iterations, boolean do3D) {
        ResolvedCalculationMap<ObjectMask, FeatureInputSingleObject, Integer> map =
                cache.search(new CalculateDilationMap(do3D));

        return cache.search(new CalculateDilation(iterations, map));
    }

    private CalculateDilation(
            int iterations,
            ResolvedCalculationMap<ObjectMask, FeatureInputSingleObject, Integer> map) {
        super(iterations, map);
    }
}
