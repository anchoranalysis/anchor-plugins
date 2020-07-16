/* (C)2020 */
package ch.ethz.biol.cell.imageprocessing.chnl.provider;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.bean.provider.ChnlProvider;
import org.anchoranalysis.image.bean.provider.stack.StackProvider;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.stack.Stack;

public class ChnlProviderStack extends ChnlProvider {

    // START
    @BeanField private int chnlIndex = 0;

    @BeanField private StackProvider stackProvider;
    // END

    public ChnlProviderStack() {}

    public ChnlProviderStack(StackProvider stackProvider) {
        this.stackProvider = stackProvider;
    }

    @Override
    public Channel create() throws CreateException {

        Stack in = stackProvider.create();
        return in.getChnl(chnlIndex);
    }

    public int getChnlIndex() {
        return chnlIndex;
    }

    public void setChnlIndex(int chnlIndex) {
        this.chnlIndex = chnlIndex;
    }

    public StackProvider getStackProvider() {
        return stackProvider;
    }

    public void setStackProvider(StackProvider stackProvider) {
        this.stackProvider = stackProvider;
    }
}
