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
