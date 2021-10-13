/*-
 * #%L
 * anchor-plugin-image
 * %%
 * Copyright (C) 2010 - 2021 Owen Feehan, ETH Zurich, University of Zurich, Hoffmann-La Roche
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
package org.anchoranalysis.plugin.image.bean.scale;

import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.image.bean.spatial.ScaleCalculator;
import org.anchoranalysis.image.bean.spatial.SizeXY;
import org.anchoranalysis.image.core.dimensions.Dimensions;
import org.anchoranalysis.image.core.dimensions.size.ResizeExtentUtilities;
import org.anchoranalysis.image.core.dimensions.size.suggestion.ImageSizeSuggestion;
import org.anchoranalysis.spatial.box.Extent;
import org.anchoranalysis.spatial.scale.ScaleFactor;

/**
 * Finds largest multiple of an {@link Extent} without being larger than another {@link Extent}.
 *
 * <p>Each dimension is calculated separately, and then the minimum scaling-factor is used for both.
 *
 * <p>e.g. for the X dimension in {@code minimumSize}, the maximum <code>multiple</code> is selected
 * so that <code>(unscaledSizeX * 2^multiple)</code> is less than <code>maxSixeX</code>.
 *
 * @author Owen Feehan
 */
public class LargestMultipleWithin extends ScaleCalculator {

    // START BEAN PROPERTIES
    /**
     * The minimum size in each dimension. Integer multiples are considered separately in each
     * dimension.
     */
    private @BeanField @Getter @Setter SizeXY minimumSize;

    /** An upper limit on the scale-factor allowed in each dimension. */
    private @BeanField @Getter @Setter int maxScaleFactor;
    // END BEAN PROPERTIES

    @Override
    public ScaleFactor calculate(
            Optional<Dimensions> dimensionsToBeScaled,
            Optional<ImageSizeSuggestion> suggestedResize)
            throws OperationFailedException {

        if (!dimensionsToBeScaled.isPresent()) {
            throw new OperationFailedException(
                    "dimensionsToBeScaled is required by the plugin but is missing.");
        }
        Extent originalSize = dimensionsToBeScaled.get().extent();
        Extent sizeLargestMultiple =
                FindLargestMultipleWithin.apply(
                        minimumSize.asExtent(), originalSize, maxScaleFactor);
        return ResizeExtentUtilities.relativeScale(originalSize, sizeLargestMultiple);
    }
}
