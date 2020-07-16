/* (C)2020 */
package ch.ethz.biol.cell.imageprocessing.binaryimgchnl.provider;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.name.provider.NamedProviderGetException;
import org.anchoranalysis.image.bean.provider.BinaryChnlProvider;
import org.anchoranalysis.image.binary.mask.Mask;

@NoArgsConstructor
public class BinaryChnlProviderReference extends BinaryChnlProvider {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private String id = "";

    /**
     * If true the channel is duplicated after it is retrieved, to prevent overwriting exisiting
     * data This is a shortcut to avoid embedding beans in a ChnlProviderDuplicate
     */
    @BeanField @Getter @Setter private boolean duplicate = false;
    // END BEAN PROPERTIES

    private Mask bi;

    public BinaryChnlProviderReference(String id) {
        super();
        this.id = id;
    }

    @Override
    public Mask create() throws CreateException {
        assert (this.isInitialized());
        if (bi == null) {
            bi = createChnl();
        }
        return bi;
    }

    private Mask createChnl() throws CreateException {
        try {
            Mask chnl = getInitializationParameters().getBinaryImageCollection().getException(id);

            if (duplicate) {
                return chnl.duplicate();
            } else {
                return chnl;
            }
        } catch (NamedProviderGetException e) {
            throw new CreateException(e);
        }
    }
}
