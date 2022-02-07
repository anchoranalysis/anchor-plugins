package org.anchoranalysis.plugin.image.task.bean.combine;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.anchoranalysis.core.exception.CreateException;
import org.anchoranalysis.core.exception.friendly.AnchorImpossibleSituationException;
import org.anchoranalysis.image.core.channel.Channel;
import org.anchoranalysis.image.core.channel.factory.ChannelFactory;
import org.anchoranalysis.image.core.dimensions.Dimensions;
import org.anchoranalysis.image.core.dimensions.IncorrectImageSizeException;
import org.anchoranalysis.image.core.stack.Stack;
import org.anchoranalysis.image.voxel.datatype.VoxelDataType;

/**
 * Creates a {@link Stack} to be used in {@link GroupedStackTestBase}.
 *
 * @author Owen Feehan
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
class CreateVoxelsHelper {

    /**
     * Creates a three-channeled {@link Stack} with the same channel-names as the existing inputs.
     *
     * @param voxelDataType the data-type of voxels that will be created.
     * @param rgb when true, the stack should be RGB, otherwise not.
     * @return a newly created {@link Stack} containing channels of a particular data-type.
     */
    public static Stack createStack(VoxelDataType voxelDataType, boolean rgb) {
        try {
            return new Stack(
                    rgb,
                    createChannel(voxelDataType),
                    createChannel(voxelDataType),
                    createChannel(voxelDataType));
        } catch (CreateException | IncorrectImageSizeException e) {
            throw new AnchorImpossibleSituationException();
        }
    }

    /**
     * Creates a {@link Channel} with a particular data-type.
     *
     * @param voxelDataType the data-type of voxels that will be created.
     * @return a newly created {@link Channel} containing channels of a particular data-type.
     */
    public static Channel createChannel(VoxelDataType voxelDataType) {
        return ChannelFactory.instance().create(new Dimensions(5, 6, 1), voxelDataType);
    }
}
