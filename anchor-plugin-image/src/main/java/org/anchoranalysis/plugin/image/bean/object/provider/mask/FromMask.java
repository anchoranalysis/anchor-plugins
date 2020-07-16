/* (C)2020 */
package org.anchoranalysis.plugin.image.bean.object.provider.mask;

import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.bean.provider.BinaryChnlProvider;
import org.anchoranalysis.image.bean.provider.ObjectCollectionProvider;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.image.object.ObjectCollectionFactory;

/**
 * Converts a binary-mask to an object-collection (containing a single object)
 *
 * @author Owen Feehan
 */
public class FromMask extends ObjectCollectionProvider {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private BinaryChnlProvider binaryChnl;
    // END BEAN PROPERTIES

    @Override
    public ObjectCollection create() throws CreateException {
        return ObjectCollectionFactory.from(binaryChnl.create());
    }
}
