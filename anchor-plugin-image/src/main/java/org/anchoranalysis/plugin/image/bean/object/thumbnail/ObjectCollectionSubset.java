package org.anchoranalysis.plugin.image.bean.object.thumbnail;

import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.anchoranalysis.image.object.ObjectCollection;

/**
 * A subset of objects from a collection and their corresponding indices
 *
 * @author Owen Feehan
 */
@AllArgsConstructor
public class ObjectCollectionSubset {

    @Getter private ObjectCollection objects;
    @Getter private Set<Integer> indices;
}
