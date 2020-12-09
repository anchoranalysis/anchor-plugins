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

import java.util.function.ToIntFunction;
import java.util.stream.Stream;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.anchoranalysis.core.functional.StreamableCollection;
import org.anchoranalysis.image.core.dimensions.resize.ResizeExtentUtilities;
import org.anchoranalysis.spatial.Extent;
import org.anchoranalysis.spatial.box.BoundingBox;
import org.anchoranalysis.spatial.scale.ScaleFactor;

/**
 * Helpers determine a scaling-factor for objects to fit in a certain-sized scene.
 *
 * @author Owen Feehan
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
class ScaleFactorCalculator {

    /**
     * Calculates a minimal scaling necessary so that each bounding-box can fit inside a certain
     * sized scene
     *
     * <p>In otherwords the largest dimension of any object, must still be able to fit inside the
     * corresponding dimension of the target scene.
     *
     * @param boundingBoxes a stream of bounding-boxes, each of which must fit inside {@code
     *     targetSize}
     * @param targetSize the size in which all bounding-boxes must fit
     * @return a scale-factor that can be applied to the bounding-boxes so that they will always fit
     *     inside {@code targetSize}
     */
    public static ScaleFactor factorSoEachBoundingBoxFitsIn(
            StreamableCollection<BoundingBox> boundingBoxes, Extent targetSize) {
        Extent maxInEachDimension =
                new Extent(
                        extractMaxDimension(boundingBoxes.stream(), Extent::x),
                        extractMaxDimension(boundingBoxes.stream(), Extent::y));
        return ResizeExtentUtilities.relativeScale(maxInEachDimension, targetSize);
    }

    private static int extractMaxDimension(
            Stream<BoundingBox> boundingBoxes, ToIntFunction<Extent> functionDimension) {
        // We add a 1 to make the object be a little bit smaller after scaling and prevent
        //  round up errors after accidentally pushing a bounding-box outside the target-window
        return boundingBoxes.map(BoundingBox::extent).mapToInt(functionDimension).max().getAsInt()
                + 1;
    }
}
