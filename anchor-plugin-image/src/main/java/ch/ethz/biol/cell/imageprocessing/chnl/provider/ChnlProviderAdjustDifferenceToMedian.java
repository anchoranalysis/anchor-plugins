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

package ch.ethz.biol.cell.imageprocessing.chnl.provider;

import java.nio.ByteBuffer;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.geometry.ReadableTuple3i;
import org.anchoranalysis.image.bean.provider.ChannelProvider;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.convert.ByteConverter;
import org.anchoranalysis.image.histogram.Histogram;
import org.anchoranalysis.image.histogram.HistogramFactory;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.image.voxel.Voxels;

// Corrects a channel in the following way
//  For each object:
//		1. Identify the median value from channelLookup
//		2. Calculate the difference of each pixel value in channelLookup to Value 1.
//		3. Adjust each pixel value by Value 2.
public class ChnlProviderAdjustDifferenceToMedian extends ChnlProviderOneObjectsSource {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private ChannelProvider chnlLookup;
    // END BEAN PROPERTIES

    @Override
    protected Channel createFromChnl(Channel chnl, ObjectCollection objectsSource)
            throws CreateException {

        Channel lookup = DimChecker.createSameSize(chnlLookup, "chnlLookup", chnl);

        try {
            for (ObjectMask object : objectsSource) {

                Histogram histogram =
                        HistogramFactory.create(lookup.voxels(), Optional.of(object));
                adjustObject(object, chnl, lookup, (int) Math.round(histogram.mean()));
            }

            return chnl;

        } catch (OperationFailedException e) {
            throw new CreateException("An error occurred calculating the mean", e);
        }
    }

    private void adjustObject(
            ObjectMask object, Channel chnl, Channel chnlLookup, int medianFromObject) {

        ReadableTuple3i cornerMin = object.boundingBox().cornerMin();
        ReadableTuple3i cornerMax = object.boundingBox().calcCornerMax();

        Voxels<ByteBuffer> voxels = chnl.voxels().asByte();
        Voxels<ByteBuffer> voxelsLookup = chnlLookup.voxels().asByte();

        for (int z = cornerMin.z(); z <= cornerMax.z(); z++) {

            ByteBuffer bbChnl = voxels.slice(z).buffer();
            ByteBuffer bbChnlLookup = voxelsLookup.slice(z).buffer();
            ByteBuffer bbMask =
                    object.voxels().slice(z - cornerMin.z()).buffer();

            int objectMaskOffset = 0;
            for (int y = cornerMin.y(); y <= cornerMax.y(); y++) {
                for (int x = cornerMin.x(); x <= cornerMax.x(); x++) {

                    if (bbMask.get(objectMaskOffset++) == object.binaryValuesByte().getOnByte()) {

                        int offset = voxels.extent().offset(x, y);

                        int lookupVal = ByteConverter.unsignedByteToInt(bbChnlLookup.get(offset));
                        int adj = (medianFromObject - lookupVal);

                        int crntVal = ByteConverter.unsignedByteToInt(bbChnl.get(offset));
                        int valNew = crntVal - adj;

                        if (valNew < 0) valNew = 0;
                        if (valNew > 255) valNew = 255;

                        bbChnl.put(offset, (byte) valNew);
                    }
                }
            }
        }
    }
}
