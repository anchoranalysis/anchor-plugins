/* (C)2020 */
package org.anchoranalysis.plugin.image.bean.object.segment;

import java.nio.ByteBuffer;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.image.bean.nonbean.error.SegmentationFailedException;
import org.anchoranalysis.image.bean.nonbean.parameters.BinarySegmentationParameters;
import org.anchoranalysis.image.bean.segment.binary.BinarySegmentation;
import org.anchoranalysis.image.bean.segment.object.SegmentChannelIntoObjects;
import org.anchoranalysis.image.bean.segment.object.SegmentChannelIntoObjectsUnary;
import org.anchoranalysis.image.binary.voxel.BinaryVoxelBox;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.image.object.ops.ExtendObjectsInto3DMask;
import org.anchoranalysis.image.seed.SeedCollection;
import org.anchoranalysis.image.voxel.box.VoxelBox;
import org.anchoranalysis.image.voxel.box.VoxelBoxWrapper;

/**
 * Perform a segmentation in a MIP instead of z-stacks, and fits the result back into a 3D
 * segmentation.
 *
 * <p>The upstream segmentation should return 2D objects as it is executed on the maximum-intensity
 * projection.
 *
 * <p>A 3D binary-segmentation is applied to the z-stack with @code{segmentStack} to produce a mask
 * over the z-stack. The the 2D objects are then expanded in the z-dimension to fit this mask.
 *
 * @author Owen Feehan
 */
public class SegmentOnMaximumIntensityAndExpandInZ extends SegmentChannelIntoObjectsUnary {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private BinarySegmentation segmentStack;
    // END BEAN PROPERTIES

    @Override
    public ObjectCollection segment(
            Channel chnl,
            Optional<ObjectMask> mask,
            Optional<SeedCollection> seeds,
            SegmentChannelIntoObjects upstreamSegmentation)
            throws SegmentationFailedException {

        if (mask.isPresent()) {
            throw new SegmentationFailedException(
                    "An object-mask is not supported for this operation");
        }

        // Collapse seeds in z direction
        seeds.ifPresent(SegmentOnMaximumIntensityAndExpandInZ::flattenSeedsInZ);

        ObjectCollection objects =
                upstreamSegmentation.segment(
                        chnl.maxIntensityProjection(), Optional.empty(), seeds);

        if (isAny3d(objects)) {
            throw new SegmentationFailedException(
                    "A 3D object was returned from the initial segmentation. This must return only 2D objects");
        }

        return ExtendObjectsInto3DMask.extendObjects(objects, binarySgmn(chnl));
    }

    private boolean isAny3d(ObjectCollection objects) {
        return objects.stream()
                .anyMatch(objectMask -> objectMask.getVoxelBox().extent().getZ() > 1);
    }

    private BinaryVoxelBox<ByteBuffer> binarySgmn(Channel chnl) throws SegmentationFailedException {
        BinarySegmentationParameters params =
                new BinarySegmentationParameters(chnl.getDimensions().getRes());

        VoxelBox<ByteBuffer> vb = chnl.getVoxelBox().asByte();

        VoxelBox<ByteBuffer> stackBinary = vb.duplicate();
        return segmentStack.sgmn(new VoxelBoxWrapper(stackBinary), params, Optional.empty());
    }

    private static SeedCollection flattenSeedsInZ(SeedCollection seeds) {
        SeedCollection seedsDup = seeds.duplicate();
        seedsDup.flattenZ();
        return seedsDup;
    }
}
