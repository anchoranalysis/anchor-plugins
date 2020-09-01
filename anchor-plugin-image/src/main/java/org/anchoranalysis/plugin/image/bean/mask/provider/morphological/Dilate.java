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

import java.nio.ByteBuffer;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.BeanInstanceMap;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.error.BeanMisconfiguredException;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.friendly.AnchorImpossibleSituationException;
import org.anchoranalysis.image.binary.mask.Mask;
import org.anchoranalysis.image.binary.voxel.BinaryVoxels;
import org.anchoranalysis.image.extent.IncorrectImageSizeException;
import org.anchoranalysis.image.object.morphological.MorphologicalDilation;

/** Performs an dilation morphological operation on a binary-image */
public class Dilate extends MorphologicalOperatorBase {

    // START BEAN FIELDS
    @BeanField @Getter @Setter private boolean zOnly = false; // Only dilates in the z-direction

    @BeanField @Getter @Setter private boolean bigNeighborhood = false;
    // END BEAN FIELDS

    // Checks that a mark's initial parameters are correct
    @Override
    public void checkMisconfigured(BeanInstanceMap defaultInstances)
            throws BeanMisconfiguredException {
        super.checkMisconfigured(defaultInstances);
        if (isSuppress3D() && zOnly) {
            throw new BeanMisconfiguredException(
                    "Either suppress3D and zOnly may be selected, but not both");
        }
    }

    // Assumes imgChannelOut has the same ImgChannelRegions
    @Override
    protected void applyMorphologicalOperation(Mask source, boolean do3D) throws CreateException {

        BinaryVoxels<ByteBuffer> out =
                MorphologicalDilation.dilate(
                        source.binaryVoxels(),
                        do3D,
                        getIterations(),
                        background(),
                        getMinIntensityValue(),
                        zOnly,
                        false,
                        Optional.empty(),
                        bigNeighborhood);

        try {
            source.replaceBy(out);
        } catch (IncorrectImageSizeException e) {
            throw new AnchorImpossibleSituationException();
        }
    }
}
