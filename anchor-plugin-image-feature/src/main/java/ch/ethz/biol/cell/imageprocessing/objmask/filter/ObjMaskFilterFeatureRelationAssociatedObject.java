package ch.ethz.biol.cell.imageprocessing.objmask.filter;

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
import java.util.Optional;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.bean.shared.relation.RelationBean;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.feature.session.calculator.FeatureCalculatorSingle;
import org.anchoranalysis.image.bean.objmask.filter.ObjMaskFilter;
import org.anchoranalysis.image.bean.objmask.match.ObjMaskMatcher;
import org.anchoranalysis.image.extent.ImageDim;
import org.anchoranalysis.image.feature.bean.evaluator.FeatureEvaluator;
import org.anchoranalysis.image.feature.objmask.FeatureInputSingleObj;
import org.anchoranalysis.image.objectmask.ObjectMask;
import org.anchoranalysis.image.objectmask.ObjectCollection;
import org.anchoranalysis.image.objmask.match.ObjWithMatches;

// Relation must hold true for all associated objects
public class ObjMaskFilterFeatureRelationAssociatedObject extends ObjMaskFilter {

	// START BEAN PROPERTIES
	@BeanField
	private FeatureEvaluator<FeatureInputSingleObj> featureEvaluator;
	
	@BeanField @OptionalBean
	private FeatureEvaluator<FeatureInputSingleObj> featureEvaluatorMatch;		// Optionally uses a different evaluator for the matched objects
	
	@BeanField
	private ObjMaskMatcher objMaskMatcher;
	
	@BeanField
	private RelationBean relation;
	
	@BeanField
	private int cacheSize = 10;			// Cache uses on featureEvaluatorMatch so we don't have to repeatedly calculate on the same object
	// END BEAN PROPERTIES
	
	private FeatureCalculatorSingle<FeatureInputSingleObj> evaluatorForMatch;
	private FeatureCalculatorSingle<FeatureInputSingleObj> featureSession;
	
	@Override
	public void filter(
		ObjectCollection objs,
		Optional<ImageDim> dim,
		Optional<ObjectCollection> objsRejected
	) throws OperationFailedException {

		start();
				
		List<ObjWithMatches> matchList = objMaskMatcher.findMatch(objs);
		
		for( ObjWithMatches owm : matchList ) {
			if (owm.getMatches().size()==0) {
				throw new OperationFailedException("No matching object found");
			}
		}
	
		try {
			for( ObjWithMatches owm : matchList ) {
				
				if (!match(owm.getSourceObj(), owm.getMatches())) {
					objs.remove(owm.getSourceObj());
					
					if (objsRejected.isPresent()) {
						objsRejected.get().add(owm.getSourceObj());
					}
				}
				
			}
		} catch (FeatureCalcException e) {
			throw new OperationFailedException(e);
		}
		
		// We free up calculations
		// TODO previously this is where we cleared the cache
	}
	
	private boolean match(ObjectMask om, ObjectCollection matches) throws FeatureCalcException {
		double val = featureSession.calc(
			new FeatureInputSingleObj(om)
		);
		return doesMatchAllAssociatedObjects(val,matches);
	}
	
	private void start() throws OperationFailedException {

		featureSession = featureEvaluator.createAndStartSession();

		// TODO this previously used FeatureEvaluatorNrgStackCache and shoudld be cached. Now it isn't.
		if (featureEvaluatorMatch!=null) {
			evaluatorForMatch = featureEvaluatorMatch.createAndStartSession();
		} else {
			evaluatorForMatch = featureSession;
		}
	}
		
	private boolean doesMatchAllAssociatedObjects( double val, ObjectCollection matches ) throws FeatureCalcException {
		
		for( ObjectMask match : matches ) {
			
			double valMatch = evaluatorForMatch.calc(
				new FeatureInputSingleObj(match)
			);
			
			//System.out.printf("Matching %f against %f\n", val, valMatch);
			
			if (!relation.create().isRelationToValueTrue(val, valMatch)) {
				return false;
			}
		}
		return true;
	}

	public RelationBean getRelation() {
		return relation;
	}

	public void setRelation(RelationBean relation) {
		this.relation = relation;
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
