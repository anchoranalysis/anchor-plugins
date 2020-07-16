/* (C)2020 */
package org.anchoranalysis.plugin.image.bean.object.provider;

import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.bean.provider.ChnlProvider;
import org.anchoranalysis.image.bean.provider.ObjectCollectionProvider;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.object.ObjectCollection;

public abstract class ObjectCollectionProviderWithChannel extends ObjectCollectionProvider {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private ChnlProvider chnl;
    // END BEAN PROPERTIES

    @Override
    public ObjectCollection create() throws CreateException {
        return createFromChnl(chnl.create());
    }

    protected abstract ObjectCollection createFromChnl(Channel chnlSrc) throws CreateException;
}
