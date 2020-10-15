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
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.log.MessageLogger;
import org.anchoranalysis.image.dimensions.Dimensions;
import org.anchoranalysis.image.voxel.VoxelsWrapper;

/**
 * A method for applying blurring to an image
 *
 * @author Owen Feehan
 */
public abstract class BlurStrategy extends AnchorBean<BlurStrategy> {

    // START BEAN PROPERTIES
    @BeanField @Positive @Getter @Setter private double sigma = 3;

    @BeanField @Getter @Setter
    private boolean sigmaInMeters = false; // Treats sigma if it's microns
    // END BEAN PROPERTIES

    public abstract void blur(VoxelsWrapper voxels, Dimensions dimensions, MessageLogger logger)
            throws OperationFailedException;

    protected double calculateSigma(Dimensions dimensions, MessageLogger logger)
            throws OperationFailedException {

        double sigmaToUse = sigma;

        if (sigmaInMeters) {
            
            if (dimensions.unitConvert().isPresent()) {
            
                // Then we reconcile our sigma in microns against the Pixel Size XY (Z is taken care of
                // later)
                sigmaToUse = dimensions.unitConvert().get().fromPhysicalDistance(sigma);    // NOSONAR
    
                logger.logFormatted("Converted sigmaInMeters=%f into sigma=%f", sigma, sigmaToUse);
            } else {
                throw new OperationFailedException("Sigma is specified in meters, but no image-resolution is present");
            }
        }

        if (sigmaToUse > dimensions.x() || sigmaToUse > dimensions.y()) {
            throw new OperationFailedException(
                    "The calculated sigma is FAR TOO LARGE. It is larger than the entire channel it is applied to");
        }

        return sigmaToUse;
    }
}
