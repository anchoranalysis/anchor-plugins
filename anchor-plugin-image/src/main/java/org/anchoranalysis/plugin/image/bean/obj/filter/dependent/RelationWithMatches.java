package org.anchoranalysis.plugin.image.bean.obj.filter.dependent;

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


import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.relation.RelationToValue;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.feature.session.calculator.FeatureCalculatorSingle;
import org.anchoranalysis.feature.session.calculator.cached.FeatureCalculatorCachedSingle;
import org.anchoranalysis.image.bean.objmask.match.ObjMaskMatcher;
import org.anchoranalysis.image.extent.ImageDim;
import org.anchoranalysis.image.feature.bean.evaluator.FeatureEvaluator;
import org.anchoranalysis.image.feature.objmask.FeatureInputSingleObj;
import org.anchoranalysis.image.objectmask.ObjectMask;
import org.anchoranalysis.image.objectmask.MatchedObject;
import org.anchoranalysis.image.objectmask.ObjectCollection;
import org.anchoranalysis.plugin.image.bean.obj.filter.ObjectFilterRelation;

/**
 * Matches each object with others, and keeps only those where a relation holds true for all matches (in terms of features)
 * 
 * @author Owen Feehan
 *
 */
public class RelationWithMatches extends ObjectFilterRelation {

	// START BEAN PROPERTIES
	@BeanField
	private FeatureEvaluator<FeatureInputSingleObj> featureEvaluator;
	
	@BeanField @OptionalBean
	private FeatureEvaluator<FeatureInputSingleObj> featureEvaluatorMatch;		// Optionally uses a different evaluator for the matched objects
	
	@BeanField
	private ObjMaskMatcher objMaskMatcher;
	
	/** Size of feature evaluation cache for featureEvaluatorMatch */
	@BeanField
	private int cacheSize = 50;
	// END BEAN PROPERTIES
	
	private FeatureCalculatorSingle<FeatureInputSingleObj> evaluatorForMatch;
	private FeatureCalculatorSingle<FeatureInputSingleObj> evaluatorForSource;
	private Map<ObjectMask,ObjectCollection> matches;
		
	@Override
	protected void start(Optional<ImageDim> dim, ObjectCollection objsToFilter) throws OperationFailedException {
		super.start(dim, objsToFilter);

		setupEvaluators();
		
		matches = createMatchesMap(
			objMaskMatcher.findMatch(objsToFilter)
		);
		
		// If any object has no matches, throw an exception
		if (matches.values().stream().anyMatch(ObjectCollection::isEmpty)) {
			throw new OperationFailedException("No matches found for at least one object");
		}
	}
	
	private void setupEvaluators() throws OperationFailedException {
		
		evaluatorForSource = featureEvaluator.createAndStartSession();

		// Use a specific evaluator for matching if defined, otherwise reuse the existing evaluator
		// and cache it so we don't have to repeatedly calculate on the same object
		evaluatorForMatch = new FeatureCalculatorCachedSingle<>(
			featureEvaluatorMatch!=null ? featureEvaluatorMatch.createAndStartSession() : evaluatorForSource,
			false,
			cacheSize
		);
		
	}
	
	@Override
	protected boolean match(ObjectMask om, Optional<ImageDim> dim, RelationToValue relation) throws OperationFailedException {
		try {
			double val = evaluatorForSource.calc(
				new FeatureInputSingleObj(om)
			);
			return doesMatchAllAssociatedObjects(
				val,
				matches.get(om),
				relation
			);
		} catch (FeatureCalcException e) {
			throw new OperationFailedException(e);
		}
	}
	
	@Override
	protected void end() throws OperationFailedException {
		super.end();
		evaluatorForMatch = null;
		evaluatorForSource = null;
		matches = null;
	}
			
	private boolean doesMatchAllAssociatedObjects( double val, ObjectCollection matches, RelationToValue relation ) throws FeatureCalcException {
		
		for( ObjectMask match : matches ) {
			
			double valMatch = evaluatorForMatch.calc(
				new FeatureInputSingleObj(match)
			);
			
			if (!relation.isRelationToValueTrue(val, valMatch)) {
				return false;
			}
		}
		return true;
	}
	
	private static Map<ObjectMask,ObjectCollection> createMatchesMap( List<MatchedObject> list ) {
		return list.stream()
	      .collect(Collectors.toMap(
    		 MatchedObject::getSourceObj,
    		 MatchedObject::getMatches
    	  )
    	);
	}

	public ObjMaskMatcher getObjMaskMatcher() {
		return objMaskMatcher;
	}

	public void setObjMaskMatcher(ObjMaskMatcher objMaskMatcher) {
		this.objMaskMatcher = objMaskMatcher;
	}

	public int getCacheSize() {
		return cacheSize;
	}

	public void setCacheSize(int cacheSize) {
		this.cacheSize = cacheSize;
	}

	public FeatureEvaluator<FeatureInputSingleObj> getFeatureEvaluator() {
		return featureEvaluator;
	}

	public void setFeatureEvaluator(FeatureEvaluator<FeatureInputSingleObj> featureEvaluator) {
		this.featureEvaluator = featureEvaluator;
	}

	public FeatureEvaluator<FeatureInputSingleObj> getFeatureEvaluatorMatch() {
		return featureEvaluatorMatch;
	}

	public void setFeatureEvaluatorMatch(
			FeatureEvaluator<FeatureInputSingleObj> featureEvaluatorMatch) {
		this.featureEvaluatorMatch = featureEvaluatorMatch;
	}
}
