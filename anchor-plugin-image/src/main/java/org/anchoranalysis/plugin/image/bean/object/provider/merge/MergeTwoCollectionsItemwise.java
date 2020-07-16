/* (C)2020 */
package org.anchoranalysis.plugin.image.bean.object.provider.merge;

import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.bean.provider.ObjectCollectionProvider;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.image.object.ObjectCollectionFactory;
import org.anchoranalysis.image.object.ops.ObjectMaskMerger;

/**
 * Merges itemwise objects from two collections.
 *
 * <p>e.g. element 0 from both collections are merged together, then element 1 etc.
 *
 * <p>Each collection must have the same number of objects, as will the newly created merged
 * collection.
 *
 * @author Owen Feehan
 */
public class MergeTwoCollectionsItemwise extends ObjectCollectionProvider {

    // START BEAN PROPERTIES
    /** First collection with items to be merged */
    @BeanField @Getter @Setter private ObjectCollectionProvider objects1;

    /** Second collection with items to be merged */
    @BeanField @Getter @Setter private ObjectCollectionProvider objects2;
    // END BEAN PROPERTIES

    @Override
    public ObjectCollection create() throws CreateException {

        ObjectCollection first = objects1.create();
        ObjectCollection second = objects2.create();

        if (first.size() != second.size()) {
            throw new CreateException(
                    String.format(
                            "Both object-providers must have the same number of items, currently %d and %d",
                            first.size(), second.size()));
        }

        return ObjectCollectionFactory.mapFromRange(
                0,
                first.size(),
                index -> ObjectMaskMerger.merge(first.get(index), second.get(index)));
    }
}
