package org.anchoranalysis.plugin.image.feature.bean.obj.pair.touching;

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


import org.anchoranalysis.core.cache.ExecuteException;
import org.anchoranalysis.feature.cache.calculation.CachedCalculation;
import org.anchoranalysis.image.extent.BoundingBox;
import org.anchoranalysis.image.extent.Extent;
import org.anchoranalysis.image.feature.objmask.pair.FeatureObjMaskPairParams;
import org.anchoranalysis.image.objmask.ObjMask;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

class CalculateIntersectionOfDilatedBoundingBox extends CachedCalculation<BoundingBox, FeatureObjMaskPairParams> {

	
	
	private boolean use3D = false;
			
	public CalculateIntersectionOfDilatedBoundingBox(boolean use3D) {
		super();
		this.use3D = use3D;
	}

	private BoundingBox findIntersectionOfDilatedBoundingBox( ObjMask om1, ObjMask om2, Extent e ) {
	
		// Grow each bounding box
		BoundingBox bbox1 = om1.getVoxelBoxBounded().dilate( use3D, e );
		BoundingBox bbox2 = om2.getVoxelBoxBounded().dilate( use3D, e );
		
		// Find the intersection
		return bbox1.intersectCreateNew(bbox2, e );
	}
	


	@Override
	protected BoundingBox execute(FeatureObjMaskPairParams params)
			throws ExecuteException {
		return findIntersectionOfDilatedBoundingBox(
			params.getObjMask1(),
			params.getObjMask2(),
			params.getNrgStack().getDimensions().getExtnt()
		);
	}
	
	@Override
	public boolean equals(Object obj) {
		 if(obj instanceof CalculateIntersectionOfDilatedBoundingBox){
			 final CalculateIntersectionOfDilatedBoundingBox other = (CalculateIntersectionOfDilatedBoundingBox) obj;
		        return new EqualsBuilder()
		            .append(use3D, other.use3D)
		            .isEquals();
	    } else{
	        return false;
	    }
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(use3D).toHashCode();
	}

}