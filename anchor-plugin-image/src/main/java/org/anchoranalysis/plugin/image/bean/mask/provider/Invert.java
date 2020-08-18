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

import java.nio.ByteBuffer;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.OptionalFactory;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.geometry.Point3i;
import org.anchoranalysis.image.bean.provider.MaskProviderUnary;
import org.anchoranalysis.image.bean.provider.MaskProvider;
import org.anchoranalysis.image.binary.mask.Mask;
import org.anchoranalysis.image.binary.mask.MaskInverter;
import org.anchoranalysis.image.binary.values.BinaryValuesByte;
import org.anchoranalysis.image.voxel.iterator.IterateVoxels;

/**
 * Switches <i>on</i> voxels to <i>off</i> and vice-versa.
 * 
 * <p>By default, this occurs by modifying the associated binary-values (an index) rather
 * than modifying the voxel buffers.</p>
 * 
 * @author Owen Feehan
 *
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
    public Mask createFromMask(Mask maskToInvert) throws CreateException {

        Optional<Mask> restricted = OptionalFactory.create(restrictTo);

        if (restricted.isPresent()) {
            invertWithMask(maskToInvert, restricted.get());
            return maskToInvert;
        } else {

            if (forceChangeBytes) {
                MaskInverter.invert(maskToInvert);
                return maskToInvert;
            } else {
                return new Mask(
                        maskToInvert.channel(), maskToInvert.binaryValues().createInverted());
            }
        }
    }

    private void invertWithMask(Mask maskToInvert, Mask restricted) {

        BinaryValuesByte invertedIndex = maskToInvert.binaryValues().createByte();
        final byte byteOn = invertedIndex.getOnByte();
        final byte byteOff = invertedIndex.getOffByte();
        
        IterateVoxels.callEachPoint(
                maskToInvert.binaryVoxels().voxels(),
                restricted,
                (Point3i point, ByteBuffer buffer, int offset) -> {
                    byte value = buffer.get(offset);

                    if (value == byteOn) {
                        buffer.put(offset, byteOff);
                    } else {
                        buffer.put(offset, byteOn);
                    }
                });
    }
}
