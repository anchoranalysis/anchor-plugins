package org.anchoranalysis.plugin.image.feature.object.calculation.single.morphological;

import java.util.Optional;

import org.anchoranalysis.core.error.CreateException;

/*
 * #%L
 * anchor-plugin-image
 * %%
 * Copyright (C) 2016 ETH Zurich, University of Zurich, Owen Feehan
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


import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.image.extent.Extent;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.image.object.morph.MorphologicalErosion;
import org.anchoranalysis.plugin.image.feature.object.calculation.single.CalculateIncrementalOperationMap;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
public class CalculateErosionMap extends CalculateIncrementalOperationMap {

	public CalculateErosionMap(boolean do3D) {
		super(do3D);
	}
	
	protected CalculateErosionMap(CalculateIncrementalOperationMap other) {
		super(other);
	}

	@Override
	protected ObjectMask applyOperation( ObjectMask om, Extent extent, boolean do3D ) throws OperationFailedException {
		try {
			return MorphologicalErosion.createErodedObjMask(
				om,
				Optional.of(extent),
				do3D,
				1,
				true,
				Optional.empty()
			);
		} catch (CreateException e) {
			throw new OperationFailedException(e);
		}
	}
}
