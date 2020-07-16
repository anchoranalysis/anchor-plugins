/* (C)2020 */
package ch.ethz.biol.cell.imageprocessing.stack.provider;

import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.bean.provider.ChnlProvider;
import org.anchoranalysis.image.bean.provider.stack.StackProvider;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.extent.ImageDimensions;
import org.anchoranalysis.image.extent.IncorrectImageSizeException;
import org.anchoranalysis.image.stack.Stack;

public class StackProviderRGBSingleChnlProvider extends StackProvider {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private ChnlProvider chnl;
    // END BEAN PROPERTIES

    @Override
    public Stack create() throws CreateException {

        Channel chnlIn = chnl.create();

        ImageDimensions sd = chnlIn.getDimensions();

        try {
            Stack out = new Stack();
            addToStack(out, chnlIn, sd);
            addToStack(out, chnlIn, sd);
            addToStack(out, chnlIn, sd);
            return out;
        } catch (IncorrectImageSizeException e) {
            throw new CreateException(e);
        }
    }

    private void addToStack(Stack stack, Channel chnl, ImageDimensions dimensions)
            throws IncorrectImageSizeException {
        stack.addChnl(chnl.duplicate());
    }
}
