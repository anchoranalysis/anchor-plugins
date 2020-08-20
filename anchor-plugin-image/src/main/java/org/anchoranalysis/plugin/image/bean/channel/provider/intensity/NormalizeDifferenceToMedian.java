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

package org.anchoranalysis.plugin.image.bean.channel.provider.intensity;

import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.image.bean.provider.ChannelProvider;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.convert.ByteConverter;
import org.anchoranalysis.image.histogram.Histogram;
import org.anchoranalysis.image.histogram.HistogramFactory;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.image.voxel.iterator.IterateVoxels;
import org.anchoranalysis.plugin.image.bean.channel.provider.UnaryWithObjectsBase;
import org.anchoranalysis.plugin.image.channel.DimensionsChecker;

/**
 * Corrects a channel in the following way
 * 
 * <p>For each object:
 * <ol>
 * <li>Identify the median value from {@code channelLookup} <i>(Value 1)</i>
 * <li>Calculate the difference of each pixel value in {@code channelLookup} <i>(Value 2)</i>
 * <li>Adjust each pixel value by <i>Value 2</i>.
 * </ol>
 * 
 * @author Owen Feehan
 */
public class NormalizeDifferenceToMedian extends UnaryWithObjectsBase {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private ChannelProvider channelLookup;
    // END BEAN PROPERTIES

    @Override
    protected Channel createFromChannel(Channel channel, ObjectCollection objects)
            throws CreateException {

        Channel lookup = DimensionsChecker.createSameSize(channelLookup, "channelLookup", channel);

        try {
            for (ObjectMask object : objects) {

                Histogram histogram = HistogramFactory.create(lookup.voxels(), Optional.of(object));
                adjustObject(object, channel, lookup, (int) Math.round(histogram.mean()));
            }

            return channel;

        } catch (OperationFailedException e) {
            throw new CreateException("An error occurred calculating the mean", e);
        }
    }

    private void adjustObject(
            ObjectMask object, Channel chnl, Channel chnlLookup, int medianFromObject) {

        IterateVoxels.callEachPointTwo(chnl.voxels().asByte(), chnlLookup.voxels().asByte(), object, (point, buffer, bufferLookup, offset) -> {
            int lookupVal = ByteConverter.unsignedByteToInt(bufferLookup.get(offset));

            int valueExisting = ByteConverter.unsignedByteToInt(buffer.get(offset));
            int valueToAssign = clipValue(valueExisting - medianFromObject + lookupVal);

            buffer.put(offset, (byte) valueToAssign);
        });
    }
    
    private static int clipValue(int value) {
        if (value < 0) {
            return 0;
        } else if (value > 255) {
            return 255;
        } else {
            return value;
        }
    }
}
