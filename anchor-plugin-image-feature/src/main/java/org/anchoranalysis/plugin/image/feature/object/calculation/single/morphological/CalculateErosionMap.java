/* (C)2020 */
package org.anchoranalysis.plugin.image.feature.object.calculation.single.morphological;

import java.util.Optional;
import lombok.EqualsAndHashCode;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.image.extent.Extent;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.image.object.morph.MorphologicalErosion;
import org.anchoranalysis.plugin.image.feature.object.calculation.single.CalculateIncrementalOperationMap;

@EqualsAndHashCode(callSuper = true)
public class CalculateErosionMap extends CalculateIncrementalOperationMap {

    public CalculateErosionMap(boolean do3D) {
        super(do3D);
    }

    protected CalculateErosionMap(CalculateIncrementalOperationMap other) {
        super(other);
    }

    @Override
    protected ObjectMask applyOperation(ObjectMask object, Extent extent, boolean do3D)
            throws OperationFailedException {
        try {
            return MorphologicalErosion.createErodedObject(
                    object, Optional.of(extent), do3D, 1, true, Optional.empty());
        } catch (CreateException e) {
            throw new OperationFailedException(e);
        }
    }
}
