package org.anchoranalysis.plugin.image.task.bean.scale;

import java.util.function.BooleanSupplier;
import lombok.AllArgsConstructor;
import lombok.Value;

/**
 * Whether two different types of outputs are enabled.
 */
@Value @AllArgsConstructor class DualEnabled {
    /** Whether the non-flattened output is enabled? */
    boolean nonFlattened;
    
    /** Whether the flattened output is enabled? */
    boolean flattened;
    
    /** Whether either of the two outputs are enabled? */
    public boolean isEitherEnabled() {
        return nonFlattened || flattened;
    }

    /**
     * Performs a <i>logical and<i> operation with two booleans for {@code nonFlattened} and {@code flattened}.
     * 
     * <p>They are passed as suppliers to only be calculated if needed.
     * 
     * <p>This is an <b>immutable</b> operation.
     * 
     * @param nonFlattenedToCombine the second argument for a logical and with {@code nonFlattened}
     * @param flattenedToCombine the second argument for a logical and with {@code flattened}
     * @return a newly created {@link DualEnabled} that is the result of the logical and.
     */
    public DualEnabled and( BooleanSupplier nonFlattenedToCombine, BooleanSupplier flattenedToCombine ) {
        return new DualEnabled(nonFlattened && nonFlattenedToCombine.getAsBoolean(), flattened && flattenedToCombine.getAsBoolean());
    }
}