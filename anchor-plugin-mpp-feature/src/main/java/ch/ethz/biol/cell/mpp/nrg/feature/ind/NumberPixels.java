package ch.ethz.biol.cell.mpp.nrg.feature.ind;

import org.anchoranalysis.anchor.mpp.feature.bean.mark.PxlListOperationFromMark;
import org.anchoranalysis.anchor.mpp.feature.bean.nrg.elem.FeatureSingleMemo;
import org.anchoranalysis.anchor.mpp.feature.input.memo.FeatureInputSingleMemo;

/*
 * #%L
 * anchor-plugin-mpp-feature
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


import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.relation.RelationToValue;
import org.anchoranalysis.feature.cache.SessionInput;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.voxel.VoxelIntensityList;

public class NumberPixels extends FeatureSingleMemo {

	// START BEAN PROPERTIES
	@BeanField
	private PxlListOperationFromMark pixelList;
	// END BEAN PROPERTIES
	
	public static int countFromPxlList( VoxelIntensityList list, RelationToValue relationToThreshold, double threshold ) {
		int count = 0;
		
		for (int i=0; i<list.size(); i++) {
			double pxlVal = list.get(i);
			
			if (relationToThreshold.isRelationToValueTrue(pxlVal,threshold)) {
				count++;
			}
		}
		return count;
	}
	
	@Override
	public double calc( SessionInput<FeatureInputSingleMemo> input ) throws FeatureCalcException {

		try {
			return pixelList.doOperation(
				input.get().getPxlPartMemo(),
				input.get().getDimensionsRequired()
			);
		} catch (OperationFailedException e) {
			throw new FeatureCalcException(e);
		}							
	}

	@Override
	public String getParamDscr() {
		return String.format("pixelList=%s", pixelList.toString() );
	}

	public PxlListOperationFromMark getPixelList() {
		return pixelList;
	}

	public void setPixelList(PxlListOperationFromMark pixelList) {
		this.pixelList = pixelList;
	}

}
