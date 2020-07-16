/* (C)2020 */
package ch.ethz.biol.cell.imageprocessing.binaryimgchnl.provider;

import ch.ethz.biol.cell.imageprocessing.dim.provider.GuessDimFromInputImage;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.bean.provider.BinaryChnlProvider;
import org.anchoranalysis.image.bean.provider.ImageDimProvider;
import org.anchoranalysis.image.binary.mask.Mask;
import org.anchoranalysis.image.extent.ImageDimensions;

public abstract class BinaryChnlProviderDimSource extends BinaryChnlProvider {

    // START BEAN PROPERTIES
    @BeanField private ImageDimProvider dim = new GuessDimFromInputImage();
    // END BEAN PROPERTIES

    @Override
    public Mask create() throws CreateException {
        return createFromSource(dim.create());
    }

    protected abstract Mask createFromSource(ImageDimensions dimSource) throws CreateException;

    public ImageDimProvider getDim() {
        return dim;
    }

    public void setDim(ImageDimProvider dim) {
        this.dim = dim;
    }
}
