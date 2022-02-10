package org.anchoranalysis.plugin.opencv.bean.stack;

import java.util.Optional;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.anchoranalysis.core.log.Logger;
import org.anchoranalysis.image.core.dimensions.OrientationChange;
import org.anchoranalysis.image.io.ImageIOException;
import org.anchoranalysis.image.io.stack.CalculateOrientationChange;
import org.opencv.core.Core;
import org.opencv.core.Mat;

/**
 * Helper class to apply orientation changes to a {@link Mat}.
 *
 * @author Owen Feehan
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
class OrientationChanger {

    /**
     * Changes the orientation of the image, if necessary.
     *
     * @param image the image whose orientation is possibly changed (inplace).
     * @param calculateOrientationChange how to calculate any needed orientation-change.
     * @param logger the logger.
     * @throws ImageIOException if an unsupported orientation-changed operation is needed.
     */
    public static void changeOrientationIfNecessary(
            Mat image,
            Optional<CalculateOrientationChange> calculateOrientationChange,
            Logger logger)
            throws ImageIOException {
        OrientationChange orientationChange =
                calculateOrientation(calculateOrientationChange, logger);
        applyOrientationChange(image, orientationChange);
    }

    /**
     * Performs any necessary rotation and flipping of {@code image} according to {@link
     * OrientationChange}.
     *
     * <p>Note that OpenCV and Anchor seem to have opposite impressions of what clockwise and
     * anti-clockwise is (due to different coordinate systems).
     */
    private static void applyOrientationChange(Mat image, OrientationChange orientation)
            throws ImageIOException {
        switch (orientation) {
            case KEEP_UNCHANGED:
                // NOTHING TO DO
                break;
            case ROTATE_90_ANTICLOCKWISE:
                Core.rotate(image, image, Core.ROTATE_90_CLOCKWISE);
                break;
            case ROTATE_90_CLOCKWISE:
                Core.rotate(image, image, Core.ROTATE_90_COUNTERCLOCKWISE);
                break;
            case ROTATE_180:
                Core.rotate(image, image, Core.ROTATE_180);
                break;
            case MIRROR_WITHOUT_ROTATION:
                flipXCoordinates(image);
                break;
            case ROTATE_180_MIRROR:
                Core.rotate(image, image, Core.ROTATE_180);
                flipXCoordinates(image);
                break;
            case ROTATE_90_ANTICLOCKWISE_MIRROR:
                Core.rotate(image, image, Core.ROTATE_90_CLOCKWISE);
                flipXCoordinates(image);
                break;
            case ROTATE_90_CLOCKWISE_MIRROR:
                Core.rotate(image, image, Core.ROTATE_90_COUNTERCLOCKWISE);
                flipXCoordinates(image);
                break;
            default:
                throw new ImageIOException(
                        "An unsupported orientation change exists, and cannot be applied to the image: "
                                + orientation);
        }
    }

    /** Flips the coordinates across the Y-axis, thus changing X-coordinates only. */
    private static void flipXCoordinates(Mat image) {
        Core.flip(image, image, 1);
    }

    /** Calculates the needed orientation change. */
    private static OrientationChange calculateOrientation(
            Optional<CalculateOrientationChange> calculate, Logger logger) throws ImageIOException {
        if (calculate.isPresent()) {
            return calculate.get().calculateOrientationChange(logger);
        } else {
            return OrientationChange.KEEP_UNCHANGED;
        }
    }
}
