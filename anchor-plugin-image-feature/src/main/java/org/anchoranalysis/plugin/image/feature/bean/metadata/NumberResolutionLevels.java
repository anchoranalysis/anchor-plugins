/*-
 * #%L
 * anchor-plugin-image-feature
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
package org.anchoranalysis.plugin.image.feature.bean.metadata;

import org.anchoranalysis.feature.calculate.FeatureCalculationException;
import org.anchoranalysis.image.core.stack.ImageMetadata;
import org.anchoranalysis.image.core.stack.ImagePyramidMetadata;
import org.anchoranalysis.image.feature.bean.FeatureImageMetadata;

/**
 * The number of resolution-levels in an image in a file.
 *
 * <p>This is typically 1 (the resolution of the image), but if the image is stored in a pyramidal
 * format, this may be more than 1, offering several different resolutions to capture the same
 * underlying image.
 *
 * <p>See <a href="https://en.wikipedia.org/wiki/Pyramid_(image_processing)">Pyramid (image
 * processing)</a>.
 *
 * @author Owen Feehan
 */
public class NumberResolutionLevels extends FeatureImageMetadata {

    @Override
    public double calculate(ImageMetadata metadata) throws FeatureCalculationException {
        return metadata.getPyramid().map(ImagePyramidMetadata::getResolutionCount).orElse(1);
    }
}
