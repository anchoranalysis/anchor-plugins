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

package org.anchoranalysis.plugin.image.bean.mask.provider.slice;

import java.nio.ByteBuffer;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.bean.provider.DimensionsProvider;
import org.anchoranalysis.image.bean.provider.MaskProviderUnary;
import org.anchoranalysis.image.binary.mask.Mask;
import org.anchoranalysis.image.binary.mask.MaskFactory;
import org.anchoranalysis.image.extent.Dimensions;
import org.anchoranalysis.image.extent.Extent;
import org.anchoranalysis.plugin.image.bean.dimensions.provider.GuessDimensions;

/**
 * Creates a new mask with specific dimensions that repeatedly duplicates a slice from an existing
 * mask
 *
 * <p>The incoming {@code mask} must have the same extent in XY as specified in {@code dimension}.
 *
 * @author Owen Feehan
 */
public class RepeatSlice extends MaskProviderUnary {

    // START BEAN PROPERTIES
    /** Dimensions to create new mask */
    @BeanField @Getter @Setter private DimensionsProvider dimensions = new GuessDimensions();

    /** Which slice to use from {@code mask} */
    @BeanField @Getter @Setter private int sliceIndex = 0;
    // END BEAN PROPERTIES

    @Override
    public Mask createFromMask(Mask mask) throws CreateException {

        Dimensions dimensionsForOutput = dimensions.create();

        Mask out = createEmptyMask(mask, dimensionsForOutput);

        // Always takes the same slice as input buffer
        ByteBuffer bufferSliceToRepeat = mask.sliceBuffer(sliceIndex);

        Extent extent = dimensionsForOutput.extent();
        extent.iterateOverZ(z -> copySliceInto(bufferSliceToRepeat, z, extent, out));
        return out;
    }

    private Mask createEmptyMask(Mask mask, Dimensions dimensionsForOutput) throws CreateException {

        if (!mask.extent().equalsIgnoreZ(dimensionsForOutput.extent())) {
            throw new CreateException(
                    "The slice does not have the same XY extent as specified in dimensions");
        }

        return MaskFactory.createMaskOff(dimensionsForOutput);
    }

    private static void copySliceInto(
            ByteBuffer bufferSliceToRepeat, int sliceIndexOut, Extent extent, Mask out) {
        // Variously takes different z slices
        ByteBuffer bufferOut = out.sliceBuffer(sliceIndexOut);

        extent.iterateOverXYOffset(
                offset -> bufferOut.put(offset, bufferSliceToRepeat.get(offset)));
    }
}
