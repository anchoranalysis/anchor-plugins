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

package org.anchoranalysis.plugin.image.bean.scale;

import java.util.Optional;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.bean.xml.exception.ProvisionFailedException;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.image.bean.provider.DimensionsProvider;
import org.anchoranalysis.image.bean.spatial.ScaleCalculator;
import org.anchoranalysis.image.core.dimensions.Dimensions;
import org.anchoranalysis.image.core.dimensions.size.suggestion.ImageSizeSuggestion;
import org.anchoranalysis.plugin.image.bean.dimensions.provider.SpecifyDimensions;
import org.anchoranalysis.spatial.scale.RelativeScaleCalculator;
import org.anchoranalysis.spatial.scale.ScaleFactor;

/**
 * Calculates a scaling-factor to make the source image have identical dimensions as {@code
 * dimensionsTarget}.
 *
 * <p>If {@code preserveAspectRatio} is true, then the aspect ratio is preserved, and both
 * dimensions may not be identical to {@code dimensionsTarget}, but at least one will be, and the
 * other will not be exceed that of {@code dimensionsTarget}.
 *
 * @author Owen Feehan
 */
@NoArgsConstructor
public class ToDimensions extends ScaleCalculator {

    // START BEAN PROPERTIES
    /** The dimensions the source image/entity is desired to be scaled to. */
    @BeanField @Getter @Setter private DimensionsProvider dimensions;

    /**
     * Source dimensions used as a fallback, if they are not passed as parameter to {@link
     * #calculate}.
     */
    @BeanField @OptionalBean @Getter @Setter private DimensionsProvider dimensionsSourceFallback;

    /**
     * If true, the ratio between x and y is kept constant when scaling.
     *
     * <p>The larger dimension is will be scaled to be the same as {@code dimensionsTarget} and the
     * the smaller dimension is guaranteed to be equal to or lesser than its equivalent in {@code
     * dimensionsTarget}.
     */
    @BeanField @Getter @Setter private boolean preserveAspectRatio = false;

    // END BEAN PROPERTIES

    /**
     * Create with specific lengths for X and Y dimensions.
     *
     * @param sizeX the size of the X-dimension.
     * @param sizeY the size of the X-dimension.
     */
    public ToDimensions(int sizeX, int sizeY) {
        this(new SpecifyDimensions(sizeX, sizeY));
    }

    /**
     * Create with specific {@code dimensions}.
     *
     * @param dimensions the dimensions the source image/entity is desired to be scaled to.
     */
    public ToDimensions(DimensionsProvider dimensions) {
        this(dimensions, false);
    }

    /**
     * Create with specific lengths for X and Y dimensions and {@code preserveAspectRatio}.
     *
     * @param sizeX the size of the X-dimension.
     * @param sizeY the size of the X-dimension.
     * @param preserveAspectRatio if true, the ratio between x and y is kept constant when scaling.
     */
    public ToDimensions(int sizeX, int sizeY, boolean preserveAspectRatio) {
        this(sizeX, sizeY);
        this.preserveAspectRatio = preserveAspectRatio;
    }

    /**
     * Create with specific {@code dimensions} and {@code preserveAspectRatio}.
     *
     * @param dimensions the dimensions the source image/entity is desired to be scaled to.
     * @param preserveAspectRatio if true, the ratio between x and y is kept constant when scaling.
     */
    public ToDimensions(DimensionsProvider dimensions, boolean preserveAspectRatio) {
        this.dimensions = dimensions;
        this.preserveAspectRatio = preserveAspectRatio;
    }

    @Override
    public ScaleFactor calculate(
            Optional<Dimensions> dimensionsToBeScaled, Optional<ImageSizeSuggestion> suggestedSize)
            throws OperationFailedException {

        Optional<Dimensions> dimensionsCombined =
                maybeReplaceSourceDimensions(dimensionsToBeScaled);

        if (dimensionsCombined.isPresent()) {
            try {
                return RelativeScaleCalculator.relativeScale(
                        dimensionsCombined.get().extent(),
                        dimensions.get().extent(),
                        preserveAspectRatio);
            } catch (ProvisionFailedException e) {
                throw new OperationFailedException(e);
            }
        } else {
            throw new OperationFailedException("No source dimensions can be found");
        }
    }

    private Optional<Dimensions> maybeReplaceSourceDimensions(Optional<Dimensions> sourceDimensions)
            throws OperationFailedException {
        if (dimensionsSourceFallback != null) {
            try {
                return Optional.of(dimensionsSourceFallback.get());
            } catch (ProvisionFailedException e) {
                throw new OperationFailedException(e);
            }
        } else {
            return sourceDimensions;
        }
    }
}
