/* (C)2020 */
package ch.ethz.biol.cell.imageprocessing.binaryimgchnl.provider;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.core.name.provider.NamedProviderGetException;
import org.anchoranalysis.image.bean.nonbean.init.ImageInitParams;
import org.anchoranalysis.image.bean.provider.BinaryChnlProvider;
import org.anchoranalysis.image.binary.mask.Mask;
import org.anchoranalysis.image.binary.values.BinaryValues;
import org.anchoranalysis.image.channel.Channel;

public class BinaryChnlProviderStackReference extends BinaryChnlProvider {

    // START BEAN PROPERTIES
    @BeanField private int chnlIndex = 0;

    @BeanField private String stackProviderID;
    // END BEAN PROPERTIES

    private Channel chnl;

    @Override
    public void onInit(ImageInitParams so) throws InitException {
        super.onInit(so);
        try {
            chnl = so.getStackCollection().getException(stackProviderID).getChnl(chnlIndex);
        } catch (NamedProviderGetException e) {
            throw InitException.createOrReuse(e.summarize());
        }
    }

    @Override
    public Mask create() throws CreateException {
        return new Mask(chnl, BinaryValues.getDefault());
    }

    public int getChnlIndex() {
        return chnlIndex;
    }

    public void setChnlIndex(int chnlIndex) {
        this.chnlIndex = chnlIndex;
    }

    public String getStackProviderID() {
        return stackProviderID;
    }

    public void setStackProviderID(String stackProviderID) {
        this.stackProviderID = stackProviderID;
    }
}
