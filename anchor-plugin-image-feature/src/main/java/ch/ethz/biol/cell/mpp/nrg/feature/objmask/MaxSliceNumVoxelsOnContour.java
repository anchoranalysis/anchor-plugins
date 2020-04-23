package ch.ethz.biol.cell.mpp.nrg.feature.objmask;

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

import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.feature.cache.SessionInput;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.binary.voxel.BinaryVoxelBox;
import org.anchoranalysis.image.feature.bean.objmask.FeatureObjMask;
import org.anchoranalysis.image.feature.objmask.FeatureInputSingleObj;
import org.anchoranalysis.image.objmask.ObjMask;
import org.anchoranalysis.image.outline.FindOutline;

public class MaxSliceNumVoxelsOnContour extends FeatureObjMask {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static int cntForByteBuffer( ByteBuffer bb, byte equalVal ) {
		int cnt = 0;
		while(bb.hasRemaining()) {
			if(bb.get()==equalVal) {
				cnt++;
			}
		}
		return cnt;
	}
	
	private static int sliceWithMaxNumVoxels( ObjMask om ) {
		
		int max = 0;
		int ind = 0;
		
		for( int z=0; z<om.getBoundingBox().extnt().getZ(); z++) {
			ByteBuffer bb = om.getVoxelBox().getPixelsForPlane(z).buffer();
			int cnt = cntForByteBuffer(bb, om.getBinaryValuesByte().getOnByte());
		
			if (cnt>max) {
				max = cnt;
				ind = z;
			}
			
		}
		
		return ind;
	}
	
	@Override
	public double calc(SessionInput<FeatureInputSingleObj> paramsCacheable) throws FeatureCalcException {
				
		try {
			FeatureInputSingleObj params = paramsCacheable.getParams();
			
			int z = sliceWithMaxNumVoxels( params.getObjMask() );
			
			ObjMask omSlice = params.getObjMask().extractSlice(z, false);
			BinaryVoxelBox<ByteBuffer> outlineSlice = FindOutline.outline(omSlice, 1, true, false ).binaryVoxelBox();
			
			return outlineSlice.countOn();
			
		} catch (OperationFailedException | CreateException e) {
			throw new FeatureCalcException(e);
		}

	}
}
