/* (C)2020 */
package org.anchoranalysis.plugin.image.bean.blur;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.imglib2.algorithm.gauss3.Gauss3;
import net.imglib2.exception.IncompatibleTypeException;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;
import org.anchoranalysis.image.extent.ImageResolution;

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
            Img<? extends RealType<?>> img, ImageResolution sr, double[] sigma) {

        assert (sigma.length == 3);

        // This is a safe upcasting operation
        @SuppressWarnings("unchecked")
        Img<T> imgCast = (Img<T>) img;

        Gauss3.gauss(sigma, Views.extendMirrorSingle(imgCast), imgCast);
    }
}
