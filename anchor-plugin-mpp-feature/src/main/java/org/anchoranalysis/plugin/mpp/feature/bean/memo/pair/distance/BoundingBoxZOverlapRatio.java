/*-
 * #%L
 * anchor-plugin-mpp-feature
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

package org.anchoranalysis.plugin.mpp.feature.bean.memo.pair.distance;

import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.feature.calculate.FeatureCalculationException;
import org.anchoranalysis.feature.calculate.cache.SessionInput;
import org.anchoranalysis.image.extent.Dimensions;
import org.anchoranalysis.image.extent.box.BoundingBox;
import org.anchoranalysis.mpp.feature.input.memo.FeatureInputPairMemo;
import org.anchoranalysis.plugin.mpp.feature.bean.memo.pair.FeaturePairMemoSingleRegion;

/**
 * Measures the amount of bounding box overlap in the Z dimension
 *
 * <p>Expresses it as a fraction of the minimum Z-extent
 *
 * <p>This is useful for measuring how much two objects overlap in Z
 *
 * <p>It is only calculated if there is overlap of the bounding boxes in XYZ, else 0 is returned
 */
public class BoundingBoxZOverlapRatio extends FeaturePairMemoSingleRegion {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private boolean normalize = true;
    // END BEAN PROPERTIES

    @Override
    public double calculate(SessionInput<FeatureInputPairMemo> input)
            throws FeatureCalculationException {

        FeatureInputPairMemo inputSessionless = input.get();

        BoundingBox box1 = box(inputSessionless, FeatureInputPairMemo::getObject1);
        BoundingBox box2 = box(inputSessionless, FeatureInputPairMemo::getObject2);

        // Check the bounding boxes intersect in general (including XY)
        if (!box1.intersection().existsWith(box2)) {
            return 0.0;
        }

        return overlap(box1, box2, inputSessionless.dimensionsRequired());
    }

    private double overlap(BoundingBox box1, BoundingBox box2, Dimensions dim) {

        Optional<BoundingBox> boxOverlap = box1.intersection().withInside(box2, dim.extent());
        if (!boxOverlap.isPresent()) {
            return 0;
        }

        int minExtentZ = Math.min(zFor(box1), zFor(box2));

        double overlapZ = (double) zFor(boxOverlap.get());

        if (normalize) {
            return overlapZ / minExtentZ;
        } else {
            return overlapZ;
        }
    }

    private static int zFor(BoundingBox box) {
        return box.extent().z();
    }
}
