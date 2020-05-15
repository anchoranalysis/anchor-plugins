package org.anchoranalysis.plugin.operator.feature.bean.conditional;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.shared.relation.threshold.RelationToThreshold;
import org.anchoranalysis.core.relation.RelationToValue;
import org.anchoranalysis.feature.bean.Feature;
import org.anchoranalysis.feature.bean.operator.FeatureGenericSingleElem;
import org.anchoranalysis.feature.cache.SessionInput;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.feature.input.FeatureInput;


/**
 * The result of featureCondition is compared to a threshold, and then either the underlying feature is calculated (positive case), or featureElse is (negative case)
 * 
 * <p>The positive case is when relation(featureCondition,value) is TRUE.</p>.
 * 
 * @author Owen Feehan
 *
 * @param <T> feature input-type
 */
public class IfCondition<T extends FeatureInput> extends FeatureGenericSingleElem<T> {

	// START BEAN PROPERTIRES
	@BeanField
	private Feature<T> featureCondition;
	
	@BeanField
	private Feature<T> featureElse;
	
	@BeanField
	private RelationToThreshold threshold;
	// END BEAN PROPERTIES
	
	@Override
	public double calc(SessionInput<T> input) throws FeatureCalcException {

		double featureConditionResult = input.calc(featureCondition); 
		RelationToValue relation = threshold.relation();
		
		if (relation.isRelationToValueTrue(featureConditionResult, threshold.threshold())) {
			return input.calc(super.getItem());
		} else {
			return input.calc(featureElse);
		}
	}
	
	public Feature<T> getFeatureCondition() {
		return featureCondition;
	}
	
	public void setFeatureCondition(Feature<T> featureCondition) {
		this.featureCondition = featureCondition;
	}

	public Feature<T> getFeatureElse() {
		return featureElse;
	}
	
	public void setFeatureElse(Feature<T> featureElse) {
		this.featureElse = featureElse;
	}

	public RelationToThreshold getThreshold() {
		return threshold;
	}

	public void setThreshold(RelationToThreshold threshold) {
		this.threshold = threshold;
	}
}
