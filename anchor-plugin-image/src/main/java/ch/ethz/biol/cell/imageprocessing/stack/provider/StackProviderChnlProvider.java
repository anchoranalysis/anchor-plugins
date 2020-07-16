/* (C)2020 */
package ch.ethz.biol.cell.imageprocessing.stack.provider;

import org.anchoranalysis.bean.BeanInstanceMap;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.bean.error.BeanMisconfiguredException;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.bean.provider.BinaryChnlProvider;
import org.anchoranalysis.image.bean.provider.ChnlProvider;
import org.anchoranalysis.image.bean.provider.stack.StackProvider;
import org.anchoranalysis.image.stack.Stack;

public class StackProviderChnlProvider extends StackProvider {

    // START BEAN PROPERTIES
    @BeanField @OptionalBean private ChnlProvider chnl;

    @BeanField @OptionalBean private BinaryChnlProvider binaryChnl;
    // END BEAN PROPERTIES

    public StackProviderChnlProvider() {}

    public StackProviderChnlProvider(ChnlProvider chnlProvider) {
        this.chnl = chnlProvider;
    }

    @Override
    public void checkMisconfigured(BeanInstanceMap defaultInstances)
            throws BeanMisconfiguredException {
        super.checkMisconfigured(defaultInstances);

        if (!(chnl != null ^ binaryChnl != null)) {
            throw new BeanMisconfiguredException(
                    String.format(
                            "Either '%s' or '%s' must be non-null",
                            "chnlProvider", "binaryImgChnlProvider"));
        }
    }

    @Override
    public Stack create() throws CreateException {

        if (chnl != null) {
            return new Stack(chnl.create());
        } else {
            return new Stack(binaryChnl.create().getChannel());
        }
    }

    public ChnlProvider getChnl() {
        return chnl;
    }

    public void setChnl(ChnlProvider chnl) {
        this.chnl = chnl;
    }

    public BinaryChnlProvider getBinaryChnl() {
        return binaryChnl;
    }

    public void setBinaryChnl(BinaryChnlProvider binaryChnl) {
        this.binaryChnl = binaryChnl;
    }
}
