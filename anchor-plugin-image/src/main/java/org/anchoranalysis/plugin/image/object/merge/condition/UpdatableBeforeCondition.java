/*-
 * #%L
 * anchor-plugin-image
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

package org.anchoranalysis.plugin.image.object.merge.condition;

import java.util.Optional;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.image.core.dimensions.UnitConverter;
import org.anchoranalysis.image.voxel.object.ObjectMask;

/**
 * A two-stage process for checking if two {@link ObjectMask}s should be merged.
 *
 * <p>This interface allows for a more efficient checking process:
 *
 * <ol>
 *   <li>Set the source object (which may involve costly pre-computations)
 *   <li>Check multiple destination objects against the pre-computed source
 * </ol>
 *
 * <p>This approach is particularly useful for operations like growing object-masks to check for
 * neighboring objects.
 */
public interface UpdatableBeforeCondition {

    /**
     * Updates the source object and performs any necessary pre-computations.
     *
     * @param source the source {@link ObjectMask}
     * @param unitConverter an optional {@link UnitConverter} for unit conversions
     * @throws OperationFailedException if the update operation fails
     */
    void updateSourceObject(ObjectMask source, Optional<UnitConverter> unitConverter)
            throws OperationFailedException;

    /**
     * Checks if the destination object should be merged with the previously set source object.
     *
     * @param destination the destination {@link ObjectMask} to check against the source
     * @return true if the objects should be merged, false otherwise
     * @throws OperationFailedException if the check operation fails
     */
    boolean accept(ObjectMask destination) throws OperationFailedException;
}
