/* (C)2020 */
package ch.ethz.biol.cell.imageprocessing.chnl.provider;

import ch.ethz.biol.cell.mpp.nrg.feature.pixelwise.createvoxelbox.CreateVoxelBoxFromPixelwiseFeature;
import ch.ethz.biol.cell.mpp.nrg.feature.pixelwise.createvoxelbox.CreateVoxelBoxFromPixelwiseFeatureWithMask;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.bean.shared.params.keyvalue.KeyValueParamsProvider;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.params.KeyValueParams;
import org.anchoranalysis.image.bean.provider.BinaryChnlProvider;
import org.anchoranalysis.image.bean.provider.ChnlProvider;
import org.anchoranalysis.image.bean.provider.HistogramProvider;
import org.anchoranalysis.image.binary.mask.Mask;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.channel.factory.ChannelFactoryByte;
import org.anchoranalysis.image.extent.BoundingBox;
import org.anchoranalysis.image.feature.bean.pixelwise.PixelScore;
import org.anchoranalysis.image.histogram.Histogram;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.image.voxel.box.VoxelBox;
import org.anchoranalysis.image.voxel.box.VoxelBoxList;
import org.anchoranalysis.image.voxel.box.VoxelBoxWrapper;

public class ChnlProviderPixelScore extends ChnlProvider {

    // START BEAN PROPERTIES
    @BeanField private ChnlProvider intensityProvider;

    @BeanField @OptionalBean private ChnlProvider gradientProvider;

    // We don't use {@link ChnlProiderMask} as here it's optional.
    @BeanField @OptionalBean private BinaryChnlProvider mask;

    @BeanField private PixelScore pixelScore;

    @BeanField private List<ChnlProvider> listChnlProviderExtra = new ArrayList<>();

    @BeanField private List<HistogramProvider> listHistogramProviderExtra = new ArrayList<>();

    @BeanField @OptionalBean private KeyValueParamsProvider keyValueParamsProvider;
    // END BEAN PROPERTIES

    private VoxelBoxList createVoxelBoxList(Channel chnlIntensity) throws CreateException {

        VoxelBoxList listOut = new VoxelBoxList();

        listOut.add(chnlIntensity.getVoxelBox());

        if (gradientProvider != null) {
            listOut.add(gradientProvider.create().getVoxelBox());
        }
        for (ChnlProvider chnlProvider : listChnlProviderExtra) {
            VoxelBoxWrapper vbExtra =
                    chnlProvider != null ? chnlProvider.create().getVoxelBox() : null;
            listOut.add(vbExtra);
        }
        return listOut;
    }

    private Optional<ObjectMask> createMaskOrNull() throws CreateException {
        if (mask == null) {
            return Optional.empty();
        }

        Mask binaryChnlMask = mask.create();
        Channel chnlMask = binaryChnlMask.getChannel();

        return Optional.of(
                new ObjectMask(
                        new BoundingBox(chnlMask.getDimensions().getExtent()),
                        chnlMask.getVoxelBox().asByte(),
                        binaryChnlMask.getBinaryValues()));
    }

    @Override
    public Channel create() throws CreateException {

        Channel chnlIntensity = intensityProvider.create();

        VoxelBoxList listVb = createVoxelBoxList(chnlIntensity);
        List<Histogram> listHistExtra =
                ProviderBeanUtilities.listFromBeans(listHistogramProviderExtra);

        Optional<KeyValueParams> kpv;
        if (keyValueParamsProvider != null) {
            kpv = Optional.of(keyValueParamsProvider.create());
        } else {
            kpv = Optional.empty();
        }

        Optional<ObjectMask> object = createMaskOrNull();

        VoxelBox<ByteBuffer> vbPixelScore;
        if (object.isPresent()) {
            CreateVoxelBoxFromPixelwiseFeatureWithMask creator =
                    new CreateVoxelBoxFromPixelwiseFeatureWithMask(listVb, kpv, listHistExtra);

            vbPixelScore = creator.createVoxelBoxFromPixelScore(pixelScore, object);

        } else {
            CreateVoxelBoxFromPixelwiseFeature creator =
                    new CreateVoxelBoxFromPixelwiseFeature(listVb, kpv, listHistExtra);

            vbPixelScore = creator.createVoxelBoxFromPixelScore(pixelScore, getLogger());
        }

        return new ChannelFactoryByte()
                .create(vbPixelScore, chnlIntensity.getDimensions().getRes());
    }

    public ChnlProvider getIntensityProvider() {
        return intensityProvider;
    }

    public void setIntensityProvider(ChnlProvider intensityProvider) {
        this.intensityProvider = intensityProvider;
    }

    public ChnlProvider getGradientProvider() {
        return gradientProvider;
    }

    public void setGradientProvider(ChnlProvider gradientProvider) {
        this.gradientProvider = gradientProvider;
    }

    public PixelScore getPixelScore() {
        return pixelScore;
    }

    public void setPixelScore(PixelScore pixelScore) {
        this.pixelScore = pixelScore;
    }

    public List<ChnlProvider> getListChnlProviderExtra() {
        return listChnlProviderExtra;
    }

    public void setListChnlProviderExtra(List<ChnlProvider> listChnlProviderExtra) {
        this.listChnlProviderExtra = listChnlProviderExtra;
    }

    public List<HistogramProvider> getListHistogramProviderExtra() {
        return listHistogramProviderExtra;
    }

    public void setListHistogramProviderExtra(List<HistogramProvider> listHistogramProviderExtra) {
        this.listHistogramProviderExtra = listHistogramProviderExtra;
    }

    public KeyValueParamsProvider getKeyValueParamsProvider() {
        return keyValueParamsProvider;
    }

    public void setKeyValueParamsProvider(KeyValueParamsProvider keyValueParamsProvider) {
        this.keyValueParamsProvider = keyValueParamsProvider;
    }

    public BinaryChnlProvider getMask() {
        return mask;
    }

    public void setMask(BinaryChnlProvider mask) {
        this.mask = mask;
    }
}
