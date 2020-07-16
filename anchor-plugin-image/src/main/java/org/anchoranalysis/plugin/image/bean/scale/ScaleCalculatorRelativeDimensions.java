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
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.image.bean.provider.ImageDimProvider;
import org.anchoranalysis.image.bean.scale.ScaleCalculator;
import org.anchoranalysis.image.extent.ImageDimensions;
import org.anchoranalysis.image.scale.ScaleFactor;
import org.anchoranalysis.image.scale.ScaleFactorUtilities;
import lombok.Getter;
import lombok.Setter;

public class ScaleCalculatorRelativeDimensions extends ScaleCalculator {

    // START BEAN PROPERTIES
    @BeanField @OptionalBean @Getter @Setter private ImageDimProvider dimSource;

    @BeanField @Getter @Setter private ImageDimProvider dimTarget;
    // END BEAN PROPERTIES

    @Override
    public ScaleFactor calc(Optional<ImageDimensions> sourceDimensions) throws OperationFailedException {

        Optional<ImageDimensions> dimensions = maybeReplaceSourceDimensions(sourceDimensions);

        if (!dimensions.isPresent()) {
            throw new OperationFailedException("No source dimensions can be found");
        }

        try {
            return ScaleFactorUtilities.calcRelativeScale(
                    dimensions.get().getExtent(),
                    dimTarget.create().getExtent()
            );
        } catch (CreateException e) {
            throw new OperationFailedException(e);
        }
    }
    
    private Optional<ImageDimensions> maybeReplaceSourceDimensions(Optional<ImageDimensions> sourceDimensions) throws OperationFailedException {
        if (dimSource != null) {
            try {
                return Optional.of( dimSource.create() );
            } catch (CreateException e) {
                throw new OperationFailedException(e);
            }
        } else {
            return sourceDimensions; 
        }
    }
}
