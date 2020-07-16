/* (C)2020 */
package org.anchoranalysis.plugin.image.bean.object.provider.filter;

import java.util.List;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.bean.provider.ObjectCollectionProvider;
import org.anchoranalysis.image.extent.ImageDimensions;
import org.anchoranalysis.image.object.MatchedObject;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.image.object.ObjectCollectionFactory;
import org.anchoranalysis.plugin.image.bean.object.match.MatcherIntersectionHelper;

public class FilterByGroup extends ObjectCollectionProviderFilterBase {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private ObjectCollectionProvider objectsGrouped;
    // END BEAN PROPERTIES

    @Override
    protected ObjectCollection createFromObjects(
            ObjectCollection objects,
            Optional<ObjectCollection> objectsRejected,
            Optional<ImageDimensions> dim)
            throws CreateException {

        List<MatchedObject> matchList =
                MatcherIntersectionHelper.matchIntersectingObjects(
                        objectsGrouped.create(), objects);

        return ObjectCollectionFactory.flatMapFromCollection(
                matchList.stream().map(MatchedObject::getMatches),
                CreateException.class,
                matches -> filter(matches, dim, objectsRejected).asList());
    }
}
