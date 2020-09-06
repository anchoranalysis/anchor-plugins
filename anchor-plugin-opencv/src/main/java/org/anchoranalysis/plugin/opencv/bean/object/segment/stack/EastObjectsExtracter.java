/*-
 * #%L
 * anchor-plugin-opencv
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

package org.anchoranalysis.plugin.opencv.bean.object.segment.stack;

import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.anchoranalysis.core.concurrency.ConcurrentModelPool;
import org.anchoranalysis.core.functional.FunctionalList;
import org.anchoranalysis.image.binary.values.BinaryValuesByte;
import org.anchoranalysis.image.extent.Dimensions;
import org.anchoranalysis.image.extent.Extent;
import org.anchoranalysis.image.extent.Resolution;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.image.object.properties.ObjectWithProperties;
import org.anchoranalysis.mpp.bean.regionmap.RegionMapSingleton;
import org.anchoranalysis.mpp.mark.GlobalRegionIdentifiers;
import org.anchoranalysis.mpp.mark.Mark;
import org.anchoranalysis.plugin.image.bean.object.segment.stack.SegmentedObjects;
import org.anchoranalysis.plugin.image.bean.object.segment.stack.WithConfidence;
import org.opencv.core.Mat;
import org.opencv.dnn.Net;

/**
 * Extracts object-masks representing text regions from an image
 *
 * <p>Each object-mask represented rotated-bounding box and is associated with a confidence score
 *
 * @author Owen Feehan
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
class EastObjectsExtracter {

    public static SegmentedObjects apply(
            ConcurrentModelPool<Net> modelPool,
            Mat image,
            Resolution resolution,
            double minConfidence)
            throws Throwable {
        List<WithConfidence<Mark>> listMarks =
                EastMarkExtracter.extractBoundingBoxes(modelPool, image, minConfidence);

        // Convert marks to object-masks
        return new SegmentedObjects(
            convertMarksToObject(listMarks, dimensionsForMatrix(image, resolution))
        );
    }

    private static List<WithConfidence<ObjectMask>> convertMarksToObject(
            List<WithConfidence<Mark>> listMarks, Dimensions dim) {
        return FunctionalList.mapToList(
                listMarks, withConfidence -> convertToObject(withConfidence, dim));
    }

    private static Dimensions dimensionsForMatrix(Mat matrix, Resolution resolution) {

        int width = (int) matrix.size().width;
        int height = (int) matrix.size().height;

        return new Dimensions(new Extent(width, height), resolution);
    }

    private static WithConfidence<ObjectMask> convertToObject(
            WithConfidence<Mark> mark, Dimensions dimensions) {

        ObjectWithProperties object =
                mark.getObject()
                        .deriveObject(
                                dimensions,
                                RegionMapSingleton.instance()
                                        .membershipWithFlagsForIndex(
                                                GlobalRegionIdentifiers.SUBMARK_INSIDE),
                                BinaryValuesByte.getDefault());
        return new WithConfidence<>(object.withoutProperties(), mark.getConfidence());
    }
}
