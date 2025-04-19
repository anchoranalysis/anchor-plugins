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

package org.anchoranalysis.plugin.image.bean.blur;

import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.AnchorBean;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.Positive;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.core.log.MessageLogger;
import org.anchoranalysis.image.core.dimensions.Dimensions;
import org.anchoranalysis.image.voxel.VoxelsUntyped;

/**
 * A method for applying blurring to an image.
 *
 * @author Owen Feehan
 */
public abstract class BlurStrategy extends AnchorBean<BlurStrategy> {

    // START BEAN PROPERTIES
    /** The sigma value for the blur operation. */
    @BeanField @Positive @Getter @Setter private double sigma = 3;

    /**
     * If true, treats sigma as if it's in meters (physical units). If false, treats sigma as if
     * it's in pixels.
     */
    @BeanField @Getter @Setter private boolean sigmaInMeters = false;
    // END BEAN PROPERTIES

    /**
     * Applies the blur operation to the given voxels.
     *
     * @param voxels the {@link VoxelsUntyped} to blur
     * @param dimensions the {@link Dimensions} of the voxels
     * @param logger the {@link MessageLogger} for logging messages
     * @throws OperationFailedException if the blur operation fails
     */
    public abstract void blur(VoxelsUntyped voxels, Dimensions dimensions, MessageLogger logger)
            throws OperationFailedException;

    /**
     * Calculates the sigma value to use for blurring, considering the dimensions and whether sigma
     * is in meters.
     *
     * @param dimensions the {@link Dimensions} of the image
     * @param logger the {@link MessageLogger} for logging messages
     * @return the calculated sigma value
     * @throws OperationFailedException if the calculation fails or the sigma is too large
     */
    protected double calculateSigma(Dimensions dimensions, MessageLogger logger)
            throws OperationFailedException {

        double sigmaToUse = sigma;

        if (sigmaInMeters) {

            if (dimensions.unitConvert().isPresent()) {

                // Then we reconcile our sigma in microns against the Pixel Size XY (Z is taken care
                // of
                // later)
                sigmaToUse = dimensions.unitConvert().get().fromPhysicalDistance(sigma); // NOSONAR

                logger.logFormatted("Converted sigmaInMeters=%f into sigma=%f", sigma, sigmaToUse);
            } else {
                throw new OperationFailedException(
                        "Sigma is specified in meters, but no image-resolution is present");
            }
        }

        if (sigmaToUse > dimensions.x() || sigmaToUse > dimensions.y()) {
            throw new OperationFailedException(
                    "The calculated sigma is FAR TOO LARGE. It is larger than the entire channel it is applied to");
        }

        return sigmaToUse;
    }
}
