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
import org.anchoranalysis.image.core.dimensions.size.suggestion.ImageSizeSuggestion;
import org.anchoranalysis.spatial.box.Extent;
import org.anchoranalysis.spatial.scale.RelativeScaleCalculator;
import org.anchoranalysis.spatial.scale.ScaleFactor;

/**
 * Scales an image to approximately similar in size to a {@link SizeXY}.
 *
 * <p>Optionally, the aspect-ratio is preserved between width and height.
 *
 * <p>Optionally, the eventual width, and height can be rounded to the nearest whole multiple of an
 * integer.
 *
 * @author Owen Feehan
 */
public class FitTo extends ScaleCalculator {

    // START BEAN PROPERTIES
    /**
     * The target size. The image will be scaled to be as similar to this as possible, preserving
     * aspect ratio.
     */
    private @BeanField @Getter @Setter SizeXY targetSize;

    /**
     * If true, the aspect-ratio is preserved between width and height. Otherwise, they are treated
     * independently.
     */
    private @BeanField @Getter @Setter boolean preserveAspectRatio = true;

    /**
     * The eventual width and height of the image must be a multiple of this number. Effecitvely
     * disabled when {@code == 1}
     */
    private @BeanField @Getter @Setter int multipleOf = 1;
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

        // What the new size will be after scaling to fit the target.
        Extent resized = calculateResized(originalSize);

        // If necessary, round each dimension to the closest whole multiple
        if (multipleOf != 1) {
            resized = roundExtentToNearestMultiple(resized, multipleOf);
        }

        return RelativeScaleCalculator.relativeScale(originalSize, resized);
    }

    /** The {@link ScaleFactor} to reduce from the original-size to the target-size. */
    private Extent calculateResized(Extent originalSize) {
        Extent target = targetSize.asExtent();
        if (preserveAspectRatio) {
            ScaleFactor scaleFactor =
                    RelativeScaleCalculator.relativeScalePreserveAspectRatio(originalSize, target);
            return originalSize.scaleXYBy(scaleFactor);
        } else {
            return target;
        }
    }

    /** Rounds the width and the height to the nearest multiple of a given number. */
    private static Extent roundExtentToNearestMultiple(Extent extent, int multipleOf) {
        return new Extent(
                roundToNearestMultiple(extent.x(), multipleOf),
                roundToNearestMultiple(extent.y(), multipleOf),
                extent.z());
    }

    /** Rounds an integer to the nearest multiple of another integer. */
    private static int roundToNearestMultiple(int valueToRound, int multipleOf) {
        double valueAsDouble = valueToRound;
        return (int) Math.round(valueAsDouble / multipleOf) * multipleOf;
    }
}
