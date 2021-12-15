package org.anchoranalysis.plugin.opencv.bean.interpolate;

import org.anchoranalysis.image.bean.interpolator.Interpolator;
import org.anchoranalysis.image.voxel.resizer.VoxelsResizer;
import org.anchoranalysis.plugin.opencv.resizer.VoxelsResizerOpenCV;

/**
 * Interpolation using OpenCV for resizing an image.
 *
 * @see VoxelsResizerOpenCV
 * @author Owen Feehan
 */
public class OpenCV extends Interpolator {

    @Override
    protected VoxelsResizer createVoxelsResizer() {
        return new VoxelsResizerOpenCV();
    }
}
