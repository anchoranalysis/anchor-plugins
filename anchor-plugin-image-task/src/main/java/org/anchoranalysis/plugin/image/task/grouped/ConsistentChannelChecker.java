/*-
 * #%L
 * anchor-plugin-image-task
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

package org.anchoranalysis.plugin.image.task.grouped;

import org.anchoranalysis.core.index.SetOperationFailedException;
import org.anchoranalysis.image.voxel.datatype.UnsignedShortVoxelType;
import org.anchoranalysis.image.voxel.datatype.VoxelDataType;
import lombok.Getter;

/**
 * Checks that the histograms created from channels all have the same data type, res, max-value etc.
 *
 * @author Owen Feehan
 */
public class ConsistentChannelChecker {

    @Getter private long maxValue = 0; // Unset
    @Getter private VoxelDataType channelType;

    /** Checks that a channel has the same type (max value) as the others */
    public void checkChannelType(VoxelDataType channelType) throws SetOperationFailedException {
        long maxValueChannel = channelType.maxValue();
        if (!isMaxValueSet()) {
            setMaxValue(maxValueChannel);
            this.channelType = channelType;
        } else {
            if (getMaxValue() != maxValueChannel) {
                throw new SetOperationFailedException(
                        "All images must have data-types of the same histogram size");
            } else if (!getChannelType().equals(channelType)) {
                throw new SetOperationFailedException("All images must have the same data type");
            }
        }
    }

    private boolean isMaxValueSet() {
        return maxValue != 0;
    }
    
    private void setMaxValue(long histogramMaxValue) throws SetOperationFailedException {

        if (histogramMaxValue > UnsignedShortVoxelType.MAX_VALUE) {
            throw new SetOperationFailedException(
                    String.format(
                            "Histogram max-value (%d) must be set less than %d",
                            histogramMaxValue, UnsignedShortVoxelType.MAX_VALUE));
        }

        this.maxValue = histogramMaxValue;
    }
}
