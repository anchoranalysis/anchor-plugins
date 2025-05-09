/*-
 * #%L
 * anchor-plugin-ij
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

package org.anchoranalysis.plugin.imagej.bean.threshold;

import ij.Prefs;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.image.bean.threshold.Thresholder;
import org.anchoranalysis.image.voxel.VoxelsUntyped;
import org.anchoranalysis.image.voxel.binary.BinaryVoxels;
import org.anchoranalysis.image.voxel.binary.values.BinaryValuesByte;
import org.anchoranalysis.image.voxel.buffer.primitive.UnsignedByteBuffer;
import org.anchoranalysis.image.voxel.object.ObjectMask;
import org.anchoranalysis.image.voxel.thresholder.VoxelsThresholder;
import org.anchoranalysis.math.histogram.Histogram;
import org.anchoranalysis.plugin.imagej.mask.ApplyImageJMorphologicalOperation;

/**
 * A thresholder that applies a simple intensity threshold and then fills holes in 2D.
 *
 * <p>This thresholder first applies an intensity threshold to create a binary image, and then uses
 * ImageJ's "fill" morphological operation to fill holes in each 2D slice.
 */
@NoArgsConstructor
@AllArgsConstructor
public class ThresholderSimpleFillHoles2D extends Thresholder {

    static {
        Prefs.blackBackground = true;
    }

    /**
     * The minimum intensity value for thresholding.
     *
     * <p>Voxels with intensity greater than or equal to this value are considered foreground.
     */
    @BeanField @Getter @Setter private int minIntensity = -1;

    // No additional doc-strings needed as the only method is overridden

    @Override
    public BinaryVoxels<UnsignedByteBuffer> threshold(
            VoxelsUntyped inputBuffer,
            BinaryValuesByte binaryValues,
            Optional<Histogram> histogram,
            Optional<ObjectMask> objectMask)
            throws OperationFailedException {

        if (objectMask.isPresent()) {
            throw new OperationFailedException("A mask is not supported for this operation");
        }

        BinaryVoxels<UnsignedByteBuffer> thresholded =
                VoxelsThresholder.threshold(
                        inputBuffer, minIntensity, binaryValues, objectMask, false);

        ApplyImageJMorphologicalOperation.applyOperation(thresholded, "fill", 1);

        return thresholded;
    }
}
