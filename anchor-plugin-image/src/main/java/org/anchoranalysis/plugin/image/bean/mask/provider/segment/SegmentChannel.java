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

package org.anchoranalysis.plugin.image.bean.mask.provider.segment;

import java.nio.ByteBuffer;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.OptionalFactory;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.bean.nonbean.error.SegmentationFailedException;
import org.anchoranalysis.image.bean.nonbean.parameters.BinarySegmentationParameters;
import org.anchoranalysis.image.bean.provider.HistogramProvider;
import org.anchoranalysis.image.bean.provider.MaskProvider;
import org.anchoranalysis.image.bean.segment.binary.BinarySegmentation;
import org.anchoranalysis.image.binary.mask.Mask;
import org.anchoranalysis.image.binary.voxel.BinaryVoxels;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.extent.Dimensions;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.plugin.image.bean.mask.provider.FromChannelBase;

/**
 * Applies a {@link BinarySegmentation} algorithm to derive a mask from a channel
 *
 * <p>Optionally, a mask restricts which part of the channel the algorithm is applied to. In this
 * case, the remainder of the channel is unaltered in the output-mask, and no check occurs to ensure
 * it containlys only the valid binary-values for OFF and ON.
 *
 * <p>Optionally a histogram of voxel intensity values is passed to the segmentation algorithm.
 *
 * @author Owen Feehan
 */
public class SegmentChannel extends FromChannelBase {

    // START BEAN PROPERTIES
    /** Segmentation algorithm */
    @BeanField @Getter @Setter private BinarySegmentation segment;

    /** An optional histogram of voxel intensity values which can be used by {@code segment} */
    @BeanField @OptionalBean @Getter @Setter private HistogramProvider histogram;

    /**
     * An optional mask which restricts the algorithm to only parts of the channel where the mask
     * has an ON voxel
     */
    @BeanField @OptionalBean @Getter @Setter private MaskProvider mask;
    // END BEAN PROPERTIES

    @Override
    protected Mask createFromSource(Channel source) throws CreateException {
        return new Mask(segmentChannel(source), source.resolution());
    }

    private BinaryVoxels<ByteBuffer> segmentChannel(Channel channel) throws CreateException {
        Optional<ObjectMask> object = objectFromMask(channel.dimensions());

        BinarySegmentationParameters params = createParams(channel.dimensions());

        try {
            return segment.segment(channel.voxels(), params, object);

        } catch (SegmentationFailedException e) {
            throw new CreateException(e);
        }
    }

    private BinarySegmentationParameters createParams(Dimensions dimensions) throws CreateException {
        return new BinarySegmentationParameters(
                dimensions.resolution(), OptionalFactory.create(histogram));
    }

    private Optional<ObjectMask> objectFromMask(Dimensions dim) throws CreateException {
        Optional<Mask> maskChannel =
                ChannelCreatorHelper.createOptionalCheckSize(mask, "mask", dim);
        return maskChannel.map(channel -> new ObjectMask(channel.binaryVoxels()));
    }
}
