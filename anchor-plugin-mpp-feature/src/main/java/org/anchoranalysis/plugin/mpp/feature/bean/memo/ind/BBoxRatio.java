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

package org.anchoranalysis.plugin.mpp.feature.bean.memo.ind;

import java.util.Arrays;
import java.util.Optional;
import org.anchoranalysis.feature.calculate.FeatureCalculationException;
import org.anchoranalysis.feature.calculate.cache.SessionInput;
import org.anchoranalysis.image.core.dimensions.Dimensions;
import org.anchoranalysis.image.core.dimensions.Resolution;
import org.anchoranalysis.mpp.feature.bean.energy.element.FeatureSingleMemo;
import org.anchoranalysis.mpp.feature.input.FeatureInputSingleMemo;
import org.anchoranalysis.mpp.mark.GlobalRegionIdentifiers;
import org.anchoranalysis.mpp.mark.conic.ConicBase;
import org.anchoranalysis.spatial.box.BoundingBox;
import org.anchoranalysis.spatial.box.Extent;

public class BBoxRatio extends FeatureSingleMemo {

    @Override
    public double calculate(SessionInput<FeatureInputSingleMemo> input)
            throws FeatureCalculationException {

        ConicBase markCast = (ConicBase) input.get().getPxlPartMemo().getMark();

        int[] extent = markExtent(markCast, input.get().dimensionsRequired());
        return calculateRatio(extent);
    }

    /**
     * The extent of the mark in each dimension, with the z-dimension adjusted for image-resolution
     */
    private static int[] markExtent(ConicBase markCast, Dimensions dimensions) {

        BoundingBox bb = markCast.box(dimensions, GlobalRegionIdentifiers.SUBMARK_INSIDE);
        int[] extent = extractAscendingSizes(bb.extent());

        // Let's change the z-dimension to include the relative-resolution
        extent[2] = zExtent(bb.extent().z(), dimensions.resolution());

        return extent;
    }

    /** Extracts the sizes of the dimensions in {@code extent} in ascencing order of magnitude. */
    private static int[] extractAscendingSizes(Extent extent) {
        int[] extents = extent.toArray();
        Arrays.sort(extents);
        return extents;
    }

    private static int zExtent(int zVoxelExtent, Optional<Resolution> resolution) {
        if (resolution.isPresent()) {
            return (int) (zVoxelExtent * resolution.get().zRelative());
        } else {
            return zVoxelExtent;
        }
    }

    private static double calculateRatio(int[] extent) {

        int len = extent.length;
        assert (len >= 2);

        if (len == 2) {
            return ((double) extent[1]) / extent[0];
        } else {
            return ((double) extent[2]) / extent[0];
        }
    }
}
