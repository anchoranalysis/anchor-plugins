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
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.image.bean.spatial.ScaleCalculator;
import org.anchoranalysis.image.core.dimensions.Dimensions;
import org.anchoranalysis.image.core.dimensions.size.suggestion.ImageSizeSuggestion;
import org.anchoranalysis.spatial.scale.ScaleFactor;

/**
 * Scales to the suggested-size if one is provided, otherwise calls {@code fallback}.
 *
 * @author Owen Feehan
 */
@NoArgsConstructor
@AllArgsConstructor
public class ToSuggested extends ScaleCalculator {

    // START BEAN PROPERTIES
    /**
     * Used as a delegate to calculate the scale if {@code suggestedSize} is empty when passed to
     * {@link #calculate}.
     */
    @BeanField @Getter @Setter private ScaleCalculator fallback = new ConstantScaleFactor(0.5);
    // END BEAN PROPERTIES

    @Override
    public ScaleFactor calculate(
            Optional<Dimensions> dimensionsToBeScaled, Optional<ImageSizeSuggestion> suggestedSize)
            throws OperationFailedException {
        if (suggestedSize.isPresent()) {
            return suggestedSize
                    .get()
                    .calculateScaleFactor(dimensionsToBeScaled.map(Dimensions::extent));
        } else {
            return fallback.calculate(dimensionsToBeScaled, suggestedSize);
        }
    }
}
