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

package org.anchoranalysis.plugin.mpp.bean.region;

import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.anchor.mpp.bean.mark.MarkRegion;
import org.anchoranalysis.anchor.mpp.mark.voxelized.memo.VoxelizedMarkMemo;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.shared.relation.threshold.RelationToThreshold;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.extent.Dimensions;
import org.anchoranalysis.image.voxel.statistics.VoxelStatistics;

public class Thresholded extends MarkRegion {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private MarkRegion region;

    @BeanField @Getter @Setter private RelationToThreshold threshold;
    // END BEAN PROPERTIES

    @Override
    public VoxelStatistics createStatisticsFor(VoxelizedMarkMemo memo, Dimensions dimensions)
            throws CreateException {
        return region.createStatisticsFor(memo, dimensions).threshold(threshold);
    }

    @Override
    public String uniqueName() {
        return String.format(
                "%s_%s_%s",
                Thresholded.class.getCanonicalName(), region.uniqueName(), threshold.uniqueName());
    }
}