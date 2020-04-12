package ch.ethz.biol.cell.mpp.nrg.feature.operator;

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


import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.core.params.KeyValueParams;
import org.anchoranalysis.feature.bean.Feature;
import org.anchoranalysis.feature.cache.CacheableParams;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.feature.calc.params.FeatureCalcParams;
import org.anchoranalysis.feature.calc.params.FeatureCalcParamsWithImageParamsDescriptor;
import org.anchoranalysis.feature.init.FeatureInitParams;
import org.anchoranalysis.feature.params.FeatureParamsDescriptor;

// TODO this behaviour is very ill-defined with parameter type. Clarify
public class Param extends Feature<FeatureCalcParams> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	// START BEAN PROPERTIES
	@BeanField
	private String id;
	// END BEAN PROPERTIES
	
	private KeyValueParams keyValueParams;
	
	@Override
	public void beforeCalc(FeatureInitParams params) throws InitException {
		super.beforeCalc(params);
		this.keyValueParams = params.getKeyValueParams();
	}
	
	@Override
	public double calc(CacheableParams<FeatureCalcParams> params)
			throws FeatureCalcException {

//		if (keyValueParams instanceof FeatureCalcParamsWithImageParams) {
//			
//			FeatureCalcParamsWithImageParams paramsCast = (FeatureCalcParamsWithImageParams) params;
//			
//			NRGElemParamsFromImage imageParams = paramsCast.getParamsImage();
//			if (imageParams.containsKey(id)) {
//				return imageParams.get(id);
//			} else {
//				return Double.NaN;
//			}
//			
//		}
		
		if (keyValueParams==null) {
			throw new FeatureCalcException("No KeyValueParams is passed");
		}
		
		if (keyValueParams.containsKey(id)) {
			return Double.parseDouble( keyValueParams.getProperty(id) );
		} else {
			throw new FeatureCalcException(
				String.format("Param '%s' is missing", id)	
			);
		}
		
		
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	@Override
	public FeatureParamsDescriptor paramType()
			throws FeatureCalcException {
		return FeatureCalcParamsWithImageParamsDescriptor.instance;
	}
}
