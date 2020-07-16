/* (C)2020 */
package org.anchoranalysis.plugin.annotation.comparison;

import lombok.Value;
import org.anchoranalysis.image.object.ObjectCollection;

@Value
public class ObjectsToCompare {

    /** Objects on left-side of the comparison */
    private final ObjectCollection left;

    /** Objects on right-side of the comparison */
    private final ObjectCollection right;
}
