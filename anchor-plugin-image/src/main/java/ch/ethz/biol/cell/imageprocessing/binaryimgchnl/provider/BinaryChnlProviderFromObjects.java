/* (C)2020 */
package ch.ethz.biol.cell.imageprocessing.binaryimgchnl.provider;

import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.bean.provider.ObjectCollectionProvider;
import org.anchoranalysis.image.binary.mask.Mask;
import org.anchoranalysis.image.binary.values.BinaryValues;
import org.anchoranalysis.image.extent.ImageDimensions;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.image.object.ops.BinaryChnlFromObjects;

/** Creates a BinaryImgChannel from a collection of object masks */
public class BinaryChnlProviderFromObjects extends BinaryChnlProviderDimSource {

    // START BEAN
    @BeanField @Getter @Setter private ObjectCollectionProvider objects;

    @BeanField @Getter @Setter private boolean invert = false;
    // END BEAN

    @Override
    protected Mask createFromSource(ImageDimensions dimSource) throws CreateException {
        return create(objects.create(), dimSource, invert);
    }

    private static Mask create(
            ObjectCollection objects, ImageDimensions dimensions, boolean invert) {

        BinaryValues bv = BinaryValues.getDefault();

        Mask maskedImage = createChannelFromObjectsMultiplex(objects, dimensions, bv, invert);
        return new Mask(maskedImage.getChannel(), bv);
    }

    private static Mask createChannelFromObjectsMultiplex(
            ObjectCollection objects, ImageDimensions sd, BinaryValues outVal, boolean invert) {
        if (invert) {
            return BinaryChnlFromObjects.createFromNotObjects(objects, sd, outVal);
        } else {
            return BinaryChnlFromObjects.createFromObjects(objects, sd, outVal);
        }
    }
}
