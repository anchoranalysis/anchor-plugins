package org.anchoranalysis.plugin.image.bean.channel.provider.gradient;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.channel.factory.ChannelFactory;
import org.anchoranalysis.image.voxel.datatype.FloatVoxelType;
import lombok.Getter;
import lombok.Setter;

public abstract class GradientBaseAddSum extends GradientBase {

    // START BEAN FIELDS
    /** Added to all gradients (so we can store negative gradients) */
    @BeanField @Getter @Setter private int addSum = 0;
    // END BEAN FIELDS
    
    @Override
    public Channel createFromChannel(Channel channelIn) throws CreateException {

        // The gradient is calculated on a float
        Channel channelIntermediate =
                ChannelFactory.instance().create(channelIn.dimensions(), FloatVoxelType.INSTANCE);

        GradientCalculator calculator =
                new GradientCalculator(createAxisArray(), (float) getScaleFactor(), addSum);
        calculator.gradient(channelIn.voxels(), channelIntermediate.voxels().asFloat());

        return convertToOutputType(channelIntermediate);
    }

    protected abstract boolean[] createAxisArray() throws CreateException;
}
