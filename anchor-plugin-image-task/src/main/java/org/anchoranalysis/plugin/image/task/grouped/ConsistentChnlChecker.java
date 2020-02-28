package org.anchoranalysis.plugin.image.task.grouped;

/*-
 * #%L
 * anchor-plugin-image-task
 * %%
 * Copyright (C) 2010 - 2019 Owen Feehan, ETH Zurich, University of Zurich, Hoffmann la Roche
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

import org.anchoranalysis.core.index.SetOperationFailedException;
import org.anchoranalysis.image.voxel.datatype.VoxelDataType;
import org.anchoranalysis.image.voxel.datatype.VoxelDataTypeUnsignedShort;

/**
 * Checks that the histograms created from channels all have the same data type, res, max-value etc.
 * 
 * @author FEEHANO
 *
 */
public class ConsistentChnlChecker {
	
	private long maxValue = 0;	// Unset
	private VoxelDataType chnlType;
	
	/** Checks that a channel has the same type (max value) as the others */
	public void checkChnlType( VoxelDataType chnlType ) throws SetOperationFailedException {
		long maxValueChnl = chnlType.maxValue();
		if( !isMaxValueSet() ) {
			setMaxValue(maxValueChnl);
			this.chnlType = chnlType;
		} else {
			if (getMaxValue()!=maxValueChnl) {
				throw new SetOperationFailedException("All images must have data-types of the same histogram size");
			} else if (!getChnlType().equals(chnlType)) {
				throw new SetOperationFailedException("All images must have the same data type");
			}
		}
	}
		
	private boolean isMaxValueSet() {
		return maxValue!=0;
	}

	public long getMaxValue() {
		return maxValue;
	}

	private void setMaxValue(long histogramMaxValue) throws SetOperationFailedException {
		
		if (histogramMaxValue>VoxelDataTypeUnsignedShort.MAX_VALUE) {
			throw new SetOperationFailedException( String.format("Histogram max-value (%d) must be set less than %d", histogramMaxValue, VoxelDataTypeUnsignedShort.MAX_VALUE) );
		}
		
		this.maxValue = histogramMaxValue;
	}

	public VoxelDataType getChnlType() {
		return chnlType;
	}
}
