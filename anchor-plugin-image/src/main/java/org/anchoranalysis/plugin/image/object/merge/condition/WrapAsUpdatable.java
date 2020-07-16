/* (C)2020 */
package org.anchoranalysis.plugin.image.object.merge.condition;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.image.extent.ImageResolution;
import org.anchoranalysis.image.object.ObjectMask;

/**
 * Wraps a BeforeCondition as an UpdatableBefoerCondition
 *
 * @author Owen Feehan
 */
@RequiredArgsConstructor
public class WrapAsUpdatable implements UpdatableBeforeCondition {

    // START REQUIRED ARGUMENTS
    private final BeforeCondition beforeCondition;
    // END REQUIRED ARGUMENTS

    // TEMPORARILY UPDATED
    private ObjectMask objectSource;
    private Optional<ImageResolution> resolution;

    @Override
    public void updateSourceObject(ObjectMask source, Optional<ImageResolution> res)
            throws OperationFailedException {
        this.objectSource = source;
        this.resolution = res;
    }

    @Override
    public boolean accept(ObjectMask destination) throws OperationFailedException {
        return beforeCondition.accept(objectSource, destination, resolution);
    }
}
