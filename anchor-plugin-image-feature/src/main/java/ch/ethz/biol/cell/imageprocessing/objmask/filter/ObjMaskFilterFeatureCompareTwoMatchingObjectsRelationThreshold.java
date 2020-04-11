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


import java.util.ArrayList;
import java.util.List;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.shared.relation.RelationBean;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.relation.RelationToValue;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.feature.nrg.NRGStackWithParams;
import org.anchoranalysis.feature.session.SequentialSession;
import org.anchoranalysis.feature.session.calculator.FeatureCalculatorMulti;
import org.anchoranalysis.feature.session.calculator.FeatureCalculatorMultiChangeParams;
import org.anchoranalysis.feature.session.calculator.FeatureCalculatorSingle;
import org.anchoranalysis.image.bean.objmask.filter.ObjMaskFilter;
import org.anchoranalysis.image.bean.objmask.match.ObjMaskMatcher;
import org.anchoranalysis.image.extent.ImageDim;
import org.anchoranalysis.image.feature.bean.evaluator.FeatureEvaluator;
import org.anchoranalysis.image.feature.objmask.FeatureObjMaskParams;
import org.anchoranalysis.image.feature.objmask.pair.FeatureObjMaskPairParams;
import org.anchoranalysis.image.feature.session.FeatureSessionCreateParamsSingle;
import org.anchoranalysis.image.objmask.ObjMask;
import org.anchoranalysis.image.objmask.ObjMaskCollection;
import org.anchoranalysis.image.objmask.match.ObjWithMatches;

// Evaluates a feature on the Source Object and exactly one Object that it matches
public class ObjMaskFilterFeatureCompareTwoMatchingObjectsRelationThreshold extends ObjMaskFilter {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// START BEAN PROPERTIES
	@BeanField
	private ObjMaskMatcher objMaskMatcher1;
	
	@BeanField
	private ObjMaskMatcher objMaskMatcher2;
	
	@BeanField
	private double threshold;
	
	@BeanField
	private RelationBean relation;
	
	@BeanField
	private FeatureEvaluator<FeatureObjMaskPairParams> featureEvaluator;
	// END BEAN PROPERTIES
	
	private void requireExactlyOneMatch( List<ObjWithMatches> matchList, String matchListName ) throws OperationFailedException {
		for( ObjWithMatches owm : matchList ) {
			if (owm.getMatches().size()==0) {
				throw new OperationFailedException( String.format("No intersecting object in %s",matchListName) );
			}
			if (owm.getMatches().size()!=1) {
				throw new OperationFailedException( String.format("Exactly one match in %s is required. %d were found.", matchListName, owm.getMatches().size()) );
			}
		}
	}

	@Override
	public void filter(ObjMaskCollection objs, ImageDim dim, ObjMaskCollection objsRejected)
			throws OperationFailedException {

		List<ObjWithMatches> matchList1 = objMaskMatcher1.findMatch(objs);
		requireExactlyOneMatch( matchList1, "matchList1" );
		
		List<ObjWithMatches> matchList2 = objMaskMatcher2.findMatch(objs);
		requireExactlyOneMatch( matchList2, "matchList2" );

		FeatureCalculatorSingle<FeatureObjMaskPairParams> featureSession = featureEvaluator.createAndStartSession();
		
		RelationToValue relationToValue = relation.create();
		
		try {
			// Our results order should be preserved in the objMaskMatcher
			
			List<ObjMask> listToRemove = new ArrayList<>();
			
			for( int i=0; i<objs.size(); i++ ) {
				
				ObjWithMatches objWithMatches1 = matchList1.get(i);  
				
				ObjMask match1 = objWithMatches1.getMatches().get(0);
				ObjMask match2 = matchList2.get(i).getMatches().get(0);
								
				double featureVal = featureSession.calcOne(
					new FeatureObjMaskPairParams(match1, match2)
				);
				
				if (!relationToValue.isRelationToValueTrue(featureVal, threshold)) {
					listToRemove.add(objWithMatches1.getSourceObj());
					
					if (objsRejected!=null) {
						objsRejected.add(objWithMatches1.getSourceObj());
					}
				}
			}
			
			
			for( ObjMask om : listToRemove ) {
				objs.remove(om);	
			}
			
			
		} catch (FeatureCalcException e) {
			throw new OperationFailedException(e);
		}
	}

	public double getThreshold() {
		return threshold;
	}

	public void setThreshold(double threshold) {
		this.threshold = threshold;
	}

	public RelationBean getRelation() {
		return relation;
	}

	public void setRelation(RelationBean relation) {
		this.relation = relation;
	}

	public ObjMaskMatcher getObjMaskMatcher1() {
		return objMaskMatcher1;
	}

	public void setObjMaskMatcher1(ObjMaskMatcher objMaskMatcher1) {
		this.objMaskMatcher1 = objMaskMatcher1;
	}

	public ObjMaskMatcher getObjMaskMatcher2() {
		return objMaskMatcher2;
	}

	public void setObjMaskMatcher2(ObjMaskMatcher objMaskMatcher2) {
		this.objMaskMatcher2 = objMaskMatcher2;
	}

	public FeatureEvaluator<FeatureObjMaskPairParams> getFeatureEvaluator() {
		return featureEvaluator;
	}

	public void setFeatureEvaluator(FeatureEvaluator<FeatureObjMaskPairParams> featureEvaluator) {
		this.featureEvaluator = featureEvaluator;
	}
}
