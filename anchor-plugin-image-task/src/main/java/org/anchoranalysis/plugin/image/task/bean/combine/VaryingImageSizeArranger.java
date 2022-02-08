/*-
 * #%L
 * anchor-plugin-image-task
 * %%
 * Copyright (C) 2010 - 2022 Owen Feehan, ETH Zurich, University of Zurich, Hoffmann-La Roche
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
package org.anchoranalysis.plugin.image.task.bean.combine;

import java.util.Optional;
import lombok.AllArgsConstructor;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.image.bean.spatial.arrange.StackArranger;
import org.anchoranalysis.image.bean.spatial.arrange.fill.Fill;
import org.anchoranalysis.image.core.dimensions.size.suggestion.ImageSizeSuggestion;
import org.anchoranalysis.spatial.scale.ScaleFactor;

/**
 * Creates a {@link StackArranger} when the image-size <b>is allowed vary</b>.
 *
 * @author Owen Feehan
 */
@AllArgsConstructor
class VaryingImageSizeArranger {

    /**
     * If no specific width or scaling-factor is suggested, this determines the default width, the
     * combined-montage should have.
     *
     * <p>The eventual width will be the maximum of this and the width calculated from {@code
     * defaultWidthRatio}.
     */
    private final int defaultWidth;

    /**
     * If no specific width or scaling-factor is suggested, this determines the default percentage
     * of the existing size, the combined-montage should have.
     *
     * <p>The eventual width will be the maximum of this and {@code defaultWidth}.
     */
    private final double defaultWidthRatio;

    /**
     * Create a {@link StackArranger} when image-size is allowed vary.
     *
     * @param numberRows the number of rows in the montage.
     * @param suggestion any suggestion on the size of the combined-image.
     * @param varyImageLocation either the order of images is allowed vary (when {@code true}), or
     *     the image order is preserved (when {@code false}).
     * @return a newly created {@link StackArranger}.
     * @throws OperationFailedException if an invalid {@code suggestedSize} is assigned.
     */
    public StackArranger create(
            int numberRows, Optional<ImageSizeSuggestion> suggestion, boolean varyImageLocation)
            throws OperationFailedException {
        Fill fill = new Fill();
        fill.setNumberRows(numberRows);
        assignWidthToFill(fill, suggestion);
        fill.setVaryNumberImagesPerRow(varyImageLocation);
        return fill;
    }

    /**
     * Assigns one or both of the {@code width} and {@code widthRatio} in {@link Fill} based on any
     * {@code suggestion}, or lack thereof.
     */
    private void assignWidthToFill(Fill fill, Optional<ImageSizeSuggestion> suggestion)
            throws OperationFailedException {
        if (suggestion.isPresent()) {
            assignWithToFillWithSuggestion(fill, suggestion.get());
        } else {
            // When no suggestion is specified, fallback on the defaults.
            fill.setWidthRatio(defaultWidthRatio);
            fill.setWidth(defaultWidth);
        }
    }

    /**
     * Assigns one or both of the {@code width} and {@code widthRatio} in {@link Fill} based on the
     * {@code suggestion}.
     */
    private static void assignWithToFillWithSuggestion(Fill fill, ImageSizeSuggestion suggestion)
            throws OperationFailedException {
        // First, look for uniform scaling.
        Optional<ScaleFactor> uniformScaling = suggestion.uniformScaleFactor();
        if (uniformScaling.isPresent()) {
            fill.setWidthRatio(uniformScaling.get().x());
            fill.setWidth(0);
            return;
        }

        // Then if it doesn't exist, then second, look instead for a uniform width.
        Optional<Integer> uniformWidth = suggestion.uniformWidth();
        if (uniformWidth.isPresent() && !suggestion.uniformHeight().isPresent()) {
            fill.setWidth(uniformWidth.get());
            fill.setWidthRatio(0.0);
        } else {
            // Otherwise, we do not have a valid size suggestion. So inform the user.
            throw new OperationFailedException(
                    "An invalid resize suggestion was specified for this mode. It must be either a constant scale-factor or a specific width: e.g. 0.5 or 1000x");
        }
    }
}
