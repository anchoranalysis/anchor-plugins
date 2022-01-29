package org.anchoranalysis.plugin.image.bean.channel.aggregator;

import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.image.voxel.buffer.ProjectableBuffer;
import org.anchoranalysis.image.voxel.buffer.primitive.UnsignedByteBuffer;
import org.anchoranalysis.image.voxel.datatype.VoxelDataType;
import org.anchoranalysis.image.voxel.projection.StandardDeviationIntensityProjection;
import org.anchoranalysis.spatial.box.Extent;

/**
 * Projection of the standard-deviation of voxels values across all inputs.
 *
 * <p>This occurs similarly to {@link MeanProjection} but calculates the standard-deviation rather
 * than the mean for each voxel.
 *
 * @author Owen Feehan
 * @param <T> buffer type used for aggregation {@link UnsignedByteBuffer} etc.
 */
public class StandardDeviationProjection<T> extends ProjectableBufferAggregator<T> {

    @Override
    protected ProjectableBuffer<T> create(VoxelDataType dataType, Extent extent)
            throws OperationFailedException {
        return StandardDeviationIntensityProjection.create(dataType, extent);
    }
}
