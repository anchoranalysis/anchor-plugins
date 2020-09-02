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
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import org.anchoranalysis.feature.cache.calculate.FeatureCalculation;
import org.anchoranalysis.feature.calculate.FeatureCalculationException;
import org.anchoranalysis.image.extent.Extent;
import org.anchoranalysis.image.extent.box.BoundingBox;
import org.anchoranalysis.image.feature.object.input.FeatureInputPairObjects;
import org.anchoranalysis.image.object.ObjectMask;

@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
class CalculateIntersectionOfDilatedBoundingBox
        extends FeatureCalculation<Optional<BoundingBox>, FeatureInputPairObjects> {

    private final boolean do3D;

    @Override
    protected Optional<BoundingBox> execute(FeatureInputPairObjects input)
            throws FeatureCalculationException {
        return findIntersectionOfDilatedBoundingBox(
                input.getFirst(), input.getSecond(), input.dimensionsRequired().extent());
    }

    private Optional<BoundingBox> findIntersectionOfDilatedBoundingBox(
            ObjectMask first, ObjectMask second, Extent extent) {

        // Grow each bounding box
        BoundingBox boxFirst = dilatedBoundingBoxFor(first, extent);
        BoundingBox boxSecond = dilatedBoundingBoxFor(second, extent);

        // Find the intersection
        return boxFirst.intersection().withInside(boxSecond, extent);
    }

    private BoundingBox dilatedBoundingBoxFor(ObjectMask object, Extent extent) {
        return object.boundedVoxels().dilate(do3D, Optional.of(extent));
    }
}
