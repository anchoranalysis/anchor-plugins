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

package org.anchoranalysis.plugin.image.bean.object.provider.segment;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.anchoranalysis.core.exception.CreateException;
import org.anchoranalysis.image.core.dimensions.Dimensions;
import org.anchoranalysis.image.voxel.object.ObjectCollection;
import org.anchoranalysis.image.voxel.object.ObjectMask;
import org.anchoranalysis.spatial.box.BoundingBox;
import org.anchoranalysis.spatial.point.ReadableTuple3i;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
class SeedsFactory {

    public static ObjectCollection createSeedsWithoutMask(ObjectCollection seeds) {
        // We create a collection of seeds localized appropriately
        return seeds.stream().map(SeedsFactory::createSeed);
    }

    public static ObjectCollection createSeedsWithMask(
            ObjectCollection seeds,
            ObjectMask containingMask,
            ReadableTuple3i subtractFromCornerMin,
            Dimensions dim)
            throws CreateException {
        // We create a collection of seeds localized appropriately
        return seeds.stream()
                .map(
                        object ->
                                createSeedWithinMask(
                                        object,
                                        containingMask.boundingBox(),
                                        subtractFromCornerMin,
                                        dim));
    }

    private static ObjectMask createSeed(ObjectMask object) {
        return object.duplicate();
    }

    private static ObjectMask createSeedWithinMask(
            ObjectMask object,
            BoundingBox containingBBox,
            ReadableTuple3i subtractFromCornerMin,
            Dimensions dim)
            throws CreateException {

        ObjectMask seed = object.shiftBackBy(subtractFromCornerMin);

        // If a seed object is partially located outside an object, the above line might fail, so we
        // should test
        return ensureInsideContainer(seed, containingBBox, dim);
    }

    private static ObjectMask ensureInsideContainer(
            ObjectMask seed, BoundingBox containingBBox, Dimensions dimensions)
            throws CreateException {
        if (!containingBBox.contains().box(seed.boundingBox())) {
            // We only take the part of the seed object that intersects with our box
            BoundingBox boxIntersect =
                    containingBBox
                            .intersection()
                            .withInside(seed.boundingBox(), dimensions.extent())
                            .orElseThrow(
                                    () ->
                                            new CreateException(
                                                    "No bounding box intersection exists between seed and containing bounding-box"));
            return seed.region(boxIntersect, false);
        } else {
            return seed;
        }
    }
}
