package org.anchoranalysis.plugin.operator.feature.physical;

/*
 * #%L
 * anchor-feature
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
import org.anchoranalysis.feature.bean.operator.FeatureGenericSingleElem;
import org.anchoranalysis.feature.cache.SessionInput;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.feature.input.FeatureInput;

// 
public class ConvertUnits<T extends FeatureInput> extends FeatureGenericSingleElem<T> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	// START BEAN PROPERTIES
	@BeanField @AllowEmpty
	private String unitTypeFrom = "";
	
	@BeanField @AllowEmpty
	private String unitTypeTo = "";
	// END BEAN PROPERTIES

	public ConvertUnits() {
		
	}

	public String getUnitTypeTo() {
		return unitTypeTo;
	}

	public void setUnitTypeTo(String unitTypeTo) {
		this.unitTypeTo = unitTypeTo;
	}

	public String getUnitTypeFrom() {
		return unitTypeFrom;
	}

	public void setUnitTypeFrom(String unitTypeFrom) {
		this.unitTypeFrom = unitTypeFrom;
	}

	@Override
	protected double calc(SessionInput<T> input) throws FeatureCalcException {
		
		double value = input.calc( getItem() );
		
		SpatialConversionUtilities.UnitSuffix typeFrom = SpatialConversionUtilities.suffixFromMeterString(unitTypeFrom);
		SpatialConversionUtilities.UnitSuffix typeTo = SpatialConversionUtilities.suffixFromMeterString(unitTypeTo);
		
		double valueBaseUnits = SpatialConversionUtilities.convertFromUnits( value, typeFrom );
		return SpatialConversionUtilities.convertToUnits( valueBaseUnits, typeTo );
	}
}