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

import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.bean.provider.ChannelProvider;
import org.anchoranalysis.image.binary.mask.Mask;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.extent.BoundingBox;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.image.object.factory.CreateFromEntireChnlFactory;

/**
 * Copies the pixels from chnlAssignFrom to chnl (possibly masking)
 *
 * <p>chnl is changed (mutable). chnlAssignFrom is unchanged (immutable)..
 *
 * @author Owen Feehan
 */
public class ChnlProviderAssign extends ChnlProviderOneMask {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private ChannelProvider chnlAssignFrom;
    // END BEAN PROPERTIES

    @Override
    protected Channel createFromMaskedChnl(Channel chnlSrc, Mask binaryImgChnl)
            throws CreateException {

        assign(
                chnlSrc,
                DimChecker.createSameSize(chnlAssignFrom, "chnlAssignFrom", chnlSrc),
                binaryImgChnl);

        return chnlSrc;
    }

    private void assign(Channel chnlSrc, Channel chnlAssignFrom, Mask mask) {

        ObjectMask object = CreateFromEntireChnlFactory.createObject(mask);
        BoundingBox bbox = new BoundingBox(chnlSrc.getDimensions().getExtent());

        chnlAssignFrom
                .getVoxelBox()
                .asByte()
                .copyPixelsToCheckMask(
                        bbox,
                        chnlSrc.getVoxelBox().asByte(),
                        bbox,
                        object.getVoxelBox(),
                        object.getBinaryValuesByte());
    }
}
