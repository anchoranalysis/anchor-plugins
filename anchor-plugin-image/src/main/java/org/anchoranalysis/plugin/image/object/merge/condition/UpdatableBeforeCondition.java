/* (C)2020 */
package org.anchoranalysis.plugin.image.object.merge.condition;

import java.util.Optional;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.image.extent.ImageResolution;
import org.anchoranalysis.image.object.ObjectMask;

/**
 * Rather than checking if obj1 vs obj2 is accepted instead there's a two stage process
 *
 * <p>1. Set Obj1 2. Check Obj2 vs Step 1
 *
 * <p>This allows additional (costly) operations to occur after Step 1, that can be repeatedly used
 * in Step 2 e.g. growing object-masks to check for neighboring objects
 *
 * @author Owen Feehan
 */
public interface UpdatableBeforeCondition {

    void updateSourceObject(ObjectMask source, Optional<ImageResolution> res)
            throws OperationFailedException;

    boolean accept(ObjectMask destination) throws OperationFailedException;
}
