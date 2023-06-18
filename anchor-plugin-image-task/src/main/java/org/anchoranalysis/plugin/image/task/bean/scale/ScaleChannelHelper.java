/*-
 * #%L
 * anchor-plugin-image-task
 * %%
 * Copyright (C) 2010 - 2023 Owen Feehan, ETH Zurich, University of Zurich, Hoffmann-La Roche
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */
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
