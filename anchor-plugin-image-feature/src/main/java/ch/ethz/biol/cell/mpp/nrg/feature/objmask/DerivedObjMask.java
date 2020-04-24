package ch.ethz.biol.cell.mpp.nrg.feature.objmask;





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
import org.anchoranalysis.feature.bean.Feature;
import org.anchoranalysis.feature.cache.SessionInput;
import org.anchoranalysis.feature.cache.calculation.CacheableCalculation;
import org.anchoranalysis.feature.cache.calculation.CalculationResolver;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.feature.calc.params.FeatureInput;
import org.anchoranalysis.image.feature.bean.objmask.FeatureObjMask;
import org.anchoranalysis.image.feature.objmask.FeatureInputSingleObj;
import org.anchoranalysis.image.objmask.ObjMask;
import org.anchoranalysis.plugin.image.feature.obj.pair.CalculateInputFromDelegateOption;

public abstract class DerivedObjMask extends FeatureObjMask {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// START BEAN PROPERTIES
	@BeanField
	private double emptyValue = 255;
	
	@BeanField
	private Feature<FeatureInputSingleObj> item;
	// END BEAN PROPERTIES

	@Override
	public double calc(SessionInput<FeatureInputSingleObj> input) throws FeatureCalcException {

		return CalculateInputFromDelegateOption.calc(
			input,
			createCachedCalculationForDerived(
				input.resolverForChild( cacheName(), FeatureInputSingleObj.class )		
			),
			delegate -> new CalculateObjForDerived(delegate),
			item,
			cacheName(),
			emptyValue
		);
	}
	
	@Override
	public SessionInput<FeatureInput> transformInput(
			SessionInput<FeatureInputSingleObj> params,
			Feature<FeatureInput> dependentFeature
	) throws FeatureCalcException {
		//try {
			/*ObjMask omDerived = derivedObjMask(params);
			
			if (omDerived==null || !omDerived.hasPixelsGreaterThan(0)) {
				// TODO is this the correct way of handling a null-mask?
				assert(false);
				// Broken, please fix
				return null;
			}
			
			return params.mapParams(
				p -> createDerivedParams(p, omDerived ),
				cacheName()
			);*/
			// TODO broken fix
			return null;
		/*} catch (ExecuteException e) {
			throw new FeatureCalcException(e.getCause());
		}*/			
	}
	
	protected abstract CacheableCalculation<ObjMask,FeatureInputSingleObj> createCachedCalculationForDerived( CalculationResolver<FeatureInputSingleObj> session ) throws FeatureCalcException;
	
	protected abstract String cacheName();
	
	public double getEmptyValue() {
		return emptyValue;
	}

	public void setEmptyValue(double emptyValue) {
		this.emptyValue = emptyValue;
	}

	public Feature<FeatureInputSingleObj> getItem() {
		return item;
	}

	public void setItem(Feature<FeatureInputSingleObj> item) {
		this.item = item;
	}
}
