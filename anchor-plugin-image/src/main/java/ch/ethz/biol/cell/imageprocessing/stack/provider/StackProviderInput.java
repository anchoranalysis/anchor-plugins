/* (C)2020 */
package ch.ethz.biol.cell.imageprocessing.stack.provider;

import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.core.name.provider.NamedProviderGetException;
import org.anchoranalysis.image.bean.nonbean.init.ImageInitParams;
import org.anchoranalysis.image.bean.provider.stack.StackProvider;
import org.anchoranalysis.image.experiment.identifiers.ImgStackIdentifiers;
import org.anchoranalysis.image.stack.Stack;

// Simply returns the inputStack
public class StackProviderInput extends StackProvider {

    private Stack inputStack;

    @Override
    public Stack create() {
        return inputStack;
    }

    @Override
    public void onInit(ImageInitParams so) throws InitException {
        super.onInit(so);
        try {
            inputStack = so.getStackCollection().getException(ImgStackIdentifiers.INPUT_IMAGE);
        } catch (NamedProviderGetException e) {
            throw new InitException(e.summarize());
        }
    }
}
