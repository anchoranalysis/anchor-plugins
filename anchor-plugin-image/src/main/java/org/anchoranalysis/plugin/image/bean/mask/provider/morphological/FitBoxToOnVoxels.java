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
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.image.bean.provider.MaskProviderUnary;
import org.anchoranalysis.image.core.mask.Mask;
import org.anchoranalysis.image.voxel.binary.BinaryVoxels;
import org.anchoranalysis.image.voxel.binary.values.BinaryValuesByte;
import org.anchoranalysis.image.voxel.buffer.primitive.UnsignedByteBuffer;
import org.anchoranalysis.image.voxel.iterator.IterateVoxelsEqualTo;
import org.anchoranalysis.spatial.box.BoundingBox;
import org.anchoranalysis.spatial.box.PointRange;

/**
 * Fits a box around the <i>on</i> voxels.
 *
 * <p>The minimally-fitting (i.e. tightest) box that fits is always used.
 *
 * @author Owen Feehan
 */
public class FitBoxToOnVoxels extends MaskProviderUnary {

    // START BEAN PROPERTIES
    // If true, then each z slice is treated separately
    @BeanField @Getter @Setter private boolean slicesSeparately = false;
    // END BEAN PROPERTIES

    @Override
    public Mask createFromMask(Mask mask) throws ProvisionFailedException {

        if (slicesSeparately) {
            mask.extent()
                    .iterateOverZ(
                            z -> {
                                BoundingBox box =
                                        minimalBoxAroundMask(mask.extractSlice(z).binaryVoxels());
                                mask.assignOn().toBox(box.shiftToZ(z));
                            });
        } else {
            BoundingBox box = minimalBoxAroundMask(mask.binaryVoxels());
            mask.binaryVoxels().assignOn().toBox(box);
        }

        return mask;
    }

    private BoundingBox minimalBoxAroundMask(BinaryVoxels<UnsignedByteBuffer> voxels)
            throws ProvisionFailedException {

        PointRange pointRange = new PointRange();

        BinaryValuesByte binaryValues = voxels.binaryValues().asByte();
        IterateVoxelsEqualTo.equalToReusePoint(
                voxels.voxels(), binaryValues.getOn(), pointRange::add);

        try {
            return pointRange.toBoundingBox();
        } catch (OperationFailedException e) {
            throw new ProvisionFailedException(e);
        }
    }
}
