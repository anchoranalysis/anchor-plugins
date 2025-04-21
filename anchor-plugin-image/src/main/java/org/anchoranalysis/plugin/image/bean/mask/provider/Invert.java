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

package org.anchoranalysis.plugin.image.bean.mask.provider;

import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.OptionalProviderFactory;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.bean.xml.exception.ProvisionFailedException;
import org.anchoranalysis.image.bean.provider.MaskProvider;
import org.anchoranalysis.image.bean.provider.MaskProviderUnary;
import org.anchoranalysis.image.core.mask.IterateVoxelsMask;
import org.anchoranalysis.image.core.mask.Mask;
import org.anchoranalysis.image.core.mask.MaskInverter;
import org.anchoranalysis.image.voxel.binary.values.BinaryValuesByte;
import org.anchoranalysis.image.voxel.buffer.primitive.UnsignedByteBuffer;
import org.anchoranalysis.spatial.point.Point3i;

/**
 * Switches <i>on</i> voxels to <i>off</i> and vice-versa.
 *
 * <p>By default, this occurs by modifying the associated binary-values (an index) rather than
 * modifying the voxel buffers.
 *
 * @author Owen Feehan
 */
public class Invert extends MaskProviderUnary {

    // START BEAN FIELDS
    /**
     * If set, the inversion only occurs on a particular region of the mask, as determined by this
     * mask
     */
    @BeanField @OptionalBean @Getter @Setter private MaskProvider restrictTo;

    /**
     * If true, rather than modifying the binary-values (an index) bytes are modified in the buffer
     * to reflect the existing binary-values.
     */
    @BeanField @Getter @Setter private boolean forceChangeBytes = false;

    // END BEAN FIELDS

    @Override
    public Mask createFromMask(Mask maskToInvert) throws ProvisionFailedException {

        Optional<Mask> restricted = OptionalProviderFactory.create(restrictTo);

        if (restricted.isPresent()) {
            invertWithMask(maskToInvert, restricted.get());
            return maskToInvert;
        } else {

            if (forceChangeBytes) {
                MaskInverter.invert(maskToInvert);
                return maskToInvert;
            } else {
                return new Mask(
                        maskToInvert.channel(), maskToInvert.binaryValuesInt().createInverted());
            }
        }
    }

    private void invertWithMask(Mask maskToInvert, Mask restricted) {

        BinaryValuesByte invertedIndex = maskToInvert.binaryValuesByte();
        final byte byteOn = invertedIndex.getOn();
        final byte byteOff = invertedIndex.getOff();

        IterateVoxelsMask.withBuffer(
                restricted,
                maskToInvert.binaryVoxels().voxels(),
                (Point3i point, UnsignedByteBuffer buffer, int offset) -> {
                    if (buffer.getRaw(offset) == byteOn) {
                        buffer.putRaw(offset, byteOff);
                    } else {
                        buffer.putRaw(offset, byteOn);
                    }
                });
    }
}
