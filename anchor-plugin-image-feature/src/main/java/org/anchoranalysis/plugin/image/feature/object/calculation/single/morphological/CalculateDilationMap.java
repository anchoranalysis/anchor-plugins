/*-
 * #%L
 * anchor-plugin-image-feature
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

package org.anchoranalysis.plugin.image.feature.object.calculation.single.morphological;

import java.util.Optional;
import lombok.EqualsAndHashCode;
import org.anchoranalysis.core.exception.CreateException;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.image.voxel.object.ObjectMask;
import org.anchoranalysis.image.voxel.object.morphological.MorphologicalDilation;
import org.anchoranalysis.plugin.image.feature.object.calculation.single.CalculateIncrementalOperationMap;
import org.anchoranalysis.spatial.box.Extent;

/**
 * Calculates a map of dilated {@link ObjectMask}s for different iteration counts.
 *
 * <p>This class extends {@link CalculateIncrementalOperationMap} to perform dilation operations on
 * object masks and cache the results for different iteration counts.
 */
@EqualsAndHashCode(callSuper = true)
public class CalculateDilationMap extends CalculateIncrementalOperationMap {

    /**
     * Creates a new {@link CalculateDilationMap}.
     *
     * @param do3D whether to perform 3D dilation (true) or 2D dilation (false)
     */
    public CalculateDilationMap(boolean do3D) {
        super(do3D);
    }

    /**
     * Copy constructor for {@link CalculateDilationMap}.
     *
     * @param other the {@link CalculateIncrementalOperationMap} to copy from
     */
    protected CalculateDilationMap(CalculateIncrementalOperationMap other) {
        super(other);
    }

    @Override
    protected ObjectMask applyOperation(ObjectMask object, Extent extent, boolean do3D)
            throws OperationFailedException {
        try {
            return MorphologicalDilation.dilate(object, Optional.of(extent), do3D, 1, false);
        } catch (CreateException e) {
            throw new OperationFailedException(e);
        }
    }
}
