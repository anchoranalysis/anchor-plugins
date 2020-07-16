/* (C)2020 */
package org.anchoranalysis.plugin.image.object.merge.condition;

import java.util.Optional;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.image.extent.ImageResolution;
import org.anchoranalysis.image.object.ObjectMask;

@FunctionalInterface
public interface BeforeCondition {
    boolean accept(ObjectMask source, ObjectMask destination, Optional<ImageResolution> res)
            throws OperationFailedException;
}
