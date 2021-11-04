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

package org.anchoranalysis.plugin.image.task.bean.grouped.histogram;

import lombok.AllArgsConstructor;
import org.anchoranalysis.core.exception.CreateException;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.experiment.JobExecutionException;
import org.anchoranalysis.image.core.channel.Channel;
import org.anchoranalysis.image.core.mask.Mask;
import org.anchoranalysis.image.core.object.HistogramFromObjectsFactory;
import org.anchoranalysis.image.voxel.binary.values.BinaryValuesInt;
import org.anchoranalysis.math.histogram.Histogram;
import org.anchoranalysis.plugin.image.task.grouped.ChannelSource;

/** Extracts a histogram from an image for a given key */
@AllArgsConstructor
class HistogramExtracter {

    private final ChannelSource source;
    private final String keyMask;
    private final int maskValue;

    public Histogram extractFrom(Channel channel) throws JobExecutionException {

        try {
            if (!keyMask.isEmpty()) {
                return HistogramFromObjectsFactory.create(channel, extractMask(keyMask));
            } else {
                return HistogramFromObjectsFactory.create(channel);
            }

        } catch (CreateException e) {
            throw new JobExecutionException("Cannot create histogram", e);
        }
    }

    private Mask extractMask(String stackName) throws JobExecutionException {
        try {
            Channel extracted = source.extractChannel(stackName, false);
            return new Mask(extracted, createMaskBinaryValues());

        } catch (OperationFailedException e) {
            throw new JobExecutionException(e);
        }
    }

    private BinaryValuesInt createMaskBinaryValues() throws JobExecutionException {
        if (maskValue == 255) {
            return new BinaryValuesInt(0, 255);
        } else if (maskValue == 0) {
            return new BinaryValuesInt(255, 0);
        } else {
            throw new JobExecutionException("Only mask-values of 255 or 0 are current supported");
        }
    }
}
