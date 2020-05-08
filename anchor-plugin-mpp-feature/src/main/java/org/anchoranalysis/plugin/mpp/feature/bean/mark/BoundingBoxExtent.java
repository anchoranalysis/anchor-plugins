package org.anchoranalysis.plugin.mpp.feature.bean.mark;

import java.util.Optional;

import org.anchoranalysis.anchor.mpp.feature.bean.mark.FeatureInputMark;
import org.anchoranalysis.anchor.mpp.feature.bean.mark.FeatureMark;
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
import org.anchoranalysis.feature.cache.SessionInput;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.extent.BoundingBox;
import org.anchoranalysis.image.extent.ImageDim;
import org.anchoranalysis.image.extent.ImageRes;
import org.anchoranalysis.image.orientation.DirectionVector;
import org.anchoranalysis.plugin.mpp.feature.bean.unit.UnitConverter;

public class BoundingBoxExtent extends FeatureMark {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// START BEAN PARAMETERS
	@BeanField
	private String axis = "x";
	
	@BeanField
	private int regionID = GlobalRegionIdentifiers.SUBMARK_INSIDE;
	
	@BeanField
	private UnitConverter unit = new UnitConverter();
	// END BEAN PARAMETERS
	

	@Override
	public double calc(SessionInput<FeatureInputMark> input) throws FeatureCalcException {
		
		ImageDim dim = input.get().getDimensionsRequired();
		
		BoundingBox bbox = input.get().getMark().bbox(dim, regionID);
		
		return rslvDistance(
			bbox,
			Optional.of(dim.getRes()),
			dimIndex()
		);
	}
	
	/** Translates a string describing the axis to the index of which dimension (X->0, Y->1, Z->2) */
	private int dimIndex() throws FeatureCalcException {
		String axisLowerCase = axis.toLowerCase();
		if (axisLowerCase.equals("x")) {
			return 0;
			
		} else if (axisLowerCase.equals("y")) {
			return 1;
			
		} else if (axisLowerCase.equals("z")) {
			return 2;
			
		} else {
			throw new FeatureCalcException(
				String.format("Axis must be x, y or z, instead a different value is specified: %s", axisLowerCase)
			);
		}
	}
	
	private double rslvDistance(BoundingBox bbox, Optional<ImageRes> res, int dimIndex) throws FeatureCalcException {
		return unit.rslvDistance(
			bbox.extnt().getIndex(dimIndex),
			res,
			unitVector(dimIndex)
		);
	}
	
	private DirectionVector unitVector(int dimIndex) {
		DirectionVector dirVector = new DirectionVector();
		dirVector.setIndex(dimIndex, 1);
		return dirVector;
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

	public UnitConverter getUnit() {
		return unit;
	}

	public void setUnit(UnitConverter unit) {
		this.unit = unit;
	}
}
