package org.anchoranalysis.plugin.image.bean.scale;

import java.util.Optional;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.image.bean.spatial.ScaleCalculator;
import org.anchoranalysis.image.core.dimensions.Dimensions;
import org.anchoranalysis.spatial.scale.ScaleFactor;

/**
 * Scales to maximally fit-inside another {@link Dimensions} while preserving the XY aspect-ratio.
 * 
 * @author Owen Feehan
 *
 */
public class FitDimensionsPreserveAspectRatio extends ScaleCalculator {

    @Override
    public ScaleFactor calculate(Optional<Dimensions> dimensionsToBeScaled)
            throws OperationFailedException {
        // TODO Auto-generated method stub
        return null;
    }

}
