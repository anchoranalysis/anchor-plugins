package ch.ethz.biol.cell.mpp.nrg.feature.stack;

import org.anchoranalysis.feature.bean.FeatureSharedObjs;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.feature.params.FeatureParamsDescriptor;
import org.anchoranalysis.image.feature.stack.FeatureStackParams;
import org.anchoranalysis.image.feature.stack.FeatureStackParamsDescriptor;

public abstract class FeatureStackSharedObjects extends FeatureSharedObjs<FeatureStackParams> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public FeatureParamsDescriptor paramType() throws FeatureCalcException {
		return FeatureStackParamsDescriptor.instance;
	}
}
