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

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.Optional;
import org.anchoranalysis.bean.shared.relation.RelationBean;
import org.anchoranalysis.core.cache.CacheMonitor;
import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.feature.session.SequentialSession;
import org.anchoranalysis.feature.session.FeatureCalculatorVector;
import org.anchoranalysis.feature.session.FeatureCalculatorVectorChangeParams;
import org.anchoranalysis.image.bean.objmask.filter.ObjMaskFilter;
import org.anchoranalysis.image.bean.objmask.match.ObjMaskMatcher;
import org.anchoranalysis.image.extent.ImageDim;
import org.anchoranalysis.image.feature.bean.evaluator.FeatureEvaluatorNrgStack;
import org.anchoranalysis.image.feature.evaluator.FeatureEvaluatorNrgStackCache;
import org.anchoranalysis.image.feature.objmask.FeatureObjMaskParams;
import org.anchoranalysis.image.feature.session.FeatureSessionCreateParamsSingle;
import org.anchoranalysis.image.objmask.ObjMask;
import org.anchoranalysis.image.objmask.ObjMaskCollection;
import org.anchoranalysis.image.objmask.match.ObjWithMatches;

// Relation must hold true for all associated objects
public class ObjMaskFilterFeatureRelationAssociatedObject extends ObjMaskFilter {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	// START BEAN PROPERTIES
	@BeanField
	private FeatureEvaluatorNrgStack<FeatureObjMaskParams> featureEvaluator;
	
	@BeanField @Optional
	private FeatureEvaluatorNrgStack<FeatureObjMaskParams> featureEvaluatorMatch;		// Optionally uses a different evaluator for the matched objects
	
	@BeanField
	private ObjMaskMatcher objMaskMatcher;
	
	@BeanField
	private RelationBean relation;
	
	@BeanField
	private int cacheSize = 10;			// Cache uses on featureEvaluatorMatch so we don't have to repeatedly calculate on the same object
	// END BEAN PROPERTIES
	
	private FeatureCalculatorVector<FeatureObjMaskParams> evaluatorForMatch;
	private FeatureCalculatorVector<FeatureObjMaskParams> featureSession;
		
	protected void start(ImageDim dim) throws OperationFailedException {

		featureSession = featureEvaluator.createAndStartSession();

		// TODO this previously used FeatureEvaluatorNrgStackCache and shoudld be cached. Now it isn't.
		if (featureEvaluatorMatch!=null) {
			evaluatorForMatch = featureEvaluatorMatch.createAndStartSession();
		} else {
			evaluatorForMatch = featureSession;
		}
	}
	
	
	private boolean doesMatchAllAssociatedObjects( double val, ObjMaskCollection matches ) throws FeatureCalcException {
		
		for( ObjMask match : matches ) {
			
			double valMatch = evaluatorForMatch.calc(
				new FeatureObjMaskParams(match)
			).get(0);
			
			//System.out.printf("Matching %f against %f\n", val, valMatch);
			
			if (!relation.create().isRelationToValueTrue(val, valMatch)) {
				return false;
			}
		}
		return true;
	}
	
	

	@Override
	public void filter(ObjMaskCollection objs, ImageDim dim, ObjMaskCollection objsRejected)
			throws OperationFailedException {

		start(dim);
				
		List<ObjWithMatches> matchList = objMaskMatcher.findMatch(objs);
		
		for( ObjWithMatches owm : matchList ) {
			if (owm.getMatches().size()==0) {
				throw new OperationFailedException("No matching object found");
			}
		}
	
		try {
			for( ObjWithMatches owm : matchList ) {
				
				if (!match(owm.getSourceObj(),dim,owm.getMatches())) {
					objs.remove(owm.getSourceObj());
					
					if (objsRejected!=null) {
						objsRejected.add(owm.getSourceObj());
					}
				}
				
			}
		} catch (FeatureCalcException e) {
			throw new OperationFailedException(e);
		}
		
		// We free up calculations
		// TODO previously this is where we cleared the cache
	}
	
	protected boolean match(ObjMask om, ImageDim dim, ObjMaskCollection matches) throws FeatureCalcException {
		double val = featureSession.calc(
			new FeatureObjMaskParams(om)
		).get(0);
		return doesMatchAllAssociatedObjects(val,matches);
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

	public FeatureEvaluatorNrgStack<FeatureObjMaskParams> getFeatureEvaluator() {
		return featureEvaluator;
	}

	public void setFeatureEvaluator(FeatureEvaluatorNrgStack<FeatureObjMaskParams> featureEvaluator) {
		this.featureEvaluator = featureEvaluator;
	}

	public FeatureEvaluatorNrgStack<FeatureObjMaskParams> getFeatureEvaluatorMatch() {
		return featureEvaluatorMatch;
	}

	public void setFeatureEvaluatorMatch(
			FeatureEvaluatorNrgStack<FeatureObjMaskParams> featureEvaluatorMatch) {
		this.featureEvaluatorMatch = featureEvaluatorMatch;
	}


}
