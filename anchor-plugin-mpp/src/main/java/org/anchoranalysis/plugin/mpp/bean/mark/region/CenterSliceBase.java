package org.anchoranalysis.plugin.mpp.bean.mark.region;

/*-
 * #%L
 * anchor-plugin-mpp-feature
 * %%
 * Copyright (C) 2010 - 2020 Owen Feehan
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

import org.anchoranalysis.anchor.mpp.mark.Mark;
import org.anchoranalysis.anchor.mpp.mark.MarkAbstractPosition;
import org.anchoranalysis.anchor.mpp.pxlmark.VoxelizedMark;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.extent.BoundingBox;
import org.anchoranalysis.image.extent.ImageDimensions;
import org.anchoranalysis.image.voxel.statistics.VoxelStatistics;

import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
public abstract class CenterSliceBase extends IndexedRegionBase {

  @Override
  protected VoxelStatistics createStatisticsFor(
      VoxelizedMark pm, Mark mark, ImageDimensions dimensions) throws CreateException {

    BoundingBox bbox = boundingBoxForRegion(pm);

    int zCenter = zCenterFromMark(mark, bbox);

    return createStatisticsForBBox(pm, dimensions, bbox, zCenter);
  }

  protected abstract VoxelStatistics createStatisticsForBBox(
      VoxelizedMark pm, ImageDimensions dimensions, BoundingBox bbox, int zCenter);

  private static int zCenterFromMark(Mark markUncasted, BoundingBox bbox) throws CreateException {
    if (!(markUncasted instanceof MarkAbstractPosition)) {
      throw new CreateException("Only marks that inherit from MarkAbstractPosition are supported");
    }

    MarkAbstractPosition mark = (MarkAbstractPosition) markUncasted;

    return (int) Math.round(mark.getPos().getZ()) - bbox.cornerMin().getZ();
  }
}
