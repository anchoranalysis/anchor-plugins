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

package org.anchoranalysis.plugin.image.bean.mask.provider.morphological;

import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.xml.exception.ProvisionFailedException;
import org.anchoranalysis.core.exception.CreateException;
import org.anchoranalysis.core.exception.friendly.AnchorImpossibleSituationException;
import org.anchoranalysis.image.core.dimensions.IncorrectImageSizeException;
import org.anchoranalysis.image.core.mask.Mask;
import org.anchoranalysis.image.voxel.binary.BinaryVoxels;
import org.anchoranalysis.image.voxel.buffer.primitive.UnsignedByteBuffer;
import org.anchoranalysis.image.voxel.kernel.OutsideKernelPolicy;
import org.anchoranalysis.image.voxel.kernel.morphological.DilationContext;
import org.anchoranalysis.image.voxel.object.morphological.MorphologicalDilation;

/**
 * Performs a <b>dilation</b> morphological operation on {@link BinaryVoxels}.
 *
 * @author Owen Feehan
 */
public class Dilate extends MorphologicalOperatorBase {

    // START BEAN FIELDS
    @BeanField @Getter @Setter private boolean bigNeighborhood = false;
    // END BEAN FIELDS

    // Assumes imgChannelOut has the same ImgChannelRegions
    @Override
    protected void applyMorphologicalOperation(Mask source, boolean do3D) {

        try {
            BinaryVoxels<UnsignedByteBuffer> out =
                    MorphologicalDilation.dilate(
                            source.binaryVoxels(),
                            getIterations(),
                            new DilationContext(
                                    OutsideKernelPolicy.AS_OFF,
                                    do3D,
                                    bigNeighborhood,
                                    precondition()));

            source.replaceBy(out);
        } catch (IncorrectImageSizeException | CreateException | ProvisionFailedException e) {
            throw new AnchorImpossibleSituationException();
        }
    }
}
