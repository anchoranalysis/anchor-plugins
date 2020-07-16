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
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.feature.cache.SessionInput;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.extent.BoundingBox;
import org.anchoranalysis.image.feature.bean.object.pair.FeaturePairObjects;
import org.anchoranalysis.image.feature.object.input.FeatureInputPairObjects;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.image.voxel.kernel.count.CountKernel;
import org.anchoranalysis.image.voxel.kernel.count.CountKernelNeighborhoodMask;

/**
 * Base class for features that calculate touching with a dilated bounding box intersection
 *
 * @author Owen Feehan
 */
public abstract class TouchingVoxels extends FeaturePairObjects {

    // START BEAN PROPERTIES
    @BeanField private boolean do3D = true;
    // END BEAN PROPERTIES

    @Override
    public double calc(SessionInput<FeatureInputPairObjects> input) throws FeatureCalcException {

        FeatureInputPairObjects inputSessionless = input.get();

        Optional<BoundingBox> bboxIntersect = bboxIntersectDilated(input);

        if (!bboxIntersect.isPresent()) {
            // No intersection, so therefore return 0
            return 0;
        }

        return calcWithIntersection(
                inputSessionless.getFirst(), inputSessionless.getSecond(), bboxIntersect.get());
    }

    protected abstract double calcWithIntersection(
            ObjectMask object1, ObjectMask object2, BoundingBox bboxIntersect)
            throws FeatureCalcException;

    /**
     * The intersection of the bounding box of one mask with the (dilated by 1 bounding-box) of the
     * other
     */
    private Optional<BoundingBox> bboxIntersectDilated(SessionInput<FeatureInputPairObjects> input)
            throws FeatureCalcException {
        return input.calc(new CalculateIntersectionOfDilatedBoundingBox(do3D));
    }

    protected CountKernel createCountKernelMask(ObjectMask object1, ObjectMask object2Relative) {
        return new CountKernelNeighborhoodMask(
                do3D, object1.getBinaryValuesByte(), object2Relative, true);
    }

    public boolean isDo3D() {
        return do3D;
    }

    public void setDo3D(boolean do3d) {
        do3D = do3d;
    }
}
