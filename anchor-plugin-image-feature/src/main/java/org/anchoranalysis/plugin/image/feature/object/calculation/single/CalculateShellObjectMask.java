/*-
 * #%L
 * anchor-plugin-image-feature
 * %%
 * Copyright (C) 2010 - 2020 Owen Feehan, ETH Zurich, University of Zurich, Hoffmann-La Roche
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */

package org.anchoranalysis.plugin.image.feature.object.calculation.single;

import java.util.Optional;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import org.anchoranalysis.core.exception.CreateException;
import org.anchoranalysis.feature.calculate.FeatureCalculationException;
import org.anchoranalysis.feature.calculate.cache.part.ResolvedPart;
import org.anchoranalysis.feature.calculate.part.CalculationPart;
import org.anchoranalysis.feature.calculate.part.CalculationPartResolver;
import org.anchoranalysis.image.feature.input.FeatureInputSingleObject;
import org.anchoranalysis.image.voxel.object.ObjectMask;
import org.anchoranalysis.image.voxel.object.morphological.MorphologicalErosion;
import org.anchoranalysis.plugin.image.feature.bean.morphological.MorphologicalIterations;
import org.anchoranalysis.plugin.image.feature.object.calculation.single.morphological.CalculateDilation;
import org.anchoranalysis.plugin.image.feature.object.calculation.single.morphological.CalculateErosion;
import org.anchoranalysis.spatial.box.Extent;

/** Calculates a shell {@link ObjectMask} by performing dilation and erosion operations. */
@RequiredArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class CalculateShellObjectMask
        extends CalculationPart<ObjectMask, FeatureInputSingleObject> {

    /** Resolved part for calculating dilation. */
    private final ResolvedPart<ObjectMask, FeatureInputSingleObject> calculateDilation;

    /** Resolved part for calculating erosion. */
    private final ResolvedPart<ObjectMask, FeatureInputSingleObject> calculateErosion;

    /** Number of iterations for the second erosion operation. */
    private final int iterationsErosionSecond;

    /** Whether to perform 3D operations. */
    private final boolean do3D;

    /** Whether to invert the shell object. */
    private final boolean inverse;

    /**
     * Creates a new {@link CalculateShellObjectMask} instance.
     *
     * @param resolver the {@link CalculationPartResolver} for {@link FeatureInputSingleObject}
     * @param iterations the {@link MorphologicalIterations} to use
     * @param iterationsErosionSecond number of iterations for the second erosion operation
     * @param inverse whether to invert the shell object
     * @return a new {@link CalculationPart} for calculating shell object masks
     */
    public static CalculationPart<ObjectMask, FeatureInputSingleObject> of(
            CalculationPartResolver<FeatureInputSingleObject> resolver,
            MorphologicalIterations iterations,
            int iterationsErosionSecond,
            boolean inverse) {
        ResolvedPart<ObjectMask, FeatureInputSingleObject> partDilation =
                CalculateDilation.of(
                        resolver, iterations.getIterationsDilation(), iterations.isDo3D());
        ResolvedPart<ObjectMask, FeatureInputSingleObject> partErosion =
                CalculateErosion.ofResolved(
                        resolver, iterations.getIterationsErosion(), iterations.isDo3D());

        return new CalculateShellObjectMask(
                partDilation, partErosion, iterationsErosionSecond, iterations.isDo3D(), inverse);
    }

    @Override
    protected ObjectMask execute(FeatureInputSingleObject input)
            throws FeatureCalculationException {

        Extent extent = input.dimensionsRequired().extent();

        ObjectMask shell = createShellObject(input);

        if (inverse) {
            ObjectMask duplicated = input.getObject().duplicate();

            Optional<ObjectMask> omShellIntersected = shell.intersect(duplicated, extent);
            omShellIntersected.ifPresent(
                    shellIntersected -> assignOffTo(duplicated, shellIntersected));

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
                calculateDilation.toString(),
                calculateErosion.toString(),
                do3D ? "true" : "false",
                inverse ? "true" : "false",
                iterationsErosionSecond);
    }

    /**
     * Creates a shell object by applying dilation and erosion operations.
     *
     * @param input the {@link FeatureInputSingleObject} to process
     * @return the created shell {@link ObjectMask}
     * @throws FeatureCalculationException if an error occurs during calculation
     */
    private ObjectMask createShellObject(FeatureInputSingleObject input)
            throws FeatureCalculationException {

        ObjectMask dilated = calculateDilation.getOrCalculate(input).duplicate();
        ObjectMask eroded = calculateErosion.getOrCalculate(input);

        // Maybe apply a second erosion
        dilated = maybeErodeSecondTime(dilated);

        assignOffTo(dilated, eroded);
        return dilated;
    }

    /**
     * Applies a second erosion operation if necessary.
     *
     * @param object the {@link ObjectMask} to erode
     * @return the eroded {@link ObjectMask}
     * @throws FeatureCalculationException if an error occurs during erosion
     */
    private ObjectMask maybeErodeSecondTime(ObjectMask object) throws FeatureCalculationException {
        try {
            if (iterationsErosionSecond > 0) {
                return MorphologicalErosion.erode(object, iterationsErosionSecond, do3D);
            } else {
                return object;
            }
        } catch (CreateException e) {
            throw new FeatureCalculationException(e);
        }
    }

    /**
     * Assigns off pixels to an object-mask based on another object-mask specified in
     * global-coordinates.
     *
     * @param toAssignTo the {@link ObjectMask} to assign off pixels to
     * @param objectMask the {@link ObjectMask} used as a reference for off pixels
     */
    private static void assignOffTo(ObjectMask toAssignTo, ObjectMask objectMask) {
        toAssignTo.assignOff().toObject(objectMask);
    }
}
