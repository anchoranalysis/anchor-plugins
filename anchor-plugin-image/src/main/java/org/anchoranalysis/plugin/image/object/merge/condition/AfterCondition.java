/* (C)2020 */
package org.anchoranalysis.plugin.image.object.merge.condition;

import java.util.Optional;
import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.log.Logger;
import org.anchoranalysis.image.extent.ImageResolution;
import org.anchoranalysis.image.object.ObjectMask;

public interface AfterCondition {

    void init(Logger logger) throws InitException;

    boolean accept(
            ObjectMask source,
            ObjectMask destination,
            ObjectMask merged,
            Optional<ImageResolution> res)
            throws OperationFailedException;
}
