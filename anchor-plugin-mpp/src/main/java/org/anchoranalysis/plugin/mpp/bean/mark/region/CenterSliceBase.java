/*-
 * #%L
 * anchor-plugin-mpp
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

package org.anchoranalysis.plugin.mpp.bean.mark.region;

import lombok.EqualsAndHashCode;
import org.anchoranalysis.anchor.mpp.mark.Mark;
import org.anchoranalysis.anchor.mpp.mark.MarkAbstractPosition;
import org.anchoranalysis.anchor.mpp.mark.voxelized.VoxelizedMark;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.extent.BoundingBox;
import org.anchoranalysis.image.extent.ImageDimensions;
import org.anchoranalysis.image.voxel.statistics.VoxelStatistics;

@EqualsAndHashCode(callSuper = true)
public abstract class CenterSliceBase extends IndexedRegionBase {

    @Override
    protected VoxelStatistics createStatisticsFor(
            VoxelizedMark voxelized, Mark mark, ImageDimensions dimensions) throws CreateException {

        BoundingBox box = boundingBoxForRegion(voxelized);

        int zCenter = zCenterFromMark(mark, box);

        return createStatisticsForBBox(voxelized, dimensions, box, zCenter);
    }

    protected abstract VoxelStatistics createStatisticsForBBox(
            VoxelizedMark mark, ImageDimensions dimensions, BoundingBox box, int zCenter);

    private static int zCenterFromMark(Mark markUncasted, BoundingBox box) throws CreateException {
        if (!(markUncasted instanceof MarkAbstractPosition)) {
            throw new CreateException(
                    "Only marks that inherit from MarkAbstractPosition are supported");
        }

        MarkAbstractPosition mark = (MarkAbstractPosition) markUncasted;

        return (int) Math.round(mark.getPos().z()) - box.cornerMin().z();
    }
}
