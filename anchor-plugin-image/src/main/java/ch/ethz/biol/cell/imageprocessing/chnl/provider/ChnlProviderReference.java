/* (C)2020 */
package ch.ethz.biol.cell.imageprocessing.chnl.provider;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.name.provider.NamedProviderGetException;
import org.anchoranalysis.image.bean.provider.ChnlProvider;
import org.anchoranalysis.image.channel.Channel;

@NoArgsConstructor
public class ChnlProviderReference extends ChnlProvider {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private String id = "";

    /**
     * If true the channel is duplicated after it is retrieved, to prevent overwriting existing data
     * This is a shortcut to avoid embedding beans in a ChnlProviderDuplicate
     */
    @BeanField @Getter @Setter private boolean duplicate = false;
    // END BEAN PROPERTIES

    private Channel channel;

    public ChnlProviderReference(String id) {
        super();
        this.id = id;
    }

    @Override
    public Channel create() throws CreateException {
        if (channel == null) {
            channel = getMaybeDuplicate();
        }
        return channel;
    }

    private Channel getMaybeDuplicate() throws CreateException {
        try {
            Channel existing = getInitializationParameters().getChnlCollection().getException(id);

            if (duplicate) {
                return existing.duplicate();
            } else {
                return existing;
            }
        } catch (NamedProviderGetException e) {
            throw new CreateException(e);
        }
    }
}
