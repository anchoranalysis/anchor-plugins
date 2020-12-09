package org.anchoranalysis.plugin.image.bean.scale;

import java.util.Optional;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.image.bean.spatial.ScaleCalculator;
import org.anchoranalysis.image.core.dimensions.Dimensions;
import org.anchoranalysis.image.core.dimensions.resize.suggestion.ImageResizeSuggestion;
import org.anchoranalysis.spatial.scale.ScaleFactor;
import lombok.Getter;
import lombok.Setter;

/**
 * Scales to the suggested-size if one is provided, otherwise calls {@code fallback}.
 * @author Owen Feehan
 *
 */
public class ToSuggested extends ScaleCalculator {

    // START BEAN PROPERTIES
    /** Used as a delegate to calculate the scale if {@code suggestedResize} is empty when passed to {@link #calculate}. */
    @BeanField @Getter @Setter private ScaleCalculator fallback;
    // END BEAN PROPERTIES
    
    @Override
    public ScaleFactor calculate(Optional<Dimensions> dimensionsToBeScaled,
            Optional<ImageResizeSuggestion> suggestedResize) throws OperationFailedException {
        if (suggestedResize.isPresent()) {
            return suggestedResize.get().calculateScaleFactor(dimensionsToBeScaled);
        } else {
            return fallback.calculate(dimensionsToBeScaled, suggestedResize);
        }
    }
}
