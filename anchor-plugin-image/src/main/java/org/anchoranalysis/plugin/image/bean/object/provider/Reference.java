/* (C)2020 */
package org.anchoranalysis.plugin.image.bean.object.provider;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.name.provider.NamedProviderGetException;
import org.anchoranalysis.image.bean.provider.ObjectCollectionProvider;
import org.anchoranalysis.image.object.ObjectCollection;

@NoArgsConstructor
public class Reference extends ObjectCollectionProvider {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private String id = "";
    // END BEAN PROPERTIES

    private ObjectCollection objects;

    public Reference(String id) {
        this.id = id;
    }

    @Override
    public ObjectCollection create() throws CreateException {
        if (objects == null) {
            try {
                objects = getInitializationParameters().getObjectCollection().getException(id);
            } catch (NamedProviderGetException e) {
                throw new CreateException(e);
            }
        }
        return objects;
    }
}
