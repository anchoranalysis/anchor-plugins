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
import org.anchoranalysis.anchor.mpp.feature.input.memo.FeatureInputPairMemo;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.feature.cache.SessionInput;
import org.anchoranalysis.feature.calc.FeatureCalculationException;
import org.anchoranalysis.image.extent.BoundingBox;
import org.anchoranalysis.image.extent.ImageDimensions;
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
    public double calc(SessionInput<FeatureInputPairMemo> input)
            throws FeatureCalculationException {

        FeatureInputPairMemo inputSessionless = input.get();

        BoundingBox bbox1 = bbox(inputSessionless, FeatureInputPairMemo::getObj1);
        BoundingBox bbox2 = bbox(inputSessionless, FeatureInputPairMemo::getObj2);

        // Check the bounding boxes intersect in general (including XY)
        if (!bbox1.intersection().existsWith(bbox2)) {
            return 0.0;
        }

        return calcOverlap(bbox1, bbox2, inputSessionless.dimensionsRequired());
    }

    private double calcOverlap(BoundingBox bbox1, BoundingBox bbox2, ImageDimensions dim) {

        Optional<BoundingBox> bboxOverlap = bbox1.intersection().withInside(bbox2, dim.extent());
        if (!bboxOverlap.isPresent()) {
            return 0;
        }

        int minExtentZ = Math.min(zFor(bbox1), zFor(bbox2));

        double overlapZ = (double) zFor(bboxOverlap.get());

        if (normalize) {
            return overlapZ / minExtentZ;
        } else {
            return overlapZ;
        }
    }

    private static int zFor(BoundingBox bbox) {
        return bbox.extent().z();
    }
}
