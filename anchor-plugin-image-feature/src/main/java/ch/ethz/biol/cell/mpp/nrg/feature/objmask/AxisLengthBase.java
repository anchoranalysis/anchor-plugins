package ch.ethz.biol.cell.mpp.nrg.feature.objmask;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.feature.cache.SessionInput;
import org.anchoranalysis.feature.cache.calculation.FeatureCalculation;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.feature.bean.objmask.FeatureObjMask;
import org.anchoranalysis.image.feature.objmask.FeatureInputSingleObj;
import org.anchoranalysis.math.moment.MomentsFromPointsCalculator;

public abstract class AxisLengthBase extends FeatureObjMask {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	// START BEAN PROPERTIES
	@BeanField
	private int index = 0;
	// END BEAN PROPERTIES
	
	@Override
	public double calc(SessionInput<FeatureInputSingleObj> input) throws FeatureCalcException {
		
		assert( input.get() instanceof FeatureInputSingleObj );
		return calcAxisLength( input, index);
	}

	protected abstract FeatureCalculation<MomentsFromPointsCalculator,FeatureInputSingleObj> momentsCalculator();
	
	private double calcAxisLength( SessionInput<FeatureInputSingleObj> input, int index ) throws FeatureCalcException {
		
		if (!input.get().getObjMask().hasPixelsGreaterThan(0)) {
			return Double.NaN;
		}

		MomentsFromPointsCalculator moments = input.calc( momentsCalculator() );
		return moments.get( index ).eigenvalueNormalizedAsAxisLength();
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}
}
