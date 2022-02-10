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

import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.core.exception.friendly.AnchorImpossibleSituationException;
import org.anchoranalysis.core.functional.OptionalUtilities;
import org.anchoranalysis.image.bean.spatial.ScaleCalculator;
import org.anchoranalysis.image.bean.spatial.arrange.StackArranger;
import org.anchoranalysis.image.bean.spatial.arrange.align.BoxAligner;
import org.anchoranalysis.image.bean.spatial.arrange.tile.Tile;
import org.anchoranalysis.image.core.dimensions.Dimensions;
import org.anchoranalysis.image.core.dimensions.size.suggestion.ImageSizeSuggestion;
import org.anchoranalysis.spatial.box.Extent;
import org.anchoranalysis.spatial.scale.ScaleFactor;

/**
 * Creates a {@link StackArranger} when the image-size <b>is not allowed vary</b>.
 *
 * @author Owen Feehan
 */
@AllArgsConstructor
class FixedImageSizeArranger {

    /** If no scaling-factor is suggested, this determines the size of each image. */
    private final ScaleCalculator fixedSizeScaler;

    /** How to align each image in the corresponding table cell. */
    private final BoxAligner aligner;

    /**
     * Create a {@link StackArranger} when image-size is <b>not</b> allowed vary (from its initial
     * size).
     *
     * @param numberRows the number of rows in the montage.
     * @param suggestion any suggestion on the size of the combined-image.
     * @param imageSizes the sizes of all images (and their corresponding paths). Each element is
     *     altered during the call to this function.
     * @return a newly created {@link StackArranger}.
     * @throws OperationFailedException if an invalid {@code suggestedSize} is assigned.
     */
    public StackArranger create(
            int numberRows, Optional<ImageSizeSuggestion> suggestion, List<SizeMapping> imageSizes)
            throws OperationFailedException {
        // Apply a scaling factor to the images, as we don't wish to use their original sizes
        scaleAllImages(imageSizes, suggestion);

        int numberColumns = calculateNumberColumns(imageSizes.size(), numberRows);

        // A strictly tabular form, where each image must fit inside its cell-size.
        Tile tile = new Tile();
        tile.setNumberColumns(numberColumns);
        tile.setNumberRows(numberRows);
        tile.setAligner(aligner);
        return tile;
    }

    /** Scale each element in {@code imageSizes}. */
    private void scaleAllImages(
            List<SizeMapping> imageSizes, Optional<ImageSizeSuggestion> suggestion)
            throws OperationFailedException {
        Optional<ScaleFactor> scaleFactor =
                OptionalUtilities.map(suggestion, FixedImageSizeArranger::extractScaleFactor);

        for (SizeMapping mapping : imageSizes) {
            scaleSizeMapping(mapping, scaleFactor);
        }
    }

    /** Scales the {@link Extent} in a pair. */
    private void scaleSizeMapping(SizeMapping mapping, Optional<ScaleFactor> scaleFactor) {
        try {
            if (scaleFactor.isPresent()) {
                Extent second = mapping.getExtent().scaleXYBy(scaleFactor.get(), true);
                mapping.assignExtent(second);
            } else {
                Dimensions dimensions = new Dimensions(mapping.getExtent());
                ScaleFactor factor =
                        fixedSizeScaler.calculate(Optional.of(dimensions), Optional.empty());
                mapping.assignExtent(mapping.getExtent().scaleXYBy(factor, true));
            }
        } catch (OperationFailedException e) {
            throw new AnchorImpossibleSituationException();
        }
    }

    /**
     * Extracts a uniform {@link ScaleFactor} from {@code suggestion} throwing a {@link
     * OperationFailedException} if none is available.
     */
    private static ScaleFactor extractScaleFactor(ImageSizeSuggestion suggestion)
            throws OperationFailedException {
        Optional<ScaleFactor> factor = suggestion.uniformScaleFactor();
        return factor.orElseThrow(
                () ->
                        new OperationFailedException(
                                "Only a constant scaling-factor is supported for this operation. Scaling that resizes to a specific width or height is unsupported."));
    }

    /** Calculates the number of columns to use in the table. */
    private static int calculateNumberColumns(int totalNumberImages, int numberRows) {
        return (int) Math.ceil(((double) totalNumberImages) / numberRows);
    }
}
