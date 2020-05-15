package org.anchoranalysis.plugin.image.obj.merge.condition;

import java.util.Optional;

import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.image.extent.ImageRes;
import org.anchoranalysis.image.objmask.ObjMask;

@FunctionalInterface
public interface BeforeCondition {
	boolean accept(ObjMask omSrc, ObjMask omDest, Optional<ImageRes> res) throws OperationFailedException;
}
