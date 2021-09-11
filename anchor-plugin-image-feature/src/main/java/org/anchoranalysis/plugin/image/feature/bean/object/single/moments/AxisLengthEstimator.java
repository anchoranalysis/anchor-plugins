package org.anchoranalysis.plugin.image.feature.bean.object.single.moments;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Estimates axis-length from an eigen-value.
 * 
 * @author Owen Feehan
 *
 */
@NoArgsConstructor(access=AccessLevel.PRIVATE)
class AxisLengthEstimator {

    /**
     * A normalization of an eigenvalue to represent axis-length.
     *
     * <p>This normalization procedure is designed to return the same result as Matlab's
     * "MajorAxisLength" feature, as per <a
     * href="http://stackoverflow.com/questions/1711784/computing-object-statistics-from-the-second-central-moments">Stackoverflow
     * post</a>
     *
     * @return an estimate of the axis-length, derived from {@code eigenvalue}.
     */
    public static double fromNormalizedEigenvalue(double eigenvalue) {
        return (4 * Math.sqrt(eigenvalue));
    }
}
