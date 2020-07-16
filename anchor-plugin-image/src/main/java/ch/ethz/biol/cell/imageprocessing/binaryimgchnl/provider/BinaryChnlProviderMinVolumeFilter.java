/* (C)2020 */
package ch.ethz.biol.cell.imageprocessing.binaryimgchnl.provider;

import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.bean.nonbean.error.UnitValueException;
import org.anchoranalysis.image.bean.provider.BinaryChnlProviderOne;
import org.anchoranalysis.image.bean.unitvalue.volume.UnitValueVolume;
import org.anchoranalysis.image.binary.mask.Mask;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.image.object.factory.CreateFromConnectedComponentsFactory;
import org.anchoranalysis.image.object.ops.BinaryChnlFromObjects;

public class BinaryChnlProviderMinVolumeFilter extends BinaryChnlProviderOne {

    // START BEAN FIELDS
    @BeanField @Getter @Setter private UnitValueVolume minVolume = null;

    @BeanField @Getter @Setter private boolean inverted = false;
    // END BEAN FIELDS

    @Override
    public Mask createFromChnl(Mask chnl) throws CreateException {
        return createMaskedImage(chnl);
    }

    private Mask createMaskedImage(Mask bi) throws CreateException {

        return BinaryChnlFromObjects.createFromObjects(
                connectedComponents(bi, inverted), bi.getDimensions(), bi.getBinaryValues());
    }

    private ObjectCollection connectedComponents(Mask bi, boolean inverted) throws CreateException {

        int resolveMinNum;
        try {
            resolveMinNum =
                    (int)
                            Math.floor(
                                    minVolume.resolveToVoxels(
                                            Optional.of(bi.getDimensions().getRes())));
        } catch (UnitValueException e) {
            throw new CreateException(e);
        }

        CreateFromConnectedComponentsFactory createObjects =
                new CreateFromConnectedComponentsFactory(resolveMinNum);
        if (inverted) {
            Mask biInverted =
                    new Mask(
                            bi.getChannel(),
                            bi.getBinaryValues()
                                    .createInverted()); // In case we've inverted the binary values
            return createObjects.createConnectedComponents(biInverted);
        } else {
            return createObjects.createConnectedComponents(bi);
        }
    }
}
