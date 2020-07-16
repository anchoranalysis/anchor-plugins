/* (C)2020 */
package org.anchoranalysis.plugin.image.feature.bean.object.single.morphological;

import java.util.Optional;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.feature.cache.calculation.CalculationResolver;
import org.anchoranalysis.feature.cache.calculation.FeatureCalculation;
import org.anchoranalysis.feature.cache.calculation.ResolvedCalculation;
import org.anchoranalysis.feature.cache.calculation.ResolvedCalculationMap;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.extent.ImageDimensions;
import org.anchoranalysis.image.feature.object.input.FeatureInputSingleObject;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.image.object.morph.MorphologicalErosion;
import org.anchoranalysis.plugin.image.feature.object.calculation.single.morphological.CalculateDilationMap;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode(callSuper = false)
class CalculateClosing extends FeatureCalculation<ObjectMask, FeatureInputSingleObject> {

    private final int iterations;
    private final ResolvedCalculationMap<ObjectMask, FeatureInputSingleObject, Integer> mapDilation;
    private final boolean do3D;

    public static ResolvedCalculation<ObjectMask, FeatureInputSingleObject> of(
            CalculationResolver<FeatureInputSingleObject> cache, int iterations, boolean do3D) {
        ResolvedCalculationMap<ObjectMask, FeatureInputSingleObject, Integer> map =
                cache.search(new CalculateDilationMap(do3D));

        return cache.search(new CalculateClosing(iterations, map, do3D));
    }

    @Override
    protected ObjectMask execute(FeatureInputSingleObject params) throws FeatureCalcException {

        try {
            ObjectMask dilated = mapDilation.getOrCalculate(params, iterations);

            return MorphologicalErosion.createErodedObject(
                    dilated,
                    params.getDimensionsOptional().map(ImageDimensions::getExtent),
                    do3D,
                    iterations,
                    false,
                    Optional.empty());

        } catch (CreateException e) {
            throw new FeatureCalcException(e);
        }
    }
}
