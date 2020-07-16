/* (C)2020 */
package org.anchoranalysis.plugin.image.feature.object.calculation.single;

import java.util.Optional;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.feature.cache.calculation.CalculationResolver;
import org.anchoranalysis.feature.cache.calculation.FeatureCalculation;
import org.anchoranalysis.feature.cache.calculation.ResolvedCalculation;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.extent.ImageDimensions;
import org.anchoranalysis.image.feature.object.input.FeatureInputSingleObject;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.image.object.morph.MorphologicalErosion;
import org.anchoranalysis.plugin.image.feature.object.calculation.single.morphological.CalculateDilation;
import org.anchoranalysis.plugin.image.feature.object.calculation.single.morphological.CalculateErosion;

@RequiredArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class CalculateShellObjectMask
        extends FeatureCalculation<ObjectMask, FeatureInputSingleObject> {

    private final ResolvedCalculation<ObjectMask, FeatureInputSingleObject> ccDilation;
    private final ResolvedCalculation<ObjectMask, FeatureInputSingleObject> ccErosion;
    private final int iterationsErosionSecond;
    private final boolean do3D;
    private final boolean inverse;

    public static FeatureCalculation<ObjectMask, FeatureInputSingleObject> of(
            CalculationResolver<FeatureInputSingleObject> params,
            int iterationsDilation,
            int iterationsErosion,
            int iterationsErosionSecond,
            boolean do3D,
            boolean inverse) {
        ResolvedCalculation<ObjectMask, FeatureInputSingleObject> ccDilation =
                CalculateDilation.of(params, iterationsDilation, do3D);
        ResolvedCalculation<ObjectMask, FeatureInputSingleObject> ccErosion =
                CalculateErosion.ofResolved(params, iterationsErosion, do3D);

        return new CalculateShellObjectMask(
                ccDilation, ccErosion, iterationsErosionSecond, do3D, inverse);
    }

    @Override
    protected ObjectMask execute(FeatureInputSingleObject input) throws FeatureCalcException {

        ImageDimensions dimensions = input.getDimensionsRequired();

        ObjectMask shell =
                createShellObject(input, ccDilation, ccErosion, iterationsErosionSecond, do3D);

        if (inverse) {
            ObjectMask duplicated = input.getObject().duplicate();

            Optional<ObjectMask> omShellIntersected = shell.intersect(duplicated, dimensions);
            omShellIntersected.ifPresent(
                    shellIntersected ->
                            duplicated
                                    .binaryVoxelBox()
                                    .setPixelsCheckMaskOff(
                                            shellIntersected.relMaskTo(
                                                    duplicated.getBoundingBox())));
            return duplicated;

        } else {
            return shell;
        }
    }

    @Override
    public String toString() {
        return String.format(
                "%s ccDilation=%s, ccErosion=%s, do3D=%s, inverse=%s, iterationsErosionSecond=%d",
                super.toString(),
                ccDilation.toString(),
                ccErosion.toString(),
                do3D ? "true" : "false",
                inverse ? "true" : "false",
                iterationsErosionSecond);
    }

    private static ObjectMask createShellObject(
            FeatureInputSingleObject input,
            ResolvedCalculation<ObjectMask, FeatureInputSingleObject> ccDilation,
            ResolvedCalculation<ObjectMask, FeatureInputSingleObject> ccErosion,
            int iterationsErosionSecond,
            boolean do3D)
            throws FeatureCalcException {

        ObjectMask objectDilated = ccDilation.getOrCalculate(input).duplicate();
        ObjectMask objectEroded = ccErosion.getOrCalculate(input);

        // Maybe apply a second erosion
        try {
            objectDilated =
                    iterationsErosionSecond > 0
                            ? MorphologicalErosion.createErodedObject(
                                    objectDilated, null, do3D, iterationsErosionSecond, true, null)
                            : objectDilated;
        } catch (CreateException e) {
            throw new FeatureCalcException(e);
        }

        ObjectMask relMask = objectEroded.relMaskTo(objectDilated.getBoundingBox());

        objectDilated.binaryVoxelBox().setPixelsCheckMaskOff(relMask);

        return objectDilated;
    }
}
