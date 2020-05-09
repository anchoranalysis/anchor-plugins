package org.anchoranalysis.plugin.operator.feature.bean;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.feature.bean.operator.FeatureGenericSingleElem;
import org.anchoranalysis.feature.cache.SessionInput;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.feature.input.FeatureInput;

/**
 * Operations that use a specified constant value
 * 
 * @author Owen Feehan
 *
 * @param <T>
 */
public abstract class FeatureGenericWithValue<T extends FeatureInput> extends FeatureGenericSingleElem<T> {

	// START BEAN PROPERTIES
	@BeanField
	private double value = 0;
	// END BEAN PROPERTIES
	
	@Override
	public double calc( SessionInput<T> input ) throws FeatureCalcException {
		return combineValueAndFeature(
			value,
			input.calc( getItem() )
		);
	}
	
	@Override
	public String getDscrLong() {
		return combineDscr(
			String.format("%f", value),
			getItem().getDscrLong()
		);
	}
	
	protected abstract double combineValueAndFeature( double value, double featureResult );

	protected abstract String combineDscr( String valueDscr, String featureDscr);
	
	public double getValue() {
		return value;
	}

	public void setValue(double value) {
		this.value = value;
	}
}
