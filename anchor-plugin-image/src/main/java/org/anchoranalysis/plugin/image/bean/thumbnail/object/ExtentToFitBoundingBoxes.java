/*-
 * #%L
 * anchor-plugin-image
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
package org.anchoranalysis.plugin.image.bean.thumbnail.object;

import java.util.Collection;
import java.util.List;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.anchoranalysis.spatial.box.BoundingBox;
import org.anchoranalysis.spatial.box.Extent;
import org.anchoranalysis.spatial.point.ReadableTuple3i;

/**
 * Derives a minimally-sized extent so that all objects in a collection fit inside
 *
 * @author Owen Feehan
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
class ExtentToFitBoundingBoxes {

    /**
     * Derives an extent that minimally fits all bounding-boxes in the stream
     *
     * <p>Both corners of the bounding-box must fit inside.
     *
     * <p>The fit is as tight as possible.
     *
     * @param boundingBoxes stream of bounding-boxes
     * @return an extent that fits the bounding-boxes
     */
    public static Extent derive(Stream<BoundingBox> boundingBoxes) {

        List<ReadableTuple3i> cornersMax =
                boundingBoxes
                        .map(BoundingBox::calculateCornerMaxInclusive)
                        .collect(Collectors.toList());

        return new Extent(
                maxDimensionValue(cornersMax, ReadableTuple3i::x) + 1,
                maxDimensionValue(cornersMax, ReadableTuple3i::y) + 1,
                maxDimensionValue(cornersMax, ReadableTuple3i::z) + 1);
    }

    private static int maxDimensionValue(
            Collection<ReadableTuple3i> cornersMax,
            ToIntFunction<ReadableTuple3i> valueForDimension) {
        return cornersMax.stream().mapToInt(valueForDimension).max().getAsInt();
    }
}
