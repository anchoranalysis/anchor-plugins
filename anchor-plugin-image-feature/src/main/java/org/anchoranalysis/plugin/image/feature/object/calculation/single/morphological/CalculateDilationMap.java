/* (C)2020 */
package org.anchoranalysis.plugin.image.feature.object.calculation.single.morphological;

import java.util.Optional;
import lombok.EqualsAndHashCode;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.image.extent.Extent;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.image.object.morph.MorphologicalDilation;
import org.anchoranalysis.plugin.image.feature.object.calculation.single.CalculateIncrementalOperationMap;

@EqualsAndHashCode(callSuper = true)
public class CalculateDilationMap extends CalculateIncrementalOperationMap {

    public CalculateDilationMap(boolean do3D) {
        super(do3D);
    }

    protected CalculateDilationMap(CalculateIncrementalOperationMap other) {
        super(other);
    }

    @Override
    protected ObjectMask applyOperation(ObjectMask object, Extent extent, boolean do3D)
            throws OperationFailedException {
        try {
            return MorphologicalDilation.createDilatedObject(
                    object, Optional.of(extent), do3D, 1, false);
        } catch (CreateException e) {
            throw new OperationFailedException(e);
        }
    }
}
