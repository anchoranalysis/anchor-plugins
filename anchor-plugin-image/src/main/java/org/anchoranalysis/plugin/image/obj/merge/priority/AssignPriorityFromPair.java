package org.anchoranalysis.plugin.image.obj.merge.priority;

import java.util.Optional;

import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.error.reporter.ErrorReporter;
import org.anchoranalysis.core.relation.RelationToValue;
import org.anchoranalysis.feature.session.calculator.FeatureCalculatorSingle;
import org.anchoranalysis.image.feature.objmask.pair.FeatureInputPairObjs;
import org.anchoranalysis.image.objmask.ObjMask;
import org.anchoranalysis.plugin.image.obj.merge.ObjVertex;


/**
 * <p>Calculates pair-feature on each potential merge, and this value determines priority.</p>.
 * 
 * @author Owen Feehan
 *
 */
public class AssignPriorityFromPair extends AssignPriority {

	private double threshold;
	private RelationToValue relation;
	private FeatureCalculatorSingle<FeatureInputPairObjs> featureCalculator;
			
	public AssignPriorityFromPair(
		FeatureCalculatorSingle<FeatureInputPairObjs> featureCalculator,
		double threshold,
		RelationToValue relation
	) throws InitException {
		super();
		this.threshold = threshold;
		this.relation = relation;
		this.featureCalculator = featureCalculator;
	}

	@Override
	public PrioritisedVertex assignPriorityToEdge(
		ObjVertex src,
		ObjVertex dest,
		ObjMask merge,
		ErrorReporter errorReporter
	) throws OperationFailedException {

		double resultPair = featureCalculator.calcSuppressErrors(
			createInput(src, dest, merge),
			errorReporter
		);

		return new PrioritisedVertex(
			merge,
			0,
			resultPair,
			relation.isRelationToValueTrue(resultPair,threshold)
		);
	}
	
	private FeatureInputPairObjs createInput(
		ObjVertex omSrcWithFeature,
		ObjVertex omDestWithFeature,
		ObjMask omMerge	
	) {
		return new FeatureInputPairObjs(
			omSrcWithFeature.getObjMask(),
			omDestWithFeature.getObjMask(),
			Optional.empty(),
			Optional.of(omMerge)
		);
	}
}