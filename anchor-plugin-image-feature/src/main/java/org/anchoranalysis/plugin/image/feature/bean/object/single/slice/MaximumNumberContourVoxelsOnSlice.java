package org.anchoranalysis.plugin.image.feature.bean.object.single.slice;

/*
 * #%L
 * anchor-plugin-image-feature
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


import java.nio.ByteBuffer;

import org.anchoranalysis.feature.cache.SessionInput;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.feature.bean.object.single.FeatureSingleObject;
import org.anchoranalysis.image.feature.object.input.FeatureInputSingleObject;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.image.outline.FindOutline;

/**
 * Maximum number of voxels on any slice's contour (edge voxels) across all slices.
 * @author Owen Feehan
 *
 */
public class MaximumNumberContourVoxelsOnSlice extends FeatureSingleObject {
	
	@Override
	public double calc(SessionInput<FeatureInputSingleObject> input) throws FeatureCalcException {
		ObjectMask object = input.get().getObject();

		return numVoxelsOnContour(
			sliceWithMaxNumVoxels(object)
		);
	}
	
	private static ObjectMask sliceWithMaxNumVoxels(ObjectMask obj) {
		return obj.extractSlice(
			indexOfSliceWithMaxNumVoxels(obj),
			false
		);
	}
	
	private static int numVoxelsOnContour( ObjectMask obj ) {
		return FindOutline
				.outline(obj, 1, true, false)
				.binaryVoxelBox()
				.countOn();
	}
	
	private static int cntForByteBuffer( ByteBuffer bb, byte equalVal ) {
		int cnt = 0;
		while(bb.hasRemaining()) {
			if(bb.get()==equalVal) {
				cnt++;
			}
		}
		return cnt;
	}
	
	private static int indexOfSliceWithMaxNumVoxels( ObjectMask object ) {
		
		int max = 0;
		int ind = 0;
		
		for( int z=0; z<object.getBoundingBox().extent().getZ(); z++) {
			ByteBuffer bb = object.getVoxelBox().getPixelsForPlane(z).buffer();
			int cnt = cntForByteBuffer(bb, object.getBinaryValuesByte().getOnByte());
		
			if (cnt>max) {
				max = cnt;
				ind = z;
			}
		}
		return ind;
	}
}
