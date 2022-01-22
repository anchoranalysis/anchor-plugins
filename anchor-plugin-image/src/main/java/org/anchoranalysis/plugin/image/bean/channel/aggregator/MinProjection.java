package org.anchoranalysis.plugin.image.bean.channel.aggregator;

import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.image.core.channel.Channel;
import org.anchoranalysis.image.voxel.buffer.ProjectableBuffer;
import org.anchoranalysis.image.voxel.buffer.max.MinIntensityProjection;
import org.anchoranalysis.image.voxel.buffer.primitive.UnsignedByteBuffer;
import org.anchoranalysis.image.voxel.datatype.VoxelDataType;
import org.anchoranalysis.spatial.box.Extent;

/**
 * Minimum-intensity-projection across {@link Channel}s.
 *
 * @author Owen Feehan
 * @param <T> buffer type used for aggregation {@link UnsignedByteBuffer} etc.
 */
public class MinProjection<T> extends ProjectableBufferAggregator<T> {

    @Override
    protected ProjectableBuffer<T> create(VoxelDataType dataType, Extent extent)
            throws OperationFailedException {
        return MinIntensityProjection.create(dataType, extent);
    }
}
