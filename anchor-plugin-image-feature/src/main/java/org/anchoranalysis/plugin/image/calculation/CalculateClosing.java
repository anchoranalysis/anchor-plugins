package org.anchoranalysis.plugin.image.calculation;

import java.util.Optional;

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


import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.feature.cache.calculation.CalculationResolver;
import org.anchoranalysis.feature.cache.calculation.FeatureCalculation;
import org.anchoranalysis.feature.cache.calculation.ResolvedCalculation;
import org.anchoranalysis.feature.cache.calculation.ResolvedCalculationMap;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.extent.ImageDim;
import org.anchoranalysis.image.feature.objmask.FeatureInputSingleObj;
import org.anchoranalysis.image.objectmask.ObjectMask;
import org.anchoranalysis.image.objectmask.morph.MorphologicalErosion;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

public class CalculateClosing extends FeatureCalculation<ObjectMask,FeatureInputSingleObj> {

	private int iterations;
	private ResolvedCalculationMap<ObjectMask,FeatureInputSingleObj,Integer> mapDilation;
	private boolean do3D;
	
	private CalculateClosing(
		int iterations,
		ResolvedCalculationMap<ObjectMask,FeatureInputSingleObj,Integer> mapDilation,
		boolean do3D
	) {
		this.iterations = iterations;
		this.mapDilation = mapDilation;
		this.do3D = do3D;
	}
	
	private CalculateClosing( CalculateClosing src ) {
		this.iterations = src.iterations;
		this.mapDilation = src.mapDilation;
		this.do3D = src.do3D;
	}
	
	public static ResolvedCalculation<ObjectMask,FeatureInputSingleObj> createFromCache(
		CalculationResolver<FeatureInputSingleObj> cache,
		int iterations,
		boolean do3D
	) {
		ResolvedCalculationMap<ObjectMask,FeatureInputSingleObj,Integer> map = cache.search(
			new CalculateDilationMap(do3D)
		);
		
		return cache.search(
			new CalculateClosing(iterations, map, do3D)
		);
	}

	@Override
	protected ObjectMask execute(FeatureInputSingleObj params) throws FeatureCalcException {
		
		try {
			ObjectMask omDilated = mapDilation.getOrCalculate(params, iterations);
			
			ObjectMask omEroded = MorphologicalErosion.createErodedObjMask(
				omDilated,
				params.getDimensionsOptional().map(ImageDim::getExtnt),
				do3D,
				iterations,
				false,
				Optional.empty()
			);
			return omEroded;
			
		} catch (CreateException e) {
			throw new FeatureCalcException(e);
		}
	}

	@Override
	public boolean equals(final Object obj){
		 if(obj instanceof CalculateClosing){
		        final CalculateClosing other = (CalculateClosing) obj;
		        return new EqualsBuilder()
		            .append(iterations, other.iterations)
		            .append(mapDilation, other.mapDilation)
		            .append(do3D, other.do3D)
		            .isEquals();
		    } else{
		        return false;
		    }
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(iterations).append(mapDilation).append(do3D).toHashCode();
	}
}