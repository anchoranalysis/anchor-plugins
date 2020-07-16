/* (C)2020 */
package ch.ethz.biol.cell.imageprocessing.chnl.provider;

import ch.ethz.biol.cell.imageprocessing.dim.provider.GuessDimFromInputImage;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.bean.provider.ChnlProvider;
import org.anchoranalysis.image.bean.provider.ImageDimProvider;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.extent.ImageDimensions;

public abstract class ChnlProviderDimSource extends ChnlProvider {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private ImageDimProvider dim = new GuessDimFromInputImage();
    // END BEAN PROPERTIES

    @Override
    public Channel create() throws CreateException {
        return createFromDim(dim.create());
    }

    protected abstract Channel createFromDim(ImageDimensions dim) throws CreateException;
}
