package ch.ethz.biol.cell.mpp.nrg.feature.objmask;

import java.nio.ByteBuffer;

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


import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.axis.AxisType;
import org.anchoranalysis.core.axis.AxisTypeConverter;
import org.anchoranalysis.core.geometry.Point3d;
import org.anchoranalysis.core.geometry.Point3i;
import org.anchoranalysis.feature.cache.SessionInput;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.feature.bean.objmask.FeatureObjMask;
import org.anchoranalysis.image.feature.objmask.FeatureInputSingleObj;
import org.anchoranalysis.image.objmask.ObjMask;
import org.anchoranalysis.image.voxel.box.VoxelBox;

/**
 * Calculates deterministicly a point that is definitely inside the object mask. A selected axis is outputted.
 * 
 * TODO cache the point calculation
 * 
 * @author Owen Feehan
 *
 */
@SuppressWarnings("unused")
public class ArbitraryInsidePoint extends FeatureObjMask {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	// START BEAN PROPERTIES
	@BeanField
	private String axis = "x";
	
	@BeanField
	private double emptyValue = 0;
	// END BEAN PROPERTIES
	
	public ArbitraryInsidePoint() {
	}
	
	public ArbitraryInsidePoint( String axis ) {
		this.axis = axis;
	}
	
	@Override
	public double calc(SessionInput<FeatureInputSingleObj> input) throws FeatureCalcException {
				
		AxisType axisType = AxisTypeConverter.createFromString(axis);
		
		Point3i arbPoint = calcArbitraryPointWithinMask(
			input.get().getObjMask()
		);
	
		if (arbPoint==null) {
			return emptyValue;
		}
		
		return arbPoint.getValueByDimension(axisType);
	}

	public String getAxis() {
		return axis;
	}

	public void setAxis(String axis) {
		this.axis = axis;
	}

	public double getEmptyValue() {
		return emptyValue;
	}

	public void setEmptyValue(double emptyValue) {
		this.emptyValue = emptyValue;
	}
	
	// A deterministic way of getting a single-point that is always within the object mask
	//   as the centre-of-gravity is not guaranteed to be
	private static Point3i calcArbitraryPointWithinMask( ObjMask om ) {
		
		// First we try the mid point of the bounding box
		Point3i midPoint = om.getBoundingBox().centerOfGravity();
		if (om.contains(midPoint)) {
			return midPoint;
		}
		
		byte maskOn = om.binaryVoxelBox().getBinaryValues().createByte().getOnByte();
		
		VoxelBox<ByteBuffer> vb = om.getVoxelBox();
		
		for( int z=0; z<vb.extent().getZ(); z++ ) {
			
			ByteBuffer bb = vb.getPixelsForPlane(z).buffer();
			
			int offset = 0;
			for( int y=0; y<vb.extent().getY(); y++ ) {
				for( int x=0; x<vb.extent().getX(); x++ ) {
				
					if( bb.get(offset)==maskOn) {
						return new Point3i(
							x + om.getBoundingBox().getCrnrMin().getX(),
							y + om.getBoundingBox().getCrnrMin().getY(),
							z + om.getBoundingBox().getCrnrMin().getZ()
						);
					}
					
					offset++;
				}
			}
		}
		
		// If there are no on points
		return null;
	}

}
