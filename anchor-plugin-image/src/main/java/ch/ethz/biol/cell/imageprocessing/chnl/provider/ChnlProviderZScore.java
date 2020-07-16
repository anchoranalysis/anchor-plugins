/* (C)2020 */
package ch.ethz.biol.cell.imageprocessing.chnl.provider;

import java.nio.ByteBuffer;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.image.bean.provider.ChnlProviderOne;
import org.anchoranalysis.image.bean.provider.HistogramProvider;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.channel.factory.ChannelFactory;
import org.anchoranalysis.image.extent.Extent;
import org.anchoranalysis.image.histogram.Histogram;
import org.anchoranalysis.image.voxel.box.VoxelBox;
import org.anchoranalysis.image.voxel.buffer.VoxelBuffer;
import org.anchoranalysis.image.voxel.datatype.VoxelDataTypeUnsignedByte;

public class ChnlProviderZScore extends ChnlProviderOne {

    // START BEAN PROPERTIES
    @BeanField private HistogramProvider histogram;

    @BeanField private boolean alwaysDuplicate = false;

    @BeanField private double factor = 100.0; // Multiples
    // END BEAN PROPERTIES

    @Override
    public Channel createFromChnl(Channel chnl) throws CreateException {

        Histogram hist = histogram.create();

        VoxelBox<ByteBuffer> out = chnl.getVoxelBox().asByteOrCreateEmpty(alwaysDuplicate);

        try {
            transformBufferToZScore(hist.mean(), hist.stdDev(), chnl, out);
        } catch (OperationFailedException e) {
            throw new CreateException(
                    "An occurred calculating the mean or std-dev of a channel's histogram");
        }

        return ChannelFactory.instance().create(out, chnl.getDimensions().getRes());
    }

    private void transformBufferToZScore(
            double histMean, double histStdDev, Channel chnl, VoxelBox<ByteBuffer> out) {

        // We loop through each item
        Extent e = chnl.getDimensions().getExtent();

        int volumeXY = e.getVolumeXY();

        for (int z = 0; z < e.getZ(); z++) {

            VoxelBuffer<?> vbIn = chnl.getVoxelBox().any().getPixelsForPlane(z);
            VoxelBuffer<?> vbOut = out.getPixelsForPlane(z);

            for (int offset = 0; offset < volumeXY; offset++) {

                int val = vbIn.getInt(offset);

                double zScoreDbl = (((double) val) - histMean) / histStdDev;

                int valOut = (int) (zScoreDbl * factor);

                // We ignore negative zScore
                if (valOut < 0) {
                    valOut = 0;
                }

                if (valOut > VoxelDataTypeUnsignedByte.MAX_VALUE_INT) {
                    valOut = VoxelDataTypeUnsignedByte.MAX_VALUE_INT;
                }

                vbOut.putInt(offset, valOut);
            }
        }
    }

    public double getFactor() {
        return factor;
    }

    public void setFactor(double factor) {
        this.factor = factor;
    }

    public boolean isAlwaysDuplicate() {
        return alwaysDuplicate;
    }

    public void setAlwaysDuplicate(boolean alwaysDuplicate) {
        this.alwaysDuplicate = alwaysDuplicate;
    }

    public HistogramProvider getHistogram() {
        return histogram;
    }

    public void setHistogram(HistogramProvider histogram) {
        this.histogram = histogram;
    }
}
