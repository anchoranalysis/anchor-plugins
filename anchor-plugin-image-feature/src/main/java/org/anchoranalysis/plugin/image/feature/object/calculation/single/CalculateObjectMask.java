/* (C)2020 */
package org.anchoranalysis.plugin.image.feature.object.calculation.single;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import org.anchoranalysis.feature.cache.calculation.FeatureCalculation;
import org.anchoranalysis.feature.cache.calculation.ResolvedCalculationMap;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.feature.object.input.FeatureInputSingleObject;
import org.anchoranalysis.image.object.ObjectMask;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(callSuper = false)
public abstract class CalculateObjectMask
        extends FeatureCalculation<ObjectMask, FeatureInputSingleObject> {

    private final int iterations;
    private final ResolvedCalculationMap<ObjectMask, FeatureInputSingleObject, Integer> map;

    /**
     * Copy constructor
     *
     * @param src where to copy from
     */
    protected CalculateObjectMask(CalculateObjectMask src) {
        this.iterations = src.iterations;
        this.map = src.map;
    }

    @Override
    protected ObjectMask execute(FeatureInputSingleObject params) throws FeatureCalcException {

        if (iterations == 0) {
            return params.getObject();
        }

        return map.getOrCalculate(params, iterations);
    }

    @Override
    public String toString() {
        return String.format(
                "%s(iterations=%d,map=%s",
                super.getClass().getSimpleName(), iterations, map.toString());
    }
}
