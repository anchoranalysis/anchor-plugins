/* (C)2020 */
package org.anchoranalysis.plugin.image.feature.bean.object.single.surface;

import org.anchoranalysis.feature.cache.calculation.FeatureCalculation;
import org.anchoranalysis.image.feature.object.input.FeatureInputSingleObject;

/**
 * The number of voxel-faces along the surface (the faces of each voxel that touch outside)
 *
 * @author Owen Feehan
 */
public class SurfaceNumberVoxelFaces extends SurfaceNumberVoxelsBase {

    @Override
    protected FeatureCalculation<Integer, FeatureInputSingleObject> createParams(
            boolean mip, boolean suppress3d) {
        return new CalculateOutlineNumberVoxelFaces(mip, suppress3d);
    }
}
