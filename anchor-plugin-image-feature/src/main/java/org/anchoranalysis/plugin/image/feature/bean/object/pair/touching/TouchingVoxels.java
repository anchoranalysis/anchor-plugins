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

package org.anchoranalysis.plugin.image.feature.bean.object.pair.touching;

import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.feature.calculate.FeatureCalculationException;
import org.anchoranalysis.feature.calculate.FeatureCalculationInput;
import org.anchoranalysis.image.feature.bean.object.pair.FeaturePairObjects;
import org.anchoranalysis.image.feature.input.FeatureInputPairObjects;
import org.anchoranalysis.image.voxel.kernel.KernelApplicationParameters;
import org.anchoranalysis.image.voxel.kernel.OutsideKernelPolicy;
import org.anchoranalysis.image.voxel.kernel.count.CountKernel;
import org.anchoranalysis.image.voxel.kernel.count.CountKernelNeighborhoodMask;
import org.anchoranalysis.image.voxel.object.ObjectMask;
import org.anchoranalysis.spatial.box.BoundingBox;

/**
 * Base class for features that calculate touching with a dilated bounding box intersection.
 *
 * @author Owen Feehan
 */
public abstract class TouchingVoxels extends FeaturePairObjects {

    // START BEAN PROPERTIES
    /** Whether to perform calculations in 3D (true) or 2D (false). */
    @BeanField @Getter @Setter private boolean do3D = true;

    // END BEAN PROPERTIES

    @Override
    public double calculate(FeatureCalculationInput<FeatureInputPairObjects> input)
            throws FeatureCalculationException {

        FeatureInputPairObjects inputSessionless = input.get();

        Optional<BoundingBox> boxIntersect = boxIntersectDilated(input);

        if (!boxIntersect.isPresent()) {
            // No intersection, so therefore return 0
            return 0;
        }

        return calculateWithIntersection(
                inputSessionless.getFirst(), inputSessionless.getSecond(), boxIntersect.get());
    }

    /**
     * Calculates the feature value for the intersection of two objects.
     *
     * @param object1 the first {@link ObjectMask}
     * @param object2 the second {@link ObjectMask}
     * @param boxIntersect the {@link BoundingBox} of the intersection
     * @return the calculated feature value
     * @throws FeatureCalculationException if the calculation fails
     */
    protected abstract double calculateWithIntersection(
            ObjectMask object1, ObjectMask object2, BoundingBox boxIntersect)
            throws FeatureCalculationException;

    /**
     * Creates a {@link CountKernel} for the given object mask.
     *
     * @param object2Relative the {@link ObjectMask} to create the kernel for
     * @return the created {@link CountKernel}
     */
    protected CountKernel createCountKernelMask(ObjectMask object2Relative) {
        return new CountKernelNeighborhoodMask(object2Relative);
    }

    /**
     * Creates {@link KernelApplicationParameters} based on the current configuration.
     *
     * @return the created {@link KernelApplicationParameters}
     */
    protected KernelApplicationParameters createParameters() {
        return new KernelApplicationParameters(OutsideKernelPolicy.AS_OFF, do3D);
    }

    /**
     * Calculates the intersection of the bounding box of one object-mask with the (dilated by 1
     * bounding-box) of the other.
     *
     * @param input the {@link FeatureCalculationInput} containing the pair of objects
     * @return an {@link Optional} containing the intersecting {@link BoundingBox}, or empty if
     *     there's no intersection
     * @throws FeatureCalculationException if the calculation fails
     */
    private Optional<BoundingBox> boxIntersectDilated(
            FeatureCalculationInput<FeatureInputPairObjects> input)
            throws FeatureCalculationException {
        return input.calculate(new CalculateIntersectionOfDilatedBoundingBox(do3D));
    }
}
