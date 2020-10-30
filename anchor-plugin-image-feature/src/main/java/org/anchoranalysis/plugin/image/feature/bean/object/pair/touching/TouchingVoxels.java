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
import org.anchoranalysis.feature.calculate.cache.SessionInput;
import org.anchoranalysis.image.feature.bean.object.pair.FeaturePairObjects;
import org.anchoranalysis.image.feature.input.FeatureInputPairObjects;
import org.anchoranalysis.image.voxel.kernel.count.CountKernel;
import org.anchoranalysis.image.voxel.kernel.count.CountKernelNeighborhoodMask;
import org.anchoranalysis.image.voxel.object.ObjectMask;
import org.anchoranalysis.spatial.box.BoundingBox;

/**
 * Base class for features that calculate touching with a dilated bounding box intersection
 *
 * @author Owen Feehan
 */
public abstract class TouchingVoxels extends FeaturePairObjects {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private boolean do3D = true;
    // END BEAN PROPERTIES

    @Override
    public double calculate(SessionInput<FeatureInputPairObjects> input)
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

    protected abstract double calculateWithIntersection(
            ObjectMask object1, ObjectMask object2, BoundingBox boxIntersect)
            throws FeatureCalculationException;

    /**
     * The intersection of the bounding box of one object-mask with the (dilated by 1 bounding-box)
     * of the other
     */
    private Optional<BoundingBox> boxIntersectDilated(SessionInput<FeatureInputPairObjects> input)
            throws FeatureCalculationException {
        return input.calculate(new CalculateIntersectionOfDilatedBoundingBox(do3D));
    }

    protected CountKernel createCountKernelMask(ObjectMask object1, ObjectMask object2Relative) {
        return new CountKernelNeighborhoodMask(
                do3D, object1.binaryValuesByte(), object2Relative, true);
    }
}
