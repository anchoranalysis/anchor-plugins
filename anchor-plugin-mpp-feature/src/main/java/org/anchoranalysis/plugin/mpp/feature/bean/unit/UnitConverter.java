package org.anchoranalysis.plugin.mpp.feature.bean.unit;

import java.util.Optional;

import org.anchoranalysis.bean.AnchorBean;

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
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.convert.ImageUnitConverter;
import org.anchoranalysis.image.extent.ImageResolution;
import org.anchoranalysis.image.orientation.DirectionVector;

/**
 * Converts units (distance, area, volume) to a another representation (e.g. physical units)
 * 
 * @author Owen Feehan
 *
 */
public class UnitConverter extends AnchorBean<UnitConverter> {

	// START BEAN PROPERTIES
	@BeanField
	private boolean physical = false;
	
	@BeanField @AllowEmpty
	private String unitType = "";
	// END BEAN PROPERTIES
	
	
	/**
	 * Convert distance into another unit representation
	 * 
	 * @param value distance expressed as number of voxels
	 * @param res image-resolution
	 * @return distance expressed in desired units
	 * @throws FeatureCalcException
	 */
	public double rslvDistance( double value, Optional<ImageResolution> res, DirectionVector dirVector ) throws FeatureCalcException {
		
		// If we aren't doing anything physical, we can just return the current value
		if (isNonPhysical(res)) {
			return value;
		}
		
		double valuePhysical = ImageUnitConverter.convertToPhysicalDistance(value, res.get(), dirVector);
		SpatialConversionUtilities.UnitSuffix prefixType = SpatialConversionUtilities.suffixFromMeterString(unitType);
		
		return SpatialConversionUtilities.convertToUnits( valuePhysical, prefixType );
	}
	
	
	/**
	 * Convert volume into another unit representation
	 * 
	 * @param value volume as number of voxels
	 * @param res image-resolution
	 * @return volume expressed in desired units
	 * @throws FeatureCalcException
	 */
	public double rslvVolume( double value, Optional<ImageResolution> res ) throws FeatureCalcException {
		
		// If we aren't doing anything physical, we can just return the current value
		if (isNonPhysical(res)) {
			return value;
		}
		
		double valuePhysical = ImageUnitConverter.convertToPhysicalVolume(value, res.get() );
		
		SpatialConversionUtilities.UnitSuffix prefixType = SpatialConversionUtilities.suffixFromMeterString(unitType);
		return SpatialConversionUtilities.convertToUnits( valuePhysical, prefixType );
	}
	
	/**
	 * Convert area into another unit representation
	 * 
	 * @param value area as number of voxels
	 * @param res image-resolution
	 * @return area expressed in desired units
	 * @throws FeatureCalcException
	 */
	public double rslvArea( double value, Optional<ImageResolution> res ) throws FeatureCalcException {
		
		// If we aren't doing anything physical, we can just return the current value
		if (isNonPhysical(res)) {
			return value;
		}
		
		double valuePhysical = ImageUnitConverter.convertToPhysicalArea(value, res.get() );
		
		SpatialConversionUtilities.UnitSuffix prefixType = SpatialConversionUtilities.suffixFromMeterString(unitType);
		return SpatialConversionUtilities.convertToUnits( valuePhysical, prefixType );
	}
	
	/** 
	 *  Do we desire to convert to NON-physical co-ordinates? If it's physical, check that image-resolution is present as required.
	 * 
	 *  @throws FeatureCalcException if physical is set, but the resolution is not
	 *  @return true iff the target-units for conversion are non-physical
	 * */
	private boolean isNonPhysical( Optional<ImageResolution> res ) throws FeatureCalcException {
		if (physical) {
			checkResIsPresent(res);
			return false;
		} else {
			return true;
		}
	}
	
	private void checkResIsPresent( Optional<ImageResolution> res ) throws FeatureCalcException {
		if (!res.isPresent()) {
			throw new FeatureCalcException("Image-resolution is required for conversions to physical units, but it is not specified");
		}
	}
	
	public String getUnitType() {
		return unitType;
	}

	public void setUnitType(String unitType) {
		this.unitType = unitType;
	}

	public boolean isPhysical() {
		return physical;
	}

	public void setPhysical(boolean physical) {
		this.physical = physical;
	}
}
