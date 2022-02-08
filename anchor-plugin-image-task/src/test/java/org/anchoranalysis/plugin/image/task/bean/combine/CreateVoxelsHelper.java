/*-
 * #%L
 * anchor-plugin-image-task
 * %%
 * Copyright (C) 2010 - 2022 Owen Feehan, ETH Zurich, University of Zurich, Hoffmann-La Roche
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
