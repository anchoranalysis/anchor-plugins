/* (C)2020 */
package ch.ethz.biol.cell.imageprocessing.chnl.provider;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.name.provider.NamedProviderGetException;
import org.anchoranalysis.image.bean.provider.ChnlProvider;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.stack.Stack;

public class ChnlProviderStackReference extends ChnlProvider {

    // START
    @BeanField private int chnlIndex = 0;

    @BeanField private String stackProviderID;
    // END

    private Channel chnl;

    @Override
    public Channel create() throws CreateException {

        try {
            if (chnl == null) {
                Stack stack =
                        getInitializationParameters()
                                .getStackCollection()
                                .getException(stackProviderID);

                chnl = stack.getChnl(chnlIndex);
                if (chnl == null) {
                    throw new CreateException(String.format("chnl %d cannot be found", chnlIndex));
                }
            }
        } catch (NamedProviderGetException e) {
            throw new CreateException(e);
        }

        return chnl;
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

    public Channel getChnl() {
        return chnl;
    }

    public void setChnl(Channel chnl) {
        this.chnl = chnl;
    }
}
