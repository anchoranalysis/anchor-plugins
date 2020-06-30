package org.anchoranalysis.plugin.annotation.bean.comparison.assigner;

/*-
 * #%L
 * anchor-plugin-annotation
 * %%
 * Copyright (C) 2010 - 2019 Owen Feehan, ETH Zurich, University of Zurich, Hoffmann la Roche
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

import org.anchoranalysis.annotation.io.assignment.AssignmentObjMaskFactory;

import org.anchoranalysis.annotation.io.assignment.AssignmentOverlapFromPairs;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.feature.shared.SharedFeaturesInitParams;
import org.anchoranalysis.image.extent.ImageDim;
import org.anchoranalysis.image.feature.bean.evaluator.FeatureEvaluatorSimple;
import org.anchoranalysis.image.feature.object.input.FeatureInputPairObjects;
import org.anchoranalysis.io.output.bound.BoundIOContext;
import org.anchoranalysis.plugin.annotation.comparison.AnnotationGroup;
import org.anchoranalysis.plugin.annotation.comparison.AnnotationGroupObjMask;
import org.anchoranalysis.plugin.annotation.comparison.ObjsToCompare;

public class FeatureCostAssigner extends AnnotationComparisonAssigner<AssignmentOverlapFromPairs> {

	// START BEAN PROPERTIES
	@BeanField
	private FeatureEvaluatorSimple<FeatureInputPairObjects> featureEvaluator;
		
	@BeanField
	private double maxCost = 1.0;
		
	@BeanField
	private int numDecimalPlaces = 3;
		
	@BeanField
	private boolean removeTouchingBorderXY = false;
	// END BEAN PROPERTIES
	
	@Override
	public AssignmentOverlapFromPairs createAssignment(
		ObjsToCompare objsToCompare,
		ImageDim dim,
		boolean useMIP,
		BoundIOContext context
	) throws CreateException {
		try {
			SharedFeaturesInitParams soFeature = SharedFeaturesInitParams.create(
				context.getLogger()
			);
			featureEvaluator.initRecursive( soFeature, context.getLogger() );

			AssignmentObjMaskFactory assignmentCreator = new AssignmentObjMaskFactory(
				featureEvaluator,
				useMIP
			);
			
			AssignmentOverlapFromPairs assignment = assignmentCreator.createAssignment(
				objsToCompare.getLeft(),
				objsToCompare.getRight(),
				maxCost,
				dim
			);
			
			// We remove any border items from the assignment
			if (removeTouchingBorderXY) {
				assignment.removeTouchingBorderXY( dim );
			}
			
			context.getOutputManager().getWriterCheckIfAllowed().write(
				"costMatrix",
				() -> new ObjMaskCollectionDistanceMatrixGenerator(assignmentCreator.getCost(), numDecimalPlaces )
			);
			
			return assignment;
		} catch (FeatureCalcException | InitException e1) {
			throw new CreateException(e1);
		}
	}

	public FeatureEvaluatorSimple<FeatureInputPairObjects> getFeatureEvaluator() {
		return featureEvaluator;
	}

	public void setFeatureEvaluator(FeatureEvaluatorSimple<FeatureInputPairObjects> featureEvaluator) {
		this.featureEvaluator = featureEvaluator;
	}

	public double getMaxCost() {
		return maxCost;
	}

	public void setMaxCost(double maxCost) {
		this.maxCost = maxCost;
	}

	public int getNumDecimalPlaces() {
		return numDecimalPlaces;
	}

	public void setNumDecimalPlaces(int numDecimalPlaces) {
		this.numDecimalPlaces = numDecimalPlaces;
	}

	@Override
	public AnnotationGroup<AssignmentOverlapFromPairs> groupForKey(String key) {
		return new AnnotationGroupObjMask(key);
	}

	@Override
	public boolean moreThanOneObj() {
		return true;
	}

	public boolean isRemoveTouchingBorderXY() {
		return removeTouchingBorderXY;
	}

	public void setRemoveTouchingBorderXY(boolean removeTouchingBorderXY) {
		this.removeTouchingBorderXY = removeTouchingBorderXY;
	}
}
