/* (C)2020 */
package org.anchoranalysis.plugin.image.bean.object.provider.filter;

import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.image.bean.object.ObjectMatcher;
import org.anchoranalysis.image.bean.provider.ObjectCollectionProviderUnary;
import org.anchoranalysis.image.object.MatchedObject;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.image.object.ObjectCollectionFactory;

public class Match extends ObjectCollectionProviderUnary {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private ObjectMatcher matcher;
    // END BEAN PROPERTIES

    @Override
    public ObjectCollection createFromObjects(ObjectCollection objects) throws CreateException {
        try {
            return ObjectCollectionFactory.flatMapFrom(
                    matcher.findMatch(objects), MatchedObject::getMatches);

        } catch (OperationFailedException e) {
            throw new CreateException(e);
        }
    }
}
