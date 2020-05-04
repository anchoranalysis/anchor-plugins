package org.anchoranalysis.plugin.image.bean.merge;

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


import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.shared.relation.GreaterThanEqualToBean;
import org.anchoranalysis.bean.shared.relation.RelationBean;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.feature.bean.list.FeatureList;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.feature.input.FeatureInputNRGStack;
import org.anchoranalysis.feature.input.FeatureInputNull;
import org.anchoranalysis.feature.nrg.NRGStackWithParams;
import org.anchoranalysis.feature.session.calculator.FeatureCalculatorSingle;
import org.anchoranalysis.feature.session.calculator.FeatureCalculatorSingleChangeInput;
import org.anchoranalysis.feature.session.calculator.FeatureCalculatorSingleFromMulti;
import org.anchoranalysis.feature.shared.SharedFeaturesInitParams;
import org.anchoranalysis.image.feature.bean.evaluator.FeatureEvaluator;
import org.anchoranalysis.image.feature.bean.evaluator.FeatureEvaluatorNrgStack;
import org.anchoranalysis.image.feature.evaluator.PayloadCalculator;
import org.anchoranalysis.image.feature.objmask.pair.FeatureInputPairObjs;
import org.anchoranalysis.image.feature.session.MergedPairsFeatures;
import org.anchoranalysis.image.feature.session.MergedPairsSession;
import org.anchoranalysis.image.objmask.ObjMask;
import org.anchoranalysis.plugin.image.merge.priority.AssignPriority;
import org.anchoranalysis.plugin.image.merge.priority.AssignPriorityFromPair;

/**
 *   Merges objects if a pair feature value satisfies a condition
 *   
 *   <p>Single-object features are NOT calculated (a 0 is always used.).</p>
 *   <p>A pair feature calculation determine the prioritys, merging if it's greater than a threshold</p>/
 *    
 */
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
	private FeatureEvaluator<FeatureInputNull> featureEvaluatorThreshold;
	
	/**
	 * Relation to threshold
	 */
	@BeanField
	private RelationBean relation = new GreaterThanEqualToBean();
	
	@BeanField
	private FeatureEvaluatorNrgStack<FeatureInputPairObjs> featureEvaluatorMerge;
	// END BEAN PROPERTIES

	@Override
	protected PayloadCalculator createPayloadCalculator() {
		// We don't care about evaluating single-features
		return (ObjMask om) -> 0;
	}
	
	@Override
	protected AssignPriority createPrioritizer() throws OperationFailedException {
		try {
			double threshold = featureEvaluatorThreshold
				.createAndStartSession()
				.calc( FeatureInputNull.instance() );
			
			return new AssignPriorityFromPair(
				createCalculatorForPairs(),
				threshold,
				relation.create()
			);
			
		} catch (FeatureCalcException | InitException | CreateException e) {
			throw new OperationFailedException(e);
		}
	}
	
	private FeatureCalculatorSingle<FeatureInputPairObjs> createCalculatorForPairs() throws CreateException {
		try {
			Optional<NRGStackWithParams> nrgStack = featureEvaluatorMerge.nrgStack();
			
			MergedPairsSession session = new MergedPairsSession(
				new MergedPairsFeatures(
					new FeatureList<>( featureEvaluatorMerge.getFeatureProvider().create() )
				)
			);
			session.start(
				getSharedObjects(),
				SharedFeaturesInitParams.create(getLogger()),	// To fix to include shared objects
				nrgStack,
				getLogger()
			);

			return maybeWrapWithNRGStack(
				new FeatureCalculatorSingleFromMulti<>(session),
				nrgStack
			);
			
		} catch (FeatureCalcException | OperationFailedException | InitException e) {
			throw new CreateException(e);
		}
	}
	
	private static <T extends FeatureInputNRGStack> FeatureCalculatorSingle<T> maybeWrapWithNRGStack(
		FeatureCalculatorSingle<T> calculator,
		Optional<NRGStackWithParams> nrgStack
	) {
		if (nrgStack.isPresent()) {
			return new FeatureCalculatorSingleChangeInput<>(
				calculator,
				input->input.setNrgStack(nrgStack)
			);
		} else {
			return calculator;
		}
	}
	
	
	public FeatureEvaluator<FeatureInputNull> getFeatureEvaluatorThreshold() {
		return featureEvaluatorThreshold;
	}

	public void setFeatureEvaluatorThreshold(FeatureEvaluator<FeatureInputNull> featureEvaluatorThreshold) {
		this.featureEvaluatorThreshold = featureEvaluatorThreshold;
	}

	public RelationBean getRelation() {
		return relation;
	}

	public void setRelation(RelationBean relation) {
		this.relation = relation;
	}

	@Override
	protected boolean isPlayloadUsed() {
		return true;
	}

	public FeatureEvaluatorNrgStack<FeatureInputPairObjs> getFeatureEvaluatorMerge() {
		return featureEvaluatorMerge;
	}

	public void setFeatureEvaluatorMerge(FeatureEvaluatorNrgStack<FeatureInputPairObjs> featureEvaluatorMerge) {
		this.featureEvaluatorMerge = featureEvaluatorMerge;
	}
}
