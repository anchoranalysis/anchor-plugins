/* (C)2020 */
package org.anchoranalysis.plugin.image.task.grouped;

import org.anchoranalysis.core.index.SetOperationFailedException;
import org.anchoranalysis.image.voxel.datatype.VoxelDataType;
import org.anchoranalysis.image.voxel.datatype.VoxelDataTypeUnsignedShort;

/**
 * Checks that the histograms created from channels all have the same data type, res, max-value etc.
 *
 * @author Owen Feehan
 */
public class ConsistentChannelChecker {

    private long maxValue = 0; // Unset
    private VoxelDataType chnlType;

    /** Checks that a channel has the same type (max value) as the others */
    public void checkChannelType(VoxelDataType chnlType) throws SetOperationFailedException {
        long maxValueChnl = chnlType.maxValue();
        if (!isMaxValueSet()) {
            setMaxValue(maxValueChnl);
            this.chnlType = chnlType;
        } else {
            if (getMaxValue() != maxValueChnl) {
                throw new SetOperationFailedException(
                        "All images must have data-types of the same histogram size");
            } else if (!getChnlType().equals(chnlType)) {
                throw new SetOperationFailedException("All images must have the same data type");
            }
        }
    }

    private boolean isMaxValueSet() {
        return maxValue != 0;
    }

    public long getMaxValue() {
        return maxValue;
    }

    private void setMaxValue(long histogramMaxValue) throws SetOperationFailedException {

        if (histogramMaxValue > VoxelDataTypeUnsignedShort.MAX_VALUE) {
            throw new SetOperationFailedException(
                    String.format(
                            "Histogram max-value (%d) must be set less than %d",
                            histogramMaxValue, VoxelDataTypeUnsignedShort.MAX_VALUE));
        }

        this.maxValue = histogramMaxValue;
    }

    public VoxelDataType getChnlType() {
        return chnlType;
    }
}
