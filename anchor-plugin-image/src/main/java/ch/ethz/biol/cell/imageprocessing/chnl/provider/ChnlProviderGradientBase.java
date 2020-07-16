/* (C)2020 */
package ch.ethz.biol.cell.imageprocessing.chnl.provider;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.bean.provider.ChnlProviderOne;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.channel.factory.ChannelFactory;
import org.anchoranalysis.image.stack.region.chnlconverter.ChannelConverter;
import org.anchoranalysis.image.stack.region.chnlconverter.ChannelConverterToUnsignedByte;
import org.anchoranalysis.image.stack.region.chnlconverter.ChannelConverterToUnsignedShort;
import org.anchoranalysis.image.stack.region.chnlconverter.ConversionPolicy;
import org.anchoranalysis.image.voxel.datatype.VoxelDataTypeFloat;

public abstract class ChnlProviderGradientBase extends ChnlProviderOne {

    // START BEAN
    @BeanField private double scaleFactor = 1.0;

    /** Iff true, outputs a short channel, otherwise byte channel */
    @BeanField private boolean outputShort = false;

    /** Added to all gradients (so we can store negative gradients) */
    @BeanField private int addSum = 0;
    // END BEAN

    @Override
    public Channel createFromChnl(Channel chnlIn) throws CreateException {

        // The gradient is calculated on a float
        Channel chnlIntermediate =
                ChannelFactory.instance()
                        .createEmptyInitialised(
                                chnlIn.getDimensions(), VoxelDataTypeFloat.INSTANCE);

        GradientCalculator calculator =
                new GradientCalculator(createDimensionArr(), (float) scaleFactor, addSum);
        calculator.calculateGradient(
                chnlIn.getVoxelBox(), chnlIntermediate.getVoxelBox().asFloat());

        return convertToOutputType(chnlIntermediate);
    }

    protected abstract boolean[] createDimensionArr() throws CreateException;

    private Channel convertToOutputType(Channel chnlToConvert) {
        ChannelConverter<?> converter =
                outputShort
                        ? new ChannelConverterToUnsignedShort()
                        : new ChannelConverterToUnsignedByte();
        return converter.convert(chnlToConvert, ConversionPolicy.CHANGE_EXISTING_CHANNEL);
    }

    public double getScaleFactor() {
        return scaleFactor;
    }

    public void setScaleFactor(double scaleFactor) {
        this.scaleFactor = scaleFactor;
    }

    public boolean isOutputShort() {
        return outputShort;
    }

    public void setOutputShort(boolean outputShort) {
        this.outputShort = outputShort;
    }

    public int getAddSum() {
        return addSum;
    }

    public void setAddSum(int addSum) {
        this.addSum = addSum;
    }
}
