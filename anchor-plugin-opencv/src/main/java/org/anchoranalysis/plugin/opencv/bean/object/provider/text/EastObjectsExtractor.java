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
/* (C)2020 */
package org.anchoranalysis.plugin.opencv.bean.object.provider.text;

import java.nio.file.Path;
import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.anchoranalysis.anchor.mpp.mark.GlobalRegionIdentifiers;
import org.anchoranalysis.anchor.mpp.mark.Mark;
import org.anchoranalysis.anchor.mpp.regionmap.RegionMapSingleton;
import org.anchoranalysis.core.functional.FunctionalList;
import org.anchoranalysis.image.binary.values.BinaryValuesByte;
import org.anchoranalysis.image.extent.Extent;
import org.anchoranalysis.image.extent.ImageDimensions;
import org.anchoranalysis.image.extent.ImageResolution;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.image.object.properties.ObjectWithProperties;
import org.anchoranalysis.plugin.opencv.nonmaxima.WithConfidence;
import org.opencv.core.Mat;

/**
 * Extracts object-masks representing text regions from an image
 *
 * <p>Each object-mask represented rotated-bounding box and is associated with a confidence score
 *
 * @author Owen Feehan
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
class EastObjectsExtractor {

    public static List<WithConfidence<ObjectMask>> apply(
            Mat image, ImageResolution res, double minConfidence, Path pathToEastModel) {
        List<WithConfidence<Mark>> listMarks =
                EastMarkExtractor.extractBoundingBoxes(image, minConfidence, pathToEastModel);

        // Convert marks to object-masks
        return convertMarksToObject(listMarks, dimensionsForMatrix(image, res));
    }

    private static List<WithConfidence<ObjectMask>> convertMarksToObject(
            List<WithConfidence<Mark>> listMarks, ImageDimensions dim) {
        return FunctionalList.mapToList(listMarks, wc -> convertToObject(wc, dim));
    }

    private static ImageDimensions dimensionsForMatrix(Mat matrix, ImageResolution res) {

        int width = (int) matrix.size().width;
        int height = (int) matrix.size().height;

        return new ImageDimensions(new Extent(width, height, 1), res);
    }

    private static WithConfidence<ObjectMask> convertToObject(
            WithConfidence<Mark> mark, ImageDimensions dimensions) {

        ObjectWithProperties om =
                mark.getObject()
                        .calcMask(
                                dimensions,
                                RegionMapSingleton.instance()
                                        .membershipWithFlagsForIndex(
                                                GlobalRegionIdentifiers.SUBMARK_INSIDE),
                                BinaryValuesByte.getDefault());
        return new WithConfidence<>(om.getMask(), mark.getConfidence());
    }
}
