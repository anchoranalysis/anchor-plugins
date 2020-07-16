/* (C)2020 */
package ch.ethz.biol.cell.sgmn.binary;

import java.nio.ByteBuffer;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.Positive;
import org.anchoranalysis.image.bean.nonbean.error.SegmentationFailedException;
import org.anchoranalysis.image.bean.nonbean.parameters.BinarySegmentationParameters;
import org.anchoranalysis.image.bean.segment.binary.BinarySegmentation;
import org.anchoranalysis.image.bean.segment.binary.BinarySegmentationOne;
import org.anchoranalysis.image.binary.voxel.BinaryVoxelBox;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.image.voxel.box.VoxelBoxWrapper;

public class SgmnRepeat extends BinarySegmentationOne {

    // START BEAN PROPERTIES
    @BeanField @Positive @Getter @Setter private int maxIter = 10;
    // END BEAN PROPERTIES

    @Override
    public BinaryVoxelBox<ByteBuffer> sgmnFromSgmn(
            VoxelBoxWrapper voxelBox,
            BinarySegmentationParameters params,
            Optional<ObjectMask> mask,
            BinarySegmentation sgmn)
            throws SegmentationFailedException {

        BinaryVoxelBox<ByteBuffer> outOld = null;

        int cnt = 0;
        while (cnt++ < maxIter) {
            BinaryVoxelBox<ByteBuffer> outNew = sgmn.sgmn(voxelBox, params, mask);

            if (outNew == null) {
                return outOld;
            }

            outOld = outNew;

            // Increasingly tightens the obj-mask used for the segmentation
            mask =
                    Optional.of(
                            mask.isPresent()
                                    ? new ObjectMask(mask.get().getBoundingBox(), outNew)
                                    : new ObjectMask(outNew));
        }

        return outOld;
    }
}
