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
import org.anchoranalysis.bean.shared.relation.RelationBean;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.relation.RelationToValue;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.feature.session.calculator.FeatureCalculatorSingle;
import org.anchoranalysis.image.bean.objmask.filter.ObjMaskFilter;
import org.anchoranalysis.image.bean.objmask.match.ObjMaskMatcher;
import org.anchoranalysis.image.extent.ImageDim;
import org.anchoranalysis.image.feature.bean.evaluator.FeatureEvaluator;
import org.anchoranalysis.image.feature.objmask.pair.FeatureInputPairObjs;
import org.anchoranalysis.image.objectmask.ObjectMask;
import org.anchoranalysis.image.objectmask.ObjectMaskCollection;
import org.anchoranalysis.image.objmask.match.ObjWithMatches;

// Evaluates a feature on the Source Object and exactly one Object that it matches
public class ObjMaskFilterFeatureMatchingObjectsRelationThreshold extends ObjMaskFilter {

	// START BEAN PROPERTIES
	@BeanField
	private ObjMaskMatcher objMaskMatcher;
	
	@BeanField
	private double threshold;
	
	@BeanField
	private RelationBean relation;
	
	@BeanField
	private FeatureEvaluator<FeatureInputPairObjs> featureEvaluator;
	// END BEAN PROPERTIES
	
	@Override
	public void filter(ObjectMaskCollection objs, Optional<ImageDim> dim, Optional<ObjectMaskCollection> objsRejected)
			throws OperationFailedException {

		List<ObjWithMatches> matchList = objMaskMatcher.findMatch(objs);
		
		for( ObjWithMatches owm : matchList ) {
			if (owm.getMatches().size()==0) {
				throw new OperationFailedException("No matching object found");
			}
			if (owm.getMatches().size()!=1) {
				throw new OperationFailedException( String.format("Exactly one match is required. %d were found.", owm.getMatches().size()) );
			}
		}
		
		RelationToValue r = relation.create();
	
		try {
			FeatureCalculatorSingle<FeatureInputPairObjs> session = featureEvaluator.createAndStartSession();
		
			for( ObjWithMatches owm : matchList ) {
				
				for (ObjectMask match : owm.getMatches()) {
					
					double featureVal = session.calc(
						new FeatureInputPairObjs(owm.getSourceObj(), match)
					);
					
					if (!r.isRelationToValueTrue(featureVal, threshold)) {
						objs.remove(owm.getSourceObj());
						
						if (objsRejected.isPresent()) {
							objsRejected.get().add(owm.getSourceObj());
						}
						
						break;
					}
				}
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

	public ObjMaskMatcher getObjMaskMatcher() {
		return objMaskMatcher;
	}

	public void setObjMaskMatcher(ObjMaskMatcher objMaskMatcher) {
		this.objMaskMatcher = objMaskMatcher;
	}

	public FeatureEvaluator<FeatureInputPairObjs> getFeatureEvaluator() {
		return featureEvaluator;
	}

	public void setFeatureEvaluator(FeatureEvaluator<FeatureInputPairObjs> featureEvaluator) {
		this.featureEvaluator = featureEvaluator;
	}

}
