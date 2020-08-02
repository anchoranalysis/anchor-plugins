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
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.bean.annotation.Positive;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.bean.provider.BinaryChnlProviderOne;
import org.anchoranalysis.image.bean.provider.ChannelProvider;
import org.anchoranalysis.image.binary.mask.Mask;
import org.anchoranalysis.image.voxel.box.VoxelBox;

/** Base class for performing morphological operations */
public abstract class BinaryChnlProviderMorphOp extends BinaryChnlProviderOne {

    // START PROPERTIES
    @BeanField @OptionalBean @Getter @Setter private ChannelProvider backgroundChnlProvider;

    @BeanField @Positive @Getter @Setter private int iterations = 1;

    @BeanField @Getter @Setter private int minIntensityValue = 0;

    @BeanField @Getter @Setter private boolean suppress3D = false;
    // END PROPERTIES

    protected abstract void applyMorphOp(Mask imgChnl, boolean do3D) throws CreateException;

    @Override
    public Mask createFromChnl(Mask chnl) throws CreateException {

        // Gets outline
        applyMorphOp(chnl, (chnl.getDimensions().getZ() > 1) && !suppress3D);

        return chnl;
    }

    protected Optional<VoxelBox<ByteBuffer>> backgroundVb() throws CreateException {

        if (minIntensityValue > 0) {
            return Optional.of(backgroundChnlProvider.create().getVoxelBox().asByte());
        } else {
            return Optional.empty();
        }
    }
}
