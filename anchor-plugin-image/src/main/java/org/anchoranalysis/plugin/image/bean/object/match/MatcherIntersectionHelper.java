/* (C)2020 */
package org.anchoranalysis.plugin.image.bean.object.match;

import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.anchoranalysis.image.object.MatchedObject;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.image.object.ObjectMask;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MatcherIntersectionHelper {

    public static List<MatchedObject> matchIntersectingObjects(
            ObjectCollection sourceObjects, ObjectCollection searchObjects) {

        // Find matching seeds for each object
        return sourceObjects.stream()
                .mapToList(
                        objectSource ->
                                new MatchedObject(
                                        objectSource,
                                        searchObjectsThatIntersectWith(
                                                searchObjects, objectSource)));
    }

    private static ObjectCollection searchObjectsThatIntersectWith(
            ObjectCollection searchObjects, ObjectMask objToIntersectWith) {
        return searchObjects.stream().filter(objToIntersectWith::hasIntersectingVoxels);
    }
}
