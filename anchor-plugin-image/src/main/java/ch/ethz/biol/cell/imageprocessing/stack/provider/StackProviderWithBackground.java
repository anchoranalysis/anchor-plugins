/* (C)2020 */
package ch.ethz.biol.cell.imageprocessing.stack.provider;

import org.anchoranalysis.bean.BeanInstanceMap;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.bean.error.BeanMisconfiguredException;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.bean.provider.ChnlProvider;
import org.anchoranalysis.image.bean.provider.stack.StackProvider;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.stack.DisplayStack;

public abstract class StackProviderWithBackground extends StackProvider {

    // START BEAN PROPERTIES

    // Either chnlProviderBackground or stackProviderBackground should be non-null
    //  but not both
    @BeanField @OptionalBean private ChnlProvider chnlBackground;

    @BeanField @OptionalBean private StackProvider stackBackground;

    @BeanField @OptionalBean private ChnlProvider chnlBackgroundMIP;
    // END BEAN PROPERTIES

    @Override
    public void checkMisconfigured(BeanInstanceMap defaultInstances)
            throws BeanMisconfiguredException {
        super.checkMisconfigured(defaultInstances);
        if (chnlBackground == null && stackBackground == null) {
            throw new BeanMisconfiguredException(
                    "Either chnlBackground or stackBackground should be set");
        }

        if (chnlBackground != null && stackBackground != null) {
            throw new BeanMisconfiguredException(
                    "Only one of chnlBackground and stackBackground should be set");
        }
    }

    protected DisplayStack backgroundStack(boolean do3D) throws CreateException {
        if (stackBackground != null) {
            return DisplayStack.create(stackBackground.createStack());

        } else {
            return DisplayStack.create(backgroundChnl(do3D));
        }
    }

    private Channel backgroundChnl(boolean do3D) throws CreateException {
        if (do3D) {
            return chnlBackground.create();
        } else {

            if (chnlBackgroundMIP != null) {
                return chnlBackgroundMIP.create();
            } else {
                return chnlBackground.create().maxIntensityProjection();
            }
        }
    }

    public ChnlProvider getChnlBackground() {
        return chnlBackground;
    }

    public void setChnlBackground(ChnlProvider chnlBackground) {
        this.chnlBackground = chnlBackground;
    }

    public StackProvider getStackBackground() {
        return stackBackground;
    }

    public void setStackBackground(StackProvider stackBackground) {
        this.stackBackground = stackBackground;
    }

    public ChnlProvider getChnlBackgroundMIP() {
        return chnlBackgroundMIP;
    }

    public void setChnlBackgroundMIP(ChnlProvider chnlBackgroundMIP) {
        this.chnlBackgroundMIP = chnlBackgroundMIP;
    }
}
