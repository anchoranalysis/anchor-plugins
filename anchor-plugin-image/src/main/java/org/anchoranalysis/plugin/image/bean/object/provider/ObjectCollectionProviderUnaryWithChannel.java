/* (C)2020 */
package org.anchoranalysis.plugin.image.bean.object.provider;

import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.bean.provider.ChnlProvider;
import org.anchoranalysis.image.bean.provider.ObjectCollectionProviderUnary;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.object.ObjectCollection;

public abstract class ObjectCollectionProviderUnaryWithChannel
        extends ObjectCollectionProviderUnary {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private ChnlProvider chnl;
    // END BEAN PROPERTIES

    @Override
    public ObjectCollection createFromObjects(ObjectCollection objectsSource)
            throws CreateException {

        return createFromObjects(objectsSource, chnl.create());
    }

    protected abstract ObjectCollection createFromObjects(
            ObjectCollection objectsSource, Channel channelSource) throws CreateException;
}
