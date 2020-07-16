/* (C)2020 */
package org.anchoranalysis.plugin.image.task.bean.grouped.raster;

import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.channel.factory.ChannelFactory;
import org.anchoranalysis.image.extent.ImageDimensions;
import org.anchoranalysis.image.voxel.datatype.VoxelDataType;
import org.anchoranalysis.image.voxel.datatype.VoxelDataTypeUnsignedInt;

/**
 * A channel associated with a count. This is a useful structure for finding the mean of many
 * channels
 */
class AggregateChnl {

    // We create only when we have the first channel, so dimensions can then be determined
    private Channel raster = null;
    private int cnt = 0;

    public synchronized void addChnl(Channel chnl) throws OperationFailedException {

        createRasterIfNecessary(chnl.getDimensions());

        if (!chnl.getDimensions().equals(raster.getDimensions())) {
            throw new OperationFailedException(
                    String.format(
                            "Dimensions of added-chnl (%s) and aggregated-chnl must be equal (%s)",
                            chnl.getDimensions(), raster.getDimensions()));
        }

        VoxelBoxArithmetic.add(
                raster.getVoxelBox().asInt(), chnl.getVoxelBox(), chnl.getVoxelDataType());

        cnt++;
    }

    /**
     * Create a channel with the mean-value of all the aggregated channels
     *
     * @return the channel with newly created voxel-box
     * @throws OperationFailedException
     */
    public Channel createMeanChnl(VoxelDataType outputType) throws OperationFailedException {

        if (cnt == 0) {
            throw new OperationFailedException(
                    "No channels have been added, so cannot create mean");
        }

        Channel chnlOut =
                ChannelFactory.instance()
                        .createEmptyInitialised(raster.getDimensions(), outputType);

        VoxelBoxArithmetic.divide(
                raster.getVoxelBox().asInt(), cnt, chnlOut.getVoxelBox(), outputType);

        return chnlOut;
    }

    private void createRasterIfNecessary(ImageDimensions dim) {
        if (raster == null) {
            this.raster =
                    ChannelFactory.instance()
                            .createEmptyInitialised(dim, VoxelDataTypeUnsignedInt.INSTANCE);
        }
    }

    public int getCnt() {
        return cnt;
    }
}
