/*-
 * #%L
 * anchor-plugin-image-task
 * %%
 * Copyright (C) 2010 - 2020 Owen Feehan, ETH Zurich, University of Zurich, Hoffmann-La Roche
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */
package org.anchoranalysis.plugin.image.task.bean.scale;

import java.util.function.BooleanSupplier;
import lombok.AllArgsConstructor;
import lombok.Value;

/** Whether two different types of outputs are enabled. */
@Value
@AllArgsConstructor
class DualEnabled {
    /** Whether the non-flattened output is enabled. */
    private boolean nonFlattened;

    /** Whether the flattened output is enabled. */
    private boolean flattened;

    /**
     * Checks if either of the two outputs are enabled.
     *
     * @return true if either non-flattened or flattened output is enabled, false otherwise.
     */
    public boolean isEitherEnabled() {
        return nonFlattened || flattened;
    }

    /**
     * Performs a <i>logical and</i> operation with two booleans for {@code nonFlattened} and {@code
     * flattened}.
     *
     * <p>They are passed as suppliers to only be calculated if needed.
     *
     * <p>This is an <b>immutable</b> operation.
     *
     * @param nonFlattenedToCombine the second argument for a logical and with {@code nonFlattened}
     * @param flattenedToCombine the second argument for a logical and with {@code flattened}
     * @return a newly created {@link DualEnabled} that is the result of the logical and.
     */
    public DualEnabled and(
            BooleanSupplier nonFlattenedToCombine, BooleanSupplier flattenedToCombine) {
        return new DualEnabled(
                nonFlattened && nonFlattenedToCombine.getAsBoolean(),
                flattened && flattenedToCombine.getAsBoolean());
    }
}
