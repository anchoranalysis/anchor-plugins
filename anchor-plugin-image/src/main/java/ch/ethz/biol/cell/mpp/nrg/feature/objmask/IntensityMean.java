package ch.ethz.biol.cell.mpp.nrg.feature.objmask;

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


import java.nio.ByteBuffer;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.geometry.Point3i;
import org.anchoranalysis.feature.cache.SessionInput;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.chnl.Chnl;
import org.anchoranalysis.image.extent.BoundingBox;
import org.anchoranalysis.image.feature.bean.objmask.FeatureObjMask;
import org.anchoranalysis.image.feature.objmask.FeatureInputSingleObj;
import org.anchoranalysis.image.objmask.ObjMask;
import org.anchoranalysis.image.voxel.box.VoxelBoxWrapper;
import org.anchoranalysis.image.voxel.buffer.VoxelBuffer;

public class IntensityMean extends FeatureObjMask {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// START BEAN PROPERTIES
	@BeanField
	private int nrgIndex = 0;
	// END BEAN PROPERTIES
	
	public static double calcMeanIntensityObjMask( Chnl chnl, ObjMask om ) throws FeatureCalcException {
		return calcMeanIntensityObjMask(chnl, om, false);
	}
	
	public static double calcMeanIntensityObjMask( Chnl chnl, ObjMask om, boolean excludeZero ) throws FeatureCalcException {
		
		if (!chnl.getDimensions().getExtnt().contains(om.getBoundingBox())) {
			throw new FeatureCalcException(
				String.format(
					"The object's bounding-box (%s) is not contained within the dimensions of the channel %s",
					om.getBoundingBox(),
					chnl.getDimensions().getExtnt()
				)
			);
		}
		
		VoxelBoxWrapper vbIntens = chnl.getVoxelBox();
		
		BoundingBox bbox = om.getBoundingBox();
		
		Point3i crnrMin = bbox.getCrnrMin();
		Point3i crnrMax = bbox.calcCrnrMax();
		
		double sum = 0.0;
		int cnt = 0;
		
		for( int z=crnrMin.getZ(); z<=crnrMax.getZ(); z++) {
			
			VoxelBuffer<?> bbIntens = vbIntens.any().getPixelsForPlane( z );
			ByteBuffer bbMask = om.getVoxelBox().getPixelsForPlane( z - crnrMin.getZ() ).buffer();
			
			int offsetMask = 0;
			for( int y=crnrMin.getY(); y<=crnrMax.getY(); y++) {
				for( int x=crnrMin.getX(); x<=crnrMax.getX(); x++) {
				
					if (bbMask.get(offsetMask)==om.getBinaryValuesByte().getOnByte()) {
						int offsetIntens = vbIntens.any().extnt().offset(x, y);
						
						int val = bbIntens.getInt(offsetIntens);
						
						if(excludeZero && val==0) {
							offsetMask++;
							continue;
						}
						
						sum += val;
						cnt++;
					}
							
					offsetMask++;
				}
			}
		}
		
		if (cnt==0) {
			throw new FeatureCalcException("There are 0 pixels in the object-mask");
		}
		
		return sum/cnt;
	}
	

	@Override
	public double calc(SessionInput<FeatureInputSingleObj> paramsCacheable) throws FeatureCalcException {
		
		FeatureInputSingleObj params = paramsCacheable.getParams();
		
		Chnl chnl = params.getNrgStack().getNrgStack().getChnl(nrgIndex);
		return calcMeanIntensityObjMask(chnl, params.getObjMask() );
	}

	public int getNrgIndex() {
		return nrgIndex;
	}

	public void setNrgIndex(int nrgIndex) {
		this.nrgIndex = nrgIndex;
	}

}
