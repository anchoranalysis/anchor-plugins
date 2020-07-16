/* (C)2020 */
package org.anchoranalysis.plugin.image.bean.object.match;

import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.image.bean.object.ObjectMatcher;
import org.anchoranalysis.image.bean.provider.ObjectCollectionProvider;
import org.anchoranalysis.image.object.MatchedObject;
import org.anchoranalysis.image.object.ObjectCollection;

public class Intersection extends ObjectMatcher {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private ObjectCollectionProvider objects;
    // END BEAN PROPERTIES

    @Override
    public List<MatchedObject> findMatch(ObjectCollection sourceObjects)
            throws OperationFailedException {

        try {
            return MatcherIntersectionHelper.matchIntersectingObjects(
                    sourceObjects, objects.create());

        } catch (CreateException e) {
            throw new OperationFailedException(e);
        }
    }
}
