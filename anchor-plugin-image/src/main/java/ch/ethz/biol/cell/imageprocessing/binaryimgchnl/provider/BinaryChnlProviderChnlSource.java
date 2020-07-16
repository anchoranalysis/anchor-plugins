/* (C)2020 */
package ch.ethz.biol.cell.imageprocessing.binaryimgchnl.provider;

import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.bean.provider.BinaryChnlProvider;
import org.anchoranalysis.image.bean.provider.ChnlProvider;
import org.anchoranalysis.image.binary.mask.Mask;
import org.anchoranalysis.image.channel.Channel;

public abstract class BinaryChnlProviderChnlSource extends BinaryChnlProvider {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private ChnlProvider chnl;
    // END BEAN PROPERTIES

    @Override
    public Mask create() throws CreateException {
        return createFromSource(chnl.create());
    }

    protected abstract Mask createFromSource(Channel chnlSource) throws CreateException;
}
