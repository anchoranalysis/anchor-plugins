/* (C)2020 */
package ch.ethz.biol.cell.imageprocessing.binaryimgchnl.provider;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.binary.mask.Mask;

// If a param is equal to a particular value, do soemthing
public class BinaryChnlProviderIfStackExists extends BinaryChnlProviderElseBase {

    // START BEAN PROPERTIES
    @BeanField private String stackID = "";
    // END BEAN PROPERTIES

    @Override
    protected boolean condition(Mask chnl) throws CreateException {
        return getInitializationParameters().getChnlCollection().keys().contains(stackID);
    }

    public String getStackID() {
        return stackID;
    }

    public void setStackID(String stackID) {
        this.stackID = stackID;
    }
}
