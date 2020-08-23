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

package org.anchoranalysis.plugin.image.bean.mask.provider.resize;

import java.nio.ByteBuffer;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.image.bean.provider.MaskProviderUnary;
import org.anchoranalysis.image.binary.mask.Mask;
import org.anchoranalysis.image.binary.values.BinaryValuesByte;
import org.anchoranalysis.image.binary.voxel.BinaryVoxels;
import org.anchoranalysis.image.extent.BoundingBox;
import org.anchoranalysis.image.points.PointRange;
import org.anchoranalysis.image.voxel.iterator.IterateVoxelsByte;

/**
 * Fits a box around the ON pixels.
 *
 * <p>The minimally-fitting (i.e. tightest) box that fits is always used
 *
 * @author Owen Feehan
 */
public class FitBoxToOnVoxels extends MaskProviderUnary {

    // START BEAN PROPERTIES
    // If true, then each z slice is treated seperately
    @BeanField @Getter @Setter private boolean slicesSeperately = false;
    // END BEAN PROPERTIES

    @Override
    public Mask createFromMask(Mask mask) throws CreateException {

        if (slicesSeperately) {
            mask.extent().iterateOverZ( z-> {
                BoundingBox box = minimalBoxAroundMask(mask.extractSlice(z).binaryVoxels());
                mask.assignOn().toBox(box.shiftToZ(z));
            });
        } else {
            BoundingBox box = minimalBoxAroundMask(mask.binaryVoxels());
            mask.binaryVoxels().assignOn().toBox(box);
        }

        return mask;
    }

    private BoundingBox minimalBoxAroundMask(BinaryVoxels<ByteBuffer> voxels)
            throws CreateException {

        PointRange pointRange = new PointRange();

        BinaryValuesByte bvb = voxels.binaryValues().createByte();
        IterateVoxelsByte.iterateEqualValuesReusePoint(voxels.voxels(), bvb.getOnByte(), pointRange::add);

        try {
            return pointRange.deriveBoundingBox();
        } catch (OperationFailedException e) {
            throw new CreateException(e);
        }
    }
}