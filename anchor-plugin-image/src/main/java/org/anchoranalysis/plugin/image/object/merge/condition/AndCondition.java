/* (C)2020 */
package org.anchoranalysis.plugin.image.object.merge.condition;

import java.util.Optional;
import lombok.AllArgsConstructor;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.image.extent.ImageResolution;
import org.anchoranalysis.image.object.ObjectMask;

/** Combines two UpdatableBeforeConditions as LOGICAL ANDs */
@AllArgsConstructor
public class AndCondition implements UpdatableBeforeCondition {

    private final UpdatableBeforeCondition firstCondition;
    private final UpdatableBeforeCondition secondCondition;

    @Override
    public void updateSourceObject(ObjectMask source, Optional<ImageResolution> res)
            throws OperationFailedException {
        firstCondition.updateSourceObject(source, res);
        secondCondition.updateSourceObject(source, res);
    }

    @Override
    public boolean accept(ObjectMask destination) throws OperationFailedException {
        return firstCondition.accept(destination) && secondCondition.accept(destination);
    }
}
