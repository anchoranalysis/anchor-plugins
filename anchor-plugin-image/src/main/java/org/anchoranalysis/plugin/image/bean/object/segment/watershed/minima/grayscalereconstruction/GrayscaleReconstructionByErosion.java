/* (C)2020 */
package org.anchoranalysis.plugin.image.bean.object.segment.watershed.minima.grayscalereconstruction;

import java.util.Optional;
import org.anchoranalysis.bean.AnchorBean;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.image.voxel.box.VoxelBoxWrapper;

public abstract class GrayscaleReconstructionByErosion
        extends AnchorBean<GrayscaleReconstructionByErosion> {

    // Reconstruction of maskImg from markerImg    mask<=markerImg  (but only inside containingMask)
    public abstract VoxelBoxWrapper reconstruction(
            VoxelBoxWrapper mask, VoxelBoxWrapper marker, Optional<ObjectMask> containingMask)
            throws OperationFailedException;
}
