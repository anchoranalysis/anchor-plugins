package org.anchoranalysis.plugin.image.bean.channel.aggregator;

import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.image.core.channel.Channel;
import org.anchoranalysis.image.voxel.buffer.ProjectableBuffer;
import org.anchoranalysis.image.voxel.buffer.primitive.UnsignedByteBuffer;
import org.anchoranalysis.image.voxel.datatype.VoxelDataType;
import org.anchoranalysis.image.voxel.projection.extrema.MaxIntensityProjection;
import org.anchoranalysis.spatial.box.Extent;

/**
 * <a
 * href="https://en.wikipedia.org/wiki/Maximum_intensity_projection">Maximum-intensity-projection</a>
 * across {@link Channel}s.
 *
 * @author Owen Feehan
 * @param <T> buffer type used for aggregation {@link UnsignedByteBuffer} etc.
 */
public class MaxProjection<T> extends ProjectableBufferAggregator<T> {

    @Override
    protected ProjectableBuffer<T> create(VoxelDataType dataType, Extent extent)
            throws OperationFailedException {
        return MaxIntensityProjection.create(dataType, extent);
    }
}
