/* (C)2020 */
package ch.ethz.biol.cell.imageprocessing.io.chnlconverter.bean;

import org.anchoranalysis.image.bean.chnl.converter.ConvertChannelTo;
import org.anchoranalysis.image.stack.region.chnlconverter.ChannelConverter;
import org.anchoranalysis.image.stack.region.chnlconverter.ChannelConverterToUnsignedByte;
import org.anchoranalysis.image.stack.region.chnlconverter.voxelbox.VoxelBoxConverterToByteScaleByType;

public class ChnlConverterBeanToByteScaleByType extends ConvertChannelTo {

    @Override
    public ChannelConverter<?> createConverter() {
        return new ChannelConverterToUnsignedByte(new VoxelBoxConverterToByteScaleByType());
    }
}
