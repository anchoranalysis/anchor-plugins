/* (C)2020 */
package ch.ethz.biol.cell.imageprocessing.stack.provider;

import ch.ethz.biol.cell.imageprocessing.chnl.provider.ChnlProviderMax;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.stack.region.chnlconverter.ChannelConverterMulti;
import org.anchoranalysis.image.voxel.datatype.VoxelDataType;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
class MaxChnls {

    public static Channel apply(Channel chnl1, Channel chnl2, VoxelDataType outputType)
            throws CreateException {
        Channel max = maxChnls(chnl1, chnl2);
        return convert(max, outputType);
    }

    private static Channel maxChnls(Channel chnl1, Channel chnl2) throws CreateException {
        if (chnl2 != null) {
            return ChnlProviderMax.createMax(chnl1, chnl2);
        } else {
            return chnl1;
        }
    }

    private static Channel convert(Channel chnl, VoxelDataType outputType) {
        ChannelConverterMulti chnlConverter = new ChannelConverterMulti();
        return chnlConverter.convert(chnl, outputType);
    }
}
