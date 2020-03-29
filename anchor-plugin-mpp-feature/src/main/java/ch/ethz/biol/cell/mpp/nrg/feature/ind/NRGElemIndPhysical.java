package ch.ethz.biol.cell.mpp.nrg.feature.ind;

import org.anchoranalysis.anchor.mpp.feature.bean.nrg.elem.NRGElemInd;

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


import org.anchoranalysis.bean.annotation.AllowEmpty;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.unit.SpatialConversionUtilities;
import org.anchoranalysis.image.convert.ImageUnitConverter;
import org.anchoranalysis.image.extent.ImageRes;
import org.anchoranalysis.image.orientation.DirectionVector;

public abstract class NRGElemIndPhysical extends NRGElemInd {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// START BEAN PROPERTIES
	@BeanField
	private boolean physical = false;
	
	@BeanField @AllowEmpty
	private String unitType = "";
	// END BEAN PROPERTIES
	
	public boolean isPhysical() {
		return physical;
	}

	public void setPhysical(boolean physical) {
		this.physical = physical;
	}
	
	protected double rslvDistance( double value, ImageRes res, DirectionVector dirVector ) {
		
		// If we aren't doing anything physical, we can just return the current value
		if (!physical) {
			return value;
		}
		
		double valuePhysical = ImageUnitConverter.convertToPhysicalDistance(value, res, dirVector);
		
		SpatialConversionUtilities.UnitSuffix prefixType = SpatialConversionUtilities.suffixFromMeterString(unitType);
		return SpatialConversionUtilities.convertToUnits( valuePhysical, prefixType );
	}
	
	protected double rslvVolume( double value, ImageRes res ) {
		
		// If we aren't doing anything physical, we can just return the current value
		if (!physical) {
			return value;
		}
		
		double valuePhysical = ImageUnitConverter.convertToPhysicalVolume(value, res);
		
		SpatialConversionUtilities.UnitSuffix prefixType = SpatialConversionUtilities.suffixFromMeterString(unitType);
		return SpatialConversionUtilities.convertToUnits( valuePhysical, prefixType );
	}
	
	protected double rslvArea( double value, ImageRes res ) {
		
		// If we aren't doing anything physical, we can just return the current value
		if (!physical) {
			return value;
		}
		
		double valuePhysical = ImageUnitConverter.convertToPhysicalArea(value, res);
		
		SpatialConversionUtilities.UnitSuffix prefixType = SpatialConversionUtilities.suffixFromMeterString(unitType);
		return SpatialConversionUtilities.convertToUnits( valuePhysical, prefixType );
	}
	
	public String getUnitType() {
		return unitType;
	}

	public void setUnitType(String unitType) {
		this.unitType = unitType;
	}

}
