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


import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.params.KeyValueParams;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.feature.calc.params.FeatureInputNRGStack;
import org.anchoranalysis.image.feature.bean.FeatureNRGStack;

public class NRGParam extends FeatureNRGStack {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	// START BEAN PROPERTIES
	@BeanField
	private String id;
	// END BEAN PROPERTIES
	
	static double calcForPropertyID( FeatureInputNRGStack params, String id ) throws FeatureCalcException {
		
		double val = calcWithMaybeNaN(params, id);
		
		if (Double.isNaN(val)) {
			throw new FeatureCalcException(
				String.format("Cannot find key: %s", id)	
			);
		}
		
		return val;
	}
	
	@Override
	public double calcCast(FeatureInputNRGStack params)
			throws FeatureCalcException {
		return calcForPropertyID( params, id );
	}
	
	private static double calcWithMaybeNaN( FeatureInputNRGStack params, String id ) throws FeatureCalcException {
		KeyValueParams kpv = params.getNrgStack().getParams();
		
		if (kpv==null) {
			throw new FeatureCalcException("NrgStack is missing params");
		}
		
		return kpv.getPropertyAsDouble(id);
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	@Override
	public String getParamDscr() {
		return id;
	}

}
