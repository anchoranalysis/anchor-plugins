/*-
 * #%L
 * anchor-plugin-image-task
 * %%
 * Copyright (C) 2010 - 2020 Owen Feehan, ETH Zurich, University of Zurich, Hoffmann-La Roche
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

package org.anchoranalysis.plugin.image.bean.channel.aggregator;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.image.bean.channel.ChannelAggregator;
import org.anchoranalysis.image.core.channel.Channel;
import org.anchoranalysis.image.core.channel.factory.ChannelFactory;
import org.anchoranalysis.image.core.dimensions.Dimensions;
import org.anchoranalysis.image.voxel.Voxels;
import org.anchoranalysis.image.voxel.buffer.ProjectableBuffer;
import org.anchoranalysis.image.voxel.buffer.primitive.UnsignedByteBuffer;
import org.anchoranalysis.image.voxel.datatype.VoxelDataType;
import org.anchoranalysis.spatial.box.Extent;

/**
 * An aggregator that calculates the <b>aggregation</b> of every voxel across successive channels
 * via a {@link ProjectableBuffer}.
 *
 * <p>This is achieved by maintaining a running sum for each voxel, and a total count of how many
 * images were added.
 *
 * @param <T> buffer type used for aggregation {@link UnsignedByteBuffer} etc.
 */
@Accessors(fluent = true)
@RequiredArgsConstructor
public abstract class ProjectableBufferAggregator<T> extends ChannelAggregator {

    /**
     * Maintains the projection across images.
     *
     * <p>Created lazily after first channel is added, as dimensions and type are only then known.
     */
    private ProjectableBuffer<T> projection;

    /** Also store the dimensions of the first channel, to compare with the subsequence channels. */
    private Dimensions dimensions;

    @Override
    protected Optional<Dimensions> existingDimensions() {
        return Optional.ofNullable(dimensions);
    }

    @SuppressWarnings("unchecked")
    protected void addChannelAfterCheck(Channel channel) throws OperationFailedException {
        createProjectionIfNeeded(channel.dimensions(), channel.getVoxelDataType());
        projection.addVoxels((Voxels<T>) channel.voxels().any());
    }

    @Override
    protected Channel retrieveCreateAggregatedChannel() {
        return ChannelFactory.instance().create(projection.completeProjection());
    }

    /**
     * Creates the {@link ProjectableBuffer} used for aggregation.
     *
     * @param dataType the data-type to use for the aggregated channel.
     * @param extent the size of the aggregated channel.
     * @return a newly created {@link ProjectableBuffer} of specified type and size.
     * @throws OperationFailedException if a buffer-type is unsupported.
     */
    protected abstract ProjectableBuffer<T> create(VoxelDataType dataType, Extent extent)
            throws OperationFailedException;

    private void createProjectionIfNeeded(Dimensions dimensions, VoxelDataType dataType)
            throws OperationFailedException {
        if (projection == null) {
            this.projection = create(dataType, dimensions.extent());
            this.dimensions = dimensions;
        }
    }
}
