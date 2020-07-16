/* (C)2020 */
package org.anchoranalysis.plugin.image.bean.object.provider;

import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.core.name.provider.NamedProviderGetException;
import org.anchoranalysis.image.bean.nonbean.init.ImageInitParams;
import org.anchoranalysis.image.bean.provider.ObjectCollectionProvider;
import org.anchoranalysis.image.object.ObjectCollection;

/**
 * Returns a object-collection by name if it exists, or else calls {@code objectsElse} if it doesn't
 * exist.
 *
 * @author Owen Feehan
 */
public class ReferenceOrElse extends ObjectCollectionProvider {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private String id = "";

    @BeanField @Getter @Setter private ObjectCollectionProvider objectsElse;
    // END BEAN PROPERTIES

    private ObjectCollection objects;

    @Override
    public void onInit(ImageInitParams so) throws InitException {
        try {
            objects = so.getObjectCollection().getException(id);
        } catch (NamedProviderGetException e) {
            throw new InitException(e.summarize());
        }
    }

    @Override
    public ObjectCollection create() throws CreateException {

        if (objects != null) {
            return objects;
        } else {
            return objectsElse.create();
        }
    }
}
