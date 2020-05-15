package ch.ethz.biol.cell.imageprocessing.objmask.provider;

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
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.feature.session.calculator.FeatureCalculatorSingle;
import org.anchoranalysis.image.feature.objmask.FeatureInputSingleObj;
import org.anchoranalysis.image.objmask.ObjMask;
import org.anchoranalysis.image.objmask.ObjMaskCollection;

public class ObjMaskProviderFindMaxFeature extends ObjMaskProviderFindMaxFeatureBase {

	@Override
	public ObjMaskCollection createFromObjs( ObjMaskCollection objMaskCollection ) throws CreateException {

		Optional<ObjMask> max = findMaxObj(
			createSession(),
			objMaskCollection
		);
		
		ObjMaskCollection out = new ObjMaskCollection();
		max.ifPresent( obj->
			out.add(obj)
		);
		return out;
	}
	
	private Optional<ObjMask> findMaxObj( FeatureCalculatorSingle<FeatureInputSingleObj> session, ObjMaskCollection in ) throws CreateException {
		
		try {
			ObjMask max = null;
			
			double maxVal = 0;
			for( ObjMask om : in ) {
				
				double featureVal = session.calc(
					new FeatureInputSingleObj(om)
				);
				
				if (max==null || featureVal>maxVal) {
					max = om;
					maxVal = featureVal;
				}
			}
			
			return Optional.ofNullable(max);
			
		} catch (FeatureCalcException e) {
			throw new CreateException(e);
		}
	}
}
