package org.anchoranalysis.plugin.image.obj.merge.condition;

import java.util.Optional;

import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.image.extent.ImageRes;
import org.anchoranalysis.image.objectmask.ObjectMask;

@FunctionalInterface
public interface BeforeCondition {
	boolean accept(ObjectMask omSrc, ObjectMask omDest, Optional<ImageRes> res) throws OperationFailedException;
}
