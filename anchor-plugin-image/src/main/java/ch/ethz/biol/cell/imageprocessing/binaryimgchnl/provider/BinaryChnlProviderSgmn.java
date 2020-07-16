/* (C)2020 */
package ch.ethz.biol.cell.imageprocessing.binaryimgchnl.provider;

import java.nio.ByteBuffer;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.OptionalFactory;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.bean.nonbean.error.SegmentationFailedException;
import org.anchoranalysis.image.bean.nonbean.parameters.BinarySegmentationParameters;
import org.anchoranalysis.image.bean.provider.BinaryChnlProvider;
import org.anchoranalysis.image.bean.provider.HistogramProvider;
import org.anchoranalysis.image.bean.segment.binary.BinarySegmentation;
import org.anchoranalysis.image.binary.mask.Mask;
import org.anchoranalysis.image.binary.voxel.BinaryVoxelBox;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.channel.factory.ChannelFactory;
import org.anchoranalysis.image.extent.ImageDimensions;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.image.voxel.datatype.VoxelDataTypeUnsignedByte;

public class BinaryChnlProviderSgmn extends BinaryChnlProviderChnlSource {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private BinarySegmentation sgmn;

    @BeanField @OptionalBean @Getter @Setter private HistogramProvider histogramProvider;

    @BeanField @OptionalBean @Getter @Setter private BinaryChnlProvider mask;
    // END BEAN PROPERTIES

    @Override
    protected Mask createFromSource(Channel chnlSource) throws CreateException {
        return new Mask(
                sgmnResult(chnlSource),
                chnlSource.getDimensions().getRes(),
                ChannelFactory.instance().get(VoxelDataTypeUnsignedByte.INSTANCE));
    }

    private BinaryVoxelBox<ByteBuffer> sgmnResult(Channel chnl) throws CreateException {
        Optional<ObjectMask> omMask = mask(chnl.getDimensions());

        BinarySegmentationParameters params = createParams(chnl.getDimensions());

        try {
            return sgmn.sgmn(chnl.getVoxelBox(), params, omMask);

        } catch (SegmentationFailedException e) {
            throw new CreateException(e);
        }
    }

    private BinarySegmentationParameters createParams(ImageDimensions dim) throws CreateException {
        return new BinarySegmentationParameters(
                dim.getRes(), OptionalFactory.create(histogramProvider));
    }

    private Optional<ObjectMask> mask(ImageDimensions dim) throws CreateException {
        Optional<Mask> maskChnl =
                ChnlProviderNullableCreator.createOptionalCheckSize(mask, "mask", dim);
        return maskChnl.map(chnl -> new ObjectMask(chnl.binaryVoxelBox()));
    }
}
