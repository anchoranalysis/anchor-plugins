package org.anchoranalysis.plugin.image.task.bean.scale;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.image.core.channel.Channel;
import org.anchoranalysis.image.core.mask.Mask;
import org.anchoranalysis.image.voxel.resizer.VoxelsResizer;
import org.anchoranalysis.spatial.scale.ScaleFactor;

/** Applies a scale-factor to a channel. */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
class ScaleChannelHelper {

    /**
     * Produce a scaled version of a {@link Channel}.
     *
     * @param channel the channel before scaling.
     * @param binary true iff the channel has binary-contents (only two possible values are
     *     allowed), and must be interpolated differently.
     * @param scaleFactor the factor used for scaling.
     * @param voxelsResizer how to resize the voxels in a channel.
     * @return the scaled version of a channel.
     * @throws OperationFailedException
     */
    public static Channel scaleChannel(
            Channel channel, boolean binary, ScaleFactor scaleFactor, VoxelsResizer voxelsResizer) {

        if (scaleFactor.isNoScale()) {
            // Nothing to do
            return channel;
        }

        if (binary) {
            Mask mask = new Mask(channel);
            return mask.scaleXY(scaleFactor).channel();
        } else {
            return channel.scaleXY(scaleFactor, voxelsResizer);
        }
    }
}
