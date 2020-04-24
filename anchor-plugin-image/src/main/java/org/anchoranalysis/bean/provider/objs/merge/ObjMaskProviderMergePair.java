package org.anchoranalysis.bean.provider.objs.merge;

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


import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.provider.objs.merge.MergeGraph.AssignPriority;
import org.anchoranalysis.bean.shared.relation.GreaterThanEqualToBean;
import org.anchoranalysis.bean.shared.relation.RelationBean;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.log.LogErrorReporter;
import org.anchoranalysis.core.relation.RelationToValue;
import org.anchoranalysis.feature.bean.Feature;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.feature.calc.FeatureInitParams;
import org.anchoranalysis.feature.input.FeatureInputNull;
import org.anchoranalysis.feature.session.calculator.FeatureCalculatorSingle;
import org.anchoranalysis.feature.shared.SharedFeatureSet;
import org.anchoranalysis.image.feature.bean.evaluator.FeatureEvaluatorSimple;
import org.anchoranalysis.image.feature.evaluator.EvaluateSingleObjMask;
import org.anchoranalysis.image.feature.objmask.FeatureInputSingleObj;
import org.anchoranalysis.image.feature.objmask.pair.merged.FeatureInputPairObjsMerged;
import org.anchoranalysis.image.objmask.ObjMask;

import objmaskwithfeature.ObjMaskWithFeature;
import objmaskwithfeature.ObjMaskWithFeatureAndPriority;

//  Merges with the object if a pair feature value satisfies a condition
//
//  We don't calculate single-object features (always use a 0)
//  We use pair-feature calculation to determine the priority, merging if it's greater than a threshold
//
public class ObjMaskProviderMergePair extends ObjMaskProviderMergeWithFeature {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// START BEAN PROPERTIES
	/**
	 * Allows merge only if the feature-value is greater than or equal to this threshold
	 */
	@BeanField
	private FeatureEvaluatorSimple<FeatureInputNull> featureEvaluatorThreshold;
	
	/**
	 * Relation to threshold
	 */
	@BeanField
	private RelationBean relation = new GreaterThanEqualToBean();
	
	@BeanField
	private Feature<FeatureInputPairObjsMerged> featureMerge;
	// END BEAN PROPERTIES

	@Override
	protected EvaluateSingleObjMask featureEvaluator(FeatureCalculatorSingle<FeatureInputSingleObj> featureSession) {
		// We don't care about evaluating single-features
		return (ObjMask om) -> 0;
	}
	
	@Override
	protected AssignPriority assignPriority() throws CreateException {
		try {
			double threshold = featureEvaluatorThreshold
				.createAndStartSession()
				.calc( FeatureInputNull.instance() );
			return new PriorityFeatureMergeSession(
				threshold,relation.create(),
				getLogger()
			);
			
		} catch (FeatureCalcException | OperationFailedException | InitException e) {
			throw new CreateException(e);
		}
	}
	
	private class PriorityFeatureMergeSession implements AssignPriority {

		private double threshold;
		private RelationToValue relation;
		private MergeSession session;
				
		public PriorityFeatureMergeSession(
			double threshold,
			RelationToValue relation,
			LogErrorReporter logErrorReporter
		) throws InitException {
			super();
			this.threshold = threshold;
			this.relation = relation;
			this.session = new MergeSession( featureMerge, new FeatureInitParams(), false );
			
			// TODO fixed shared features
			this.session.start(new SharedFeatureSet<FeatureInputPairObjsMerged>(),logErrorReporter);
		}

		@Override
		public ObjMaskWithFeatureAndPriority assignPriority(
			ObjMaskWithFeature omSrcWithFeature,
			ObjMaskWithFeature omDestWithFeature,
			ObjMask omMerge,
			EvaluateSingleObjMask featureEvaluatorSingleObj,
			LogErrorReporter logErrorReporter
		) throws OperationFailedException {
						
			double featureMerge;
			boolean considerForMerge;
			try {
				featureMerge = session.calc(omSrcWithFeature.getObjMask(), omDestWithFeature.getObjMask(), omMerge);
				considerForMerge = relation.isRelationToValueTrue(featureMerge,threshold);
				
			} catch (FeatureCalcException e) {
				logErrorReporter.getErrorReporter().recordError(PriorityFeatureMergeSession.class, "Cannot evaluate merge");
				logErrorReporter.getErrorReporter().recordError(PriorityFeatureMergeSession.class, e);
				featureMerge = Double.NaN;
				considerForMerge = false;
			}
			return new ObjMaskWithFeatureAndPriority(omMerge, 0, featureMerge, considerForMerge);
		}

		@Override
		public void end() throws OperationFailedException {
			session.clearCache();
		}
		
	}

	public FeatureEvaluatorSimple<FeatureInputNull> getFeatureEvaluatorThreshold() {
		return featureEvaluatorThreshold;
	}

	public void setFeatureEvaluatorThreshold(
			FeatureEvaluatorSimple<FeatureInputNull> featureEvaluatorThreshold) {
		this.featureEvaluatorThreshold = featureEvaluatorThreshold;
	}

	public RelationBean getRelation() {
		return relation;
	}

	public void setRelation(RelationBean relation) {
		this.relation = relation;
	}

	@Override
	protected boolean isSingleFeatureUsed() {
		return false;
	}



}
