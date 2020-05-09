package org.anchoranalysis.plugin.image.feature.bean.physical;

/*-
 * #%L
 * anchor-image-feature
 * %%
 * Copyright (C) 2010 - 2019 Owen Feehan, ETH Zurich, University of Zurich, Hoffmann la Roche
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
import org.anchoranalysis.feature.input.FeatureInputWithRes;
import org.anchoranalysis.image.bean.unitvalue.areavolume.UnitValueAreaOrVolume;
import org.anchoranalysis.image.bean.unitvalue.volume.UnitValueVolumeVoxels;
import org.anchoranalysis.image.extent.ImageRes;
import org.anchoranalysis.image.feature.bean.physical.FeatureSingleElemWithRes;
import org.anchoranalysis.image.unitvalue.UnitValueException;

/**
 * Checks if a value lies within a range defined by units (a minimum and maximum boundary)
 * 
 * @author Owen Feehan
 *
 * @param <T> feature input-type
 * 
 */
public class UnitsWithinRange<T extends FeatureInputWithRes> extends FeatureSingleElemWithRes<T> {

	// START BEAN PROPERTIES
	/** Returned as a constant if a value lies within the range */
	@BeanField
	private double within = 0;

	/** Returned as a constant if a value lies otside the range */
	@BeanField
	private double outside = -10;
	
	/** 
	 * Minimum-boundary for acceptable range 
	 * 
	 * We default to volume as units, but it could also be area. It's arbitrary for 0-value.
	 **/
	@BeanField
	private UnitValueAreaOrVolume min = new UnitValueVolumeVoxels( 0 );
	
	/** 
	 * Maximum-boundary for acceptable range 
	 * 
	 * We default to volume as units, but it could also be area. It's arbitrary for infinity-value.
	 **/
	@BeanField
	private UnitValueAreaOrVolume max = new UnitValueVolumeVoxels( Double.MAX_VALUE );
	// END BEAN PROPERTIES
	
	@Override
	protected double calcWithRes( double value, ImageRes res ) throws FeatureCalcException {
		
		try {
			double minVoxels = min.rslv(res);
			double maxVoxels = max.rslv(res);
					
			if (value >= minVoxels && value <= maxVoxels) {
				return within;
			} else {
				return outside;
			}
			
		} catch (UnitValueException e) {
			throw new FeatureCalcException(e);
		}
	}
	
	@Override
	public String getParamDscr() {
		return String.format(
			"min=%s,max=%s,within=%8.3f outside=%8.3f",
			min,
			max,
			within,
			outside
		);
	}

	public UnitValueAreaOrVolume getMin() {
		return min;
	}

	public void setMin(UnitValueAreaOrVolume min) {
		this.min = min;
	}

	public UnitValueAreaOrVolume getMax() {
		return max;
	}

	public void setMax(UnitValueAreaOrVolume max) {
		this.max = max;
	}

	public double getWithin() {
		return within;
	}

	public void setWithin(double within) {
		this.within = within;
	}

	public double getOutside() {
		return outside;
	}

	public void setOutside(double outside) {
		this.outside = outside;
	}
}
