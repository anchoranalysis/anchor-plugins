package org.anchoranalysis.plugin.image.calculation;

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
import org.anchoranalysis.image.objmask.ObjMask;
import org.anchoranalysis.image.objmask.morph.MorphologicalDilation;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class CalculateDilationMap extends CalculateIncrementalOperationMap {

	public CalculateDilationMap(boolean do3d) {
		super(do3d);
	}
	
	protected CalculateDilationMap(CalculateIncrementalOperationMap other) {
		super(other);
	}

	@Override
	protected ObjMask applyOperation( ObjMask om, Extent extnt, boolean do3D ) throws OperationFailedException {
		try {
			return MorphologicalDilation.createDilatedObjMask(
				om,
				Optional.of(extnt),
				do3D,
				1,
				false
			);
		} catch (CreateException e) {
			throw new OperationFailedException(e);
		}
	}
	
	@Override
	public boolean equals(final Object obj){
	    if(obj instanceof CalculateDilationMap){
	        return super.equals(obj);
	    } else{
	        return false;
	    }
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder()
			.appendSuper( super.hashCode() )
			.hashCode();
	}
}