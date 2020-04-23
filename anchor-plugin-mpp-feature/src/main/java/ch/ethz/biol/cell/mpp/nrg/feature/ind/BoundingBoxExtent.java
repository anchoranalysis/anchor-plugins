package ch.ethz.biol.cell.mpp.nrg.feature.ind;

import org.anchoranalysis.anchor.mpp.feature.input.memo.FeatureInputSingleMemo;
import org.anchoranalysis.anchor.mpp.mark.GlobalRegionIdentifiers;

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
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.extent.BoundingBox;
import org.anchoranalysis.image.extent.ImageRes;
import org.anchoranalysis.image.orientation.DirectionVector;

// TODO we can also create a FeatureMark version 
public class BoundingBoxExtent extends NRGElemIndPhysical {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// START BEAN PARAMETERS
	@BeanField
	private String axis = "x";
	
	@BeanField
	private int regionID = GlobalRegionIdentifiers.SUBMARK_INSIDE;
	// END BEAN PARAMETERS
	
	@Override
	public double calcCast( FeatureInputSingleMemo params ) throws FeatureCalcException {
		
		BoundingBox bbox = params.getPxlPartMemo().getMark().bbox(params.getDimensions(), regionID);
		
		String axisLowerCase = axis.toLowerCase();
		
		return calcAxisValue(axisLowerCase, bbox, params.getDimensions().getRes() );
	}
	
	
	private double calcAxisValue( String axisLowerCase, BoundingBox bbox, ImageRes res) {
		
		if (axisLowerCase.equals("x")) {
			
			double extnt = bbox.extnt().getX();
			return rslvDistance(extnt, res, new DirectionVector(1,0,0) ); 
			
		} else if (axisLowerCase.equals("y")) {
			
			double extnt = bbox.extnt().getY();
			return rslvDistance(extnt, res, new DirectionVector(0,1,0) );
			
		} else if (axisLowerCase.equals("z")) {
			
			double extnt = bbox.extnt().getZ();
			return rslvDistance(extnt, res, new DirectionVector(0,1,0) );
			
		} else {
			assert false;
			return -1;
		}
	}
	
	@Override
	public String getParamDscr() {
		return String.format("%s", axis);
	}

	public String getAxis() {
		return axis;
	}

	public void setAxis(String axis) {
		this.axis = axis;
	}

	public int getRegionID() {
		return regionID;
	}


	public void setRegionID(int regionID) {
		this.regionID = regionID;
	}

}
