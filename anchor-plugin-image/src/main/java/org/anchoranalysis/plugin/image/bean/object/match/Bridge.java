/* (C)2020 */
package org.anchoranalysis.plugin.image.bean.object.match;

import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.image.bean.object.ObjectMatcher;
import org.anchoranalysis.image.object.MatchedObject;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.image.object.ObjectCollectionFactory;

/**
 * Matches to another object, and then uses that object to bridge to another
 *
 * @author Owen Feehan
 */
public class Bridge extends ObjectMatcher {

    // START BEAN PROPERTIES
    /** Used to match each input-object to an intermediary-object */
    @BeanField @Getter @Setter private ObjectMatcher bridgeMatcher;

    /** Used to match each intermediary-object to a final-object */
    @BeanField @Getter @Setter private ObjectMatcher objectMatcher;
    // END BEAN PROPERTIES

    @Override
    public List<MatchedObject> findMatch(ObjectCollection sourceObjects)
            throws OperationFailedException {

        List<MatchedObject> bridgeMatches = bridgeMatcher.findMatch(sourceObjects);

        ObjectCollection bridgeObjects =
                ObjectCollectionFactory.flatMapFrom(
                        bridgeMatches.stream().map(MatchedObject::getMatches),
                        OperationFailedException.class,
                        Bridge::checkExactlyOneMatch);

        return objectMatcher.findMatch(bridgeObjects);
    }

    private static ObjectCollection checkExactlyOneMatch(ObjectCollection matches)
            throws OperationFailedException {

        if (matches.size() == 0) {
            throw new OperationFailedException("At least one object has no match. One is needed");
        }

        if (matches.size() > 1) {
            throw new OperationFailedException(
                    "At least one object has multiple matches. Only one is allowed.");
        }

        return matches;
    }
}
