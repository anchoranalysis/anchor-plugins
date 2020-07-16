/* (C)2020 */
package org.anchoranalysis.plugin.image.feature.bean.object.single.surface;

import org.anchoranalysis.feature.cache.calculation.FeatureCalculation;
import org.anchoranalysis.image.feature.object.input.FeatureInputSingleObject;

/**
 * The number of voxels on the surface (all voxels on the exterior of the object)
 *
 * @author Owen Feehan
 */
public class SurfaceNumberVoxels extends SurfaceNumberVoxelsBase {

    @Override
    protected FeatureCalculation<Integer, FeatureInputSingleObject> createParams(
            boolean mip, boolean suppress3d) {
        return new CalculateOutlineNumberVoxels(mip, suppress3d);
    }
}
