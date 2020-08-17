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

package ch.ethz.biol.cell.imageprocessing.binaryimgchnl.provider;

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
import org.anchoranalysis.image.extent.ImageDimensions;
import org.anchoranalysis.image.object.ObjectMask;

public class BinaryChnlProviderSgmn extends BinaryChnlProviderChnlSource {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private BinarySegmentation sgmn;

    @BeanField @OptionalBean @Getter @Setter private HistogramProvider histogramProvider;

    @BeanField @OptionalBean @Getter @Setter private MaskProvider mask;
    // END BEAN PROPERTIES

    @Override
    protected Mask createFromSource(Channel chnlSource) throws CreateException {
        return new Mask(sgmnResult(chnlSource), chnlSource.dimensions().resolution());
    }

    private BinaryVoxels<ByteBuffer> sgmnResult(Channel chnl) throws CreateException {
        Optional<ObjectMask> omMask = objectFromMask(chnl.dimensions());

        BinarySegmentationParameters params = createParams(chnl.dimensions());

        try {
            return sgmn.segment(chnl.voxels(), params, omMask);

        } catch (SegmentationFailedException e) {
            throw new CreateException(e);
        }
    }

    private BinarySegmentationParameters createParams(ImageDimensions dim) throws CreateException {
        return new BinarySegmentationParameters(
                dim.resolution(), OptionalFactory.create(histogramProvider));
    }

    private Optional<ObjectMask> objectFromMask(ImageDimensions dim) throws CreateException {
        Optional<Mask> maskChannel =
                ChnlProviderNullableCreator.createOptionalCheckSize(mask, "mask", dim);
        return maskChannel.map(chnl -> new ObjectMask(chnl.binaryVoxels()));
    }
}
