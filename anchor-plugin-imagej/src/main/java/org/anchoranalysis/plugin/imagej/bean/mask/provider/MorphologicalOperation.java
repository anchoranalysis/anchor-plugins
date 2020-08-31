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

package org.anchoranalysis.plugin.imagej.bean.mask.provider;

import java.nio.ByteBuffer;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.Positive;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.image.bean.provider.MaskProviderUnary;
import org.anchoranalysis.image.binary.mask.Mask;
import org.anchoranalysis.image.binary.voxel.BinaryVoxels;
import org.anchoranalysis.plugin.imagej.mask.ApplyImageJMorphologicalOperation;

/**
 * Applies an ImageJ (2D) morphological operation to each slice
 *
 * <p>Note that the slices are processed independently of each other, as the procedure only supports
 * 2D morphological operations.
 *
 * @author Owen Feehan
 */
public class MorphologicalOperation extends MaskProviderUnary {

    // START BEAN PROPERTIES
    /** One of: open, close, fill, erode, dilate, skel, outline */
    @BeanField @Getter @Setter private String command = "";

    /** iterations for erode, dilate, open, close */
    @BeanField @Positive @Getter @Setter private int iterations = 1;
    // END BEAN PROPERTIES

    @Override
    public Mask createFromMask(Mask mask) throws CreateException {
        try {
            BinaryVoxels<ByteBuffer> processed =
                    ApplyImageJMorphologicalOperation.applyOperation(
                            mask.binaryVoxels(), command, iterations);
            return new Mask(processed, mask.dimensions().resolution());
        } catch (OperationFailedException e) {
            throw new CreateException(e);
        }
    }
}
