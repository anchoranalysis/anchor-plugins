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

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.imglib2.algorithm.gauss3.Gauss3;
import net.imglib2.exception.IncompatibleTypeException;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;
import org.anchoranalysis.image.extent.Resolution;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
class GaussianBlurUtilities {

    /**
     * Applies a Gaussian blur to an ImgLib2 image.
     *
     * <p>This is a mutable operation that alters the current image
     *
     * @param <T> ImgLib2 data-type
     * @param img the image
     * @param sr the resolution of the image (assumes X and Y are the same)
     * @param sigma the sigma parameter for the gaussian blur for each dimension (should be length
     *     3)
     * @throws IncompatibleTypeException
     */
    public static <T extends NumericType<T>> void applyBlur(
            Img<? extends RealType<?>> img, Resolution sr, double[] sigma) {

        assert (sigma.length == 3);

        // This is a safe upcasting operation
        @SuppressWarnings("unchecked")
        Img<T> imgCast = (Img<T>) img;

        Gauss3.gauss(sigma, Views.extendMirrorSingle(imgCast), imgCast);
    }
}
