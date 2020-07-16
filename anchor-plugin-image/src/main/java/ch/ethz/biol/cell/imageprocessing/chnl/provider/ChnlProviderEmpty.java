/* (C)2020 */
package ch.ethz.biol.cell.imageprocessing.chnl.provider;

import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.bean.provider.ChnlProvider;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.channel.factory.ChannelFactory;
import org.anchoranalysis.image.extent.Extent;
import org.anchoranalysis.image.extent.ImageDimensions;
import org.anchoranalysis.image.extent.ImageResolution;
import org.anchoranalysis.image.voxel.datatype.VoxelDataTypeUnsignedByte;

/**
 * TODO change property names
 *
 * @author Owen Feehan
 */
public class ChnlProviderEmpty extends ChnlProvider {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private int sx = 1024;

    @BeanField @Getter @Setter private int sy = 768;

    @BeanField @Getter @Setter private int sz = 1;
    // END BEAN PROPERTIES

    @Override
    public Channel create() throws CreateException {
        ImageDimensions dimensions =
                new ImageDimensions(new Extent(sx, sy, sz), new ImageResolution());
        return ChannelFactory.instance()
                .createEmptyInitialised(dimensions, VoxelDataTypeUnsignedByte.INSTANCE);
    }
}
