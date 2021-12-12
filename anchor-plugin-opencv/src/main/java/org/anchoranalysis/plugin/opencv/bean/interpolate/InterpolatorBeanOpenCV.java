package org.anchoranalysis.plugin.opencv.bean.interpolate;

import org.anchoranalysis.image.bean.interpolator.InterpolatorBean;
import org.anchoranalysis.image.voxel.interpolator.Interpolator;
import org.anchoranalysis.plugin.opencv.interpolator.InterpolatorOpenCV;

/**
 * Creates an {@link Interpolator} that uses OpenCV.
 *
 * @author Owen Feehan
 */
public class InterpolatorBeanOpenCV extends InterpolatorBean {

    @Override
    public Interpolator create() {
        return new InterpolatorOpenCV();
    }
}
