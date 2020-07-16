/* (C)2020 */
package ch.ethz.biol.cell.imageprocessing.chnl.provider;

import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.core.name.provider.NamedProviderGetException;
import org.anchoranalysis.image.bean.nonbean.init.ImageInitParams;
import org.anchoranalysis.image.bean.provider.ChnlProvider;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.experiment.identifiers.ImgStackIdentifiers;
import org.anchoranalysis.image.stack.Stack;

// Simply returns the inputStack
public class ChnlProviderInput extends ChnlProvider {

    private Stack inputStack;

    @Override
    public void onInit(ImageInitParams so) throws InitException {
        super.onInit(so);
        try {
            inputStack = so.getStackCollection().getException(ImgStackIdentifiers.INPUT_IMAGE);
        } catch (NamedProviderGetException e) {
            throw InitException.createOrReuse(e.summarize());
        }
    }

    @Override
    public Channel create() throws CreateException {
        return inputStack.getChnl(0);
    }
}
