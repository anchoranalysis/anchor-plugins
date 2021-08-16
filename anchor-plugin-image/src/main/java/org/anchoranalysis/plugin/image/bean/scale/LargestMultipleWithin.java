package org.anchoranalysis.plugin.image.bean.scale;

import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.image.bean.spatial.ScaleCalculator;
import org.anchoranalysis.image.bean.spatial.SizeXY;
import org.anchoranalysis.image.core.dimensions.Dimensions;
import org.anchoranalysis.image.core.dimensions.size.ResizeExtentUtilities;
import org.anchoranalysis.image.core.dimensions.size.suggestion.ImageSizeSuggestion;
import org.anchoranalysis.spatial.Extent;
import org.anchoranalysis.spatial.scale.ScaleFactor;

/**
 * Finds largest multiple of an {@link Extent} without being larger than another {@link Extent}.
 *
 * <p>Each dimension is calculated separately, and then the minimum scaling-factor is used for both.
 *
 * <p>e.g. for the X dimension in {@code minimumSize}, the maximum <code>multiple</code> is selected
 * so that <code>(unscaledSizeX * 2^multiple)</code> is less than <code>maxSixeX</code>.
 *
 * @author Owen Feehan
 */
public class LargestMultipleWithin extends ScaleCalculator {

    // START BEAN PROPERTIES
    /**
     * The minimum size in each dimension. Integer multiples are considered separately in each
     * dimension.
     */
    private @BeanField @Getter @Setter SizeXY minimumSize;

    /** An upper limit on the scale-factor allowed in each dimension. */
    private @BeanField @Getter @Setter int maxScaleFactor;
    // END BEAN PROPERTIES

    @Override
    public ScaleFactor calculate(
            Optional<Dimensions> dimensionsToBeScaled,
            Optional<ImageSizeSuggestion> suggestedResize)
            throws OperationFailedException {

        if (!dimensionsToBeScaled.isPresent()) {
            throw new OperationFailedException(
                    "dimensionsToBeScaled is required by the plugin but is missing.");
        }
        Extent originalSize = dimensionsToBeScaled.get().extent();
        Extent sizeLargestMultiple =
                FindLargestMultipleWithin.apply(
                        minimumSize.asExtent(), originalSize, maxScaleFactor);
        return ResizeExtentUtilities.relativeScale(originalSize, sizeLargestMultiple);
    }
}
