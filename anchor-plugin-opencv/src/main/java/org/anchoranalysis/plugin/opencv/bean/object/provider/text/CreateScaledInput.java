/* (C)2020 */
package org.anchoranalysis.plugin.opencv.bean.object.provider.text;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.extent.Extent;
import org.anchoranalysis.image.scale.ScaleFactor;
import org.anchoranalysis.image.scale.ScaleFactorUtilities;
import org.anchoranalysis.image.stack.Stack;
import org.anchoranalysis.plugin.opencv.MatConverter;
import org.apache.commons.math3.util.Pair;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

/**
 * Creates a scaled-version of a stack to use as input
 *
 * @author Owen Feehan
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
class CreateScaledInput {

    /**
     * Returns a scaled-down version of the stack, and a scale-factor that would return it to
     * original size
     */
    public static Pair<Mat, ScaleFactor> apply(Stack stack, Extent targetExtent)
            throws CreateException {

        // TODO Better to scale before openCV conversion, so less bytes to process for RGB
        // conversion
        Mat original = MatConverter.makeRGBStack(stack);

        Mat input = resizeMatToTarget(original, targetExtent);

        ScaleFactor sf = calcRelativeScale(original, input);

        return new Pair<>(input, sf);
    }

    private static ScaleFactor calcRelativeScale(Mat original, Mat resized) {
        return ScaleFactorUtilities.calcRelativeScale(
                extentFromMat(resized), extentFromMat(original));
    }

    private static Extent extentFromMat(Mat mat) {
        return new Extent(mat.cols(), mat.rows(), 1);
    }

    private static Mat resizeMatToTarget(Mat src, Extent targetExtent) {
        Mat dst = new Mat();
        Size sz = new Size(targetExtent.getX(), targetExtent.getY());
        Imgproc.resize(src, dst, sz);
        return dst;
    }
}
