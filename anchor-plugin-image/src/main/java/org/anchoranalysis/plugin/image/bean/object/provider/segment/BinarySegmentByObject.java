/* (C)2020 */
package org.anchoranalysis.plugin.image.bean.object.provider.segment;

import java.nio.ByteBuffer;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.bean.nonbean.error.SegmentationFailedException;
import org.anchoranalysis.image.bean.nonbean.parameters.BinarySegmentationParameters;
import org.anchoranalysis.image.bean.segment.binary.BinarySegmentation;
import org.anchoranalysis.image.binary.voxel.BinaryVoxelBox;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.image.voxel.box.VoxelBox;
import org.anchoranalysis.image.voxel.box.VoxelBoxWrapper;
import org.anchoranalysis.plugin.image.bean.object.provider.ObjectCollectionProviderUnaryWithChannel;

/**
 * Performs a binary-segmentation using the upstream objects as masks.
 *
 * <p>Note that if there is more than one upstream object, multiple segmentations occur (one for
 * each mask) and are then combined.
 *
 * @author Owen Feehan
 */
public class BinarySegmentByObject extends ObjectCollectionProviderUnaryWithChannel {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private BinarySegmentation binarySgmn;
    // END BEAN PROPERTIES

    @Override
    protected ObjectCollection createFromObjects(
            ObjectCollection objectsSource, Channel channelSource) throws CreateException {
        try {
            return objectsSource.stream().map(object -> sgmnObject(object, channelSource));
        } catch (SegmentationFailedException e) {
            throw new CreateException(e);
        }
    }

    private ObjectMask sgmnObject(ObjectMask object, Channel channelSource)
            throws SegmentationFailedException {
        VoxelBox<?> vb = channelSource.getVoxelBox().any().region(object.getBoundingBox(), true);

        BinaryVoxelBox<ByteBuffer> bvb =
                binarySgmn.sgmn(
                        new VoxelBoxWrapper(vb),
                        new BinarySegmentationParameters(),
                        Optional.of(new ObjectMask(object.getVoxelBox())));

        return new ObjectMask(object.getBoundingBox(), bvb);
    }
}
