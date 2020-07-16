/* (C)2020 */
package ch.ethz.biol.cell.sgmn.binary;

import java.nio.ByteBuffer;
import java.util.Optional;
import org.anchoranalysis.anchor.mpp.bean.bound.MarkBounds;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.image.bean.nonbean.error.SegmentationFailedException;
import org.anchoranalysis.image.bean.nonbean.parameters.BinarySegmentationParameters;
import org.anchoranalysis.image.bean.segment.binary.BinarySegmentation;
import org.anchoranalysis.image.bean.segment.binary.BinarySegmentationThreshold;
import org.anchoranalysis.image.bean.threshold.CalculateLevel;
import org.anchoranalysis.image.bean.threshold.ThresholderGlobal;
import org.anchoranalysis.image.binary.voxel.BinaryVoxelBox;
import org.anchoranalysis.image.extent.Extent;
import org.anchoranalysis.image.extent.ImageResolution;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.image.voxel.box.VoxelBoxWrapper;
import org.anchoranalysis.plugin.image.bean.histogram.threshold.Constant;

// Performs a thresholding that accepts only channel values with intensities
//   greater than the minimum bound
public class SgmnThrshldAboveMinBound extends BinarySegmentation {

    // START BEAN PROPERTIES
    @BeanField private boolean suppress3D = false;

    @BeanField private MarkBounds markBounds;
    // END BEAN PROPERTIES

    private BinarySegmentationThreshold delegate = new BinarySegmentationThreshold();

    @Override
    public BinaryVoxelBox<ByteBuffer> sgmn(
            VoxelBoxWrapper voxelBox,
            BinarySegmentationParameters params,
            Optional<ObjectMask> mask)
            throws SegmentationFailedException {

        setUpDelegate(
                voxelBox.any().extent(),
                params.getRes()
                        .orElseThrow(
                                () ->
                                        new SegmentationFailedException(
                                                "Image-resolution is required but missing")));

        return delegate.sgmn(voxelBox, params, mask);
    }

    private void setUpDelegate(Extent e, ImageResolution res) {
        double minBound = markBounds.getMinResolved(res, e.getZ() > 1 && !suppress3D);

        int threshold = (int) Math.floor(minBound);

        CalculateLevel calculateLevel = new Constant(threshold);

        ThresholderGlobal thresholder = new ThresholderGlobal();
        thresholder.setCalculateLevel(calculateLevel);

        delegate.setThresholder(thresholder);
    }

    public boolean isSuppress3D() {
        return suppress3D;
    }

    public void setSuppress3D(boolean suppress3d) {
        suppress3D = suppress3d;
    }

    public MarkBounds getMarkBounds() {
        return markBounds;
    }

    public void setMarkBounds(MarkBounds markBounds) {
        this.markBounds = markBounds;
    }
}
