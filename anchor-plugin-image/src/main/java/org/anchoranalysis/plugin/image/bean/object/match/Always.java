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
import org.anchoranalysis.image.object.ObjectCollectionFactory;
import org.anchoranalysis.image.object.ObjectMask;

/**
 * Specifies a single object that will always be the "match" for whatever source object.
 *
 * @author Owen Feehan
 */
public class Always extends ObjectMatcher {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private ObjectCollectionProvider objects;
    // END BEAN PROPERTIES

    @Override
    public List<MatchedObject> findMatch(ObjectCollection sourceObjects)
            throws OperationFailedException {

        ObjectMask match = determineMatch();

        return sourceObjects.stream()
                .mapToList(
                        object -> new MatchedObject(object, ObjectCollectionFactory.from(match)));
    }

    private ObjectMask determineMatch() throws OperationFailedException {
        ObjectCollection objectCollection;
        try {
            objectCollection = objects.create();
        } catch (CreateException e) {
            throw new OperationFailedException(e);
        }

        if (objectCollection.size() == 0) {
            throw new OperationFailedException("No objects provided");
        }
        if (objectCollection.size() > 1) {
            throw new OperationFailedException("More than one objects provided");
        }

        return objectCollection.get(0);
    }
}
