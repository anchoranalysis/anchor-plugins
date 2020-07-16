/* (C)2020 */
package org.anchoranalysis.plugin.image.bean.object.provider;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.bean.provider.ObjectCollectionProvider;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.image.object.ObjectCollectionFactory;

public class Concatenate extends ObjectCollectionProvider {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private List<ObjectCollectionProvider> list = new ArrayList<>();
    // END BEAN PROPERTIES

    @Override
    public ObjectCollection create() throws CreateException {
        return ObjectCollectionFactory.flatMapFrom(
                list.stream(), CreateException.class, ObjectCollectionProvider::create);
    }
}
