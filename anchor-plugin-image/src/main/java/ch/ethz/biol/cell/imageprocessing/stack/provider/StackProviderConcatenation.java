/* (C)2020 */
package ch.ethz.biol.cell.imageprocessing.stack.provider;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.bean.provider.BeanImgStackProvider;
import org.anchoranalysis.image.bean.provider.stack.StackProvider;
import org.anchoranalysis.image.extent.IncorrectImageSizeException;
import org.anchoranalysis.image.stack.Stack;

public class StackProviderConcatenation extends StackProvider {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private List<BeanImgStackProvider<?, ?>> list = new ArrayList<>();
    // END BEAN PROPERTIES

    @Override
    public Stack create() throws CreateException {

        try {
            Stack out = new Stack();

            for (BeanImgStackProvider<?, ?> provider : list) {

                Stack crnt = provider.createStack();

                for (int c = 0; c < crnt.getNumChnl(); c++) {
                    out.addChnl(crnt.getChnl(c));
                }
            }

            return out;

        } catch (IncorrectImageSizeException e) {
            throw new CreateException(e);
        }
    }
}
