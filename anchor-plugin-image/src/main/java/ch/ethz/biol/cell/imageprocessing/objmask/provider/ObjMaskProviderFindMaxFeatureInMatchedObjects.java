package ch.ethz.biol.cell.imageprocessing.objmask.provider;

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


import java.util.List;
import java.util.Optional;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.feature.session.calculator.FeatureCalculatorSingle;
import org.anchoranalysis.image.bean.object.ObjectMatcher;
import org.anchoranalysis.image.feature.object.input.FeatureInputSingleObject;
import org.anchoranalysis.image.object.MatchedObject;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.image.object.ObjectCollectionFactory;
import org.anchoranalysis.image.object.ObjectMask;

// Returns a collection of each Max Object found in matches
public class ObjMaskProviderFindMaxFeatureInMatchedObjects extends ObjMaskProviderFindMaxFeatureBase {

	// START BEAN PROPERTIES
	@BeanField
	private ObjectMatcher objMaskMatcher;
	// END BEAN PROPERTIES

	@Override
	public ObjectCollection createFromObjs( ObjectCollection in ) throws CreateException {
		
		FeatureCalculatorSingle<FeatureInputSingleObject> session = createSession();

		try {
			List<MatchedObject> listMatches = objMaskMatcher.findMatch(in);

			return ObjectCollectionFactory.mapFromOptional(
				listMatches,
				owm -> findMax(session, owm.getMatches()) 
			);
			
		} catch (OperationFailedException | FeatureCalcException e) {
			throw new CreateException(e);
		}
	}
	
	private Optional<ObjectMask> findMax( FeatureCalculatorSingle<FeatureInputSingleObject> session, ObjectCollection objs ) throws FeatureCalcException {
		Optional<ObjectMask> max = Optional.empty();
		double maxVal = 0;
		
		for( ObjectMask om : objs ) {
			
			double featureVal = session.calc(
				new FeatureInputSingleObject(om)
			);
			
			if (!max.isPresent() || featureVal>maxVal) {
				max = Optional.of(om);
				maxVal = featureVal;
			}
		}
		
		return max;
	}

	public ObjectMatcher getObjMaskMatcher() {
		return objMaskMatcher;
	}

	public void setObjMaskMatcher(ObjectMatcher objMaskMatcher) {
		this.objMaskMatcher = objMaskMatcher;
	}
}
