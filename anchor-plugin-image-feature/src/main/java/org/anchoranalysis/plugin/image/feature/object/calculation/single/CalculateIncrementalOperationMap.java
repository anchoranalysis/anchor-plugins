/* (C)2020 */
package org.anchoranalysis.plugin.image.feature.object.calculation.single;

import lombok.EqualsAndHashCode;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.extent.Extent;
import org.anchoranalysis.image.feature.object.input.FeatureInputSingleObject;
import org.anchoranalysis.image.object.ObjectMask;

@EqualsAndHashCode(callSuper = false)
public abstract class CalculateIncrementalOperationMap
        extends CacheableCalculationMapHash<
                ObjectMask, FeatureInputSingleObject, Integer, FeatureCalcException> {
    private boolean do3D;

    public CalculateIncrementalOperationMap(boolean do3D) {
        super(100);
        this.do3D = do3D;
    }

    protected CalculateIncrementalOperationMap(CalculateIncrementalOperationMap other) {
        super(100);
        this.do3D = other.do3D;
    }

    @Override
    protected ObjectMask execute(FeatureInputSingleObject params, Integer key)
            throws FeatureCalcException {
        Extent extent = params.getDimensionsRequired().getExtent();

        if (key == 0) {
            throw new FeatureCalcException("Key must be > 0");
        }

        int lowestExistingKey = findHighestExistingKey(key - 1);

        ObjectMask object =
                lowestExistingKey != 0 ? getOrNull(lowestExistingKey) : params.getObject();

        try {
            for (int i = (lowestExistingKey + 1); i <= key; i++) {

                ObjectMask next = applyOperation(object, extent, do3D);

                // save in cache, as long as it's not the final one, as this will save after the
                // function executes
                if (i != key) {
                    this.put(i, next);
                }

                object = next;
            }
            return object;

        } catch (OperationFailedException e) {
            throw new FeatureCalcException(e);
        }
    }

    /**
     * Lowest existing key. 0 if no key exists.
     *
     * @param max
     * @return
     */
    private int findHighestExistingKey(int max) {
        for (int i = max; i >= 1; i--) {
            if (this.hasKey(i)) {
                return i;
            }
        }
        return 0;
    }

    protected abstract ObjectMask applyOperation(ObjectMask object, Extent extent, boolean do3D)
            throws OperationFailedException;
}
