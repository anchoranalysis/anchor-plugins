/* (C)2020 */
package ch.ethz.biol.cell.imageprocessing.chnl.provider;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.bean.provider.ChnlProvider;
import org.anchoranalysis.image.bean.provider.ChnlProviderOne;
import org.anchoranalysis.image.bean.provider.HistogramProvider;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.extent.Extent;
import org.anchoranalysis.image.extent.ImageDimensions;
import org.anchoranalysis.image.feature.bean.pixelwise.PixelScore;
import org.anchoranalysis.image.histogram.Histogram;
import org.anchoranalysis.image.voxel.box.VoxelBoxWrapper;
import org.anchoranalysis.image.voxel.buffer.VoxelBuffer;

public class ChnlProviderPixelScoreFeature extends ChnlProviderOne {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private PixelScore pixelScore;

    @BeanField @Getter @Setter
    private List<ChnlProvider> listAdditionalChnlProviders = new ArrayList<>();

    @BeanField @OptionalBean @Getter @Setter private HistogramProvider histogramProvider;
    // END BEAN PROPERTIES

    @Override
    public Channel createFromChnl(Channel chnl) throws CreateException {

        List<Channel> listAdditional = additionalChnls(chnl.getDimensions());

        try {
            pixelScore.init(histograms(), Optional.empty());
            calcScoresIntoVoxelBox(chnl.getVoxelBox(), listAdditional, pixelScore);

        } catch (FeatureCalcException | InitException e) {
            throw new CreateException(e);
        }

        return chnl;
    }

    private List<Histogram> histograms() throws CreateException {
        if (histogramProvider != null) {
            return Arrays.asList(histogramProvider.create());
        } else {
            return new ArrayList<>();
        }
    }

    private static void calcScoresIntoVoxelBox(
            VoxelBoxWrapper vb, List<Channel> listAdditional, PixelScore pixelScore)
            throws FeatureCalcException {

        ByteBuffer[] arrByteBuffer = new ByteBuffer[listAdditional.size()];

        Extent e = vb.any().extent();
        for (int z = 0; z < e.getZ(); z++) {

            VoxelBuffer<?> bb = vb.any().getPixelsForPlane(z);

            for (int i = 0; i < listAdditional.size(); i++) {
                Channel additional = listAdditional.get(i);
                arrByteBuffer[i] = additional.getVoxelBox().asByte().getPixelsForPlane(z).buffer();
            }

            int offset = 0;
            for (int y = 0; y < e.getY(); y++) {
                for (int x = 0; x < e.getX(); x++) {

                    double result = pixelScore.calc(createParams(bb, arrByteBuffer, offset));

                    int valOut = (int) Math.round(result);

                    if (valOut < 0) valOut = 0;
                    if (valOut > 255) valOut = 255;

                    bb.putInt(offset, valOut);

                    offset++;
                }
            }
        }
    }

    private static int[] createParams(
            VoxelBuffer<?> bufferPrimary, ByteBuffer[] buffersAdd, int offset) {
        int[] out = new int[1 + buffersAdd.length];

        out[0] = bufferPrimary.getInt(offset);

        int i = 1;
        for (ByteBuffer bbAdd : buffersAdd) {
            out[i++] = bbAdd.getInt(offset);
        }
        return out;
    }

    private List<Channel> additionalChnls(ImageDimensions dimensions) throws CreateException {
        List<Channel> listAdditional = new ArrayList<>();
        for (ChnlProvider cp : listAdditionalChnlProviders) {
            Channel chnlAdditional = cp.create();

            if (!chnlAdditional.getDimensions().equals(dimensions)) {
                throw new CreateException(
                        "Dimensions of additional channel are not equal to main channel");
            }

            listAdditional.add(chnlAdditional);
        }
        return listAdditional;
    }
}
