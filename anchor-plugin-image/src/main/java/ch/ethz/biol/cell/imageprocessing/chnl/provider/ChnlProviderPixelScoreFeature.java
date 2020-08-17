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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.feature.calculate.FeatureCalculationException;
import org.anchoranalysis.image.bean.provider.ChannelProvider;
import org.anchoranalysis.image.bean.provider.ChnlProviderOne;
import org.anchoranalysis.image.bean.provider.HistogramProvider;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.extent.Extent;
import org.anchoranalysis.image.extent.ImageDimensions;
import org.anchoranalysis.image.feature.bean.pixelwise.PixelScore;
import org.anchoranalysis.image.histogram.Histogram;
import org.anchoranalysis.image.voxel.VoxelsWrapper;
import org.anchoranalysis.image.voxel.buffer.VoxelBuffer;

public class ChnlProviderPixelScoreFeature extends ChnlProviderOne {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private PixelScore pixelScore;

    @BeanField @Getter @Setter
    private List<ChannelProvider> listAdditionalChnlProviders = new ArrayList<>();

    @BeanField @OptionalBean @Getter @Setter private HistogramProvider histogramProvider;
    // END BEAN PROPERTIES

    @Override
    public Channel createFromChannel(Channel channel) throws CreateException {

        List<Channel> listAdditional = additionalChnls(channel.dimensions());

        try {
            pixelScore.init(histograms(), Optional.empty());
            scoresToVoxels(channel.voxels(), listAdditional, pixelScore);

        } catch (FeatureCalculationException | InitException e) {
            throw new CreateException(e);
        }

        return channel;
    }

    private List<Histogram> histograms() throws CreateException {
        if (histogramProvider != null) {
            return Arrays.asList(histogramProvider.create());
        } else {
            return new ArrayList<>();
        }
    }

    private static void scoresToVoxels(
            VoxelsWrapper voxels, List<Channel> listAdditional, PixelScore pixelScore)
            throws FeatureCalculationException {

        ByteBuffer[] arrByteBuffer = new ByteBuffer[listAdditional.size()];

        Extent e = voxels.extent();
        for (int z = 0; z < e.z(); z++) {

            VoxelBuffer<?> bb = voxels.slice(z);

            for (int i = 0; i < listAdditional.size(); i++) {
                Channel additional = listAdditional.get(i);
                arrByteBuffer[i] = additional.voxels().asByte().sliceBuffer(z);
            }

            int offset = 0;
            for (int y = 0; y < e.y(); y++) {
                for (int x = 0; x < e.x(); x++) {

                    double result = pixelScore.calculate(createParams(bb, arrByteBuffer, offset));

                    int valOut = (int) Math.round(result);

                    if (valOut < 0) valOut = 0;
                    if (valOut > 255) valOut = 255;

                    bb.putInt(offset, valOut);

                    offset++;
                }
            }
        }
    }

    private static int[] createParams(
            VoxelBuffer<?> bufferPrimary, ByteBuffer[] buffersAdd, int offset) {
        int[] out = new int[1 + buffersAdd.length];

        out[0] = bufferPrimary.getInt(offset);

        int i = 1;
        for (ByteBuffer bbAdd : buffersAdd) {
            out[i++] = bbAdd.getInt(offset);
        }
        return out;
    }

    private List<Channel> additionalChnls(ImageDimensions dimensions) throws CreateException {
        List<Channel> listAdditional = new ArrayList<>();
        for (ChannelProvider cp : listAdditionalChnlProviders) {
            Channel chnlAdditional = cp.create();

            if (!chnlAdditional.dimensions().equals(dimensions)) {
                throw new CreateException(
                        "Dimensions of additional channel are not equal to main channel");
            }

            listAdditional.add(chnlAdditional);
        }
        return listAdditional;
    }
}
