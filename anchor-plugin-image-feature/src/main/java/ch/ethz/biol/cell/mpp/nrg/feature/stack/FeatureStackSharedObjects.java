package ch.ethz.biol.cell.mpp.nrg.feature.stack;

import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.feature.params.FeatureInputDescriptor;
import org.anchoranalysis.image.feature.bean.FeatureSharedObjs;
import org.anchoranalysis.image.feature.stack.FeatureInputStack;
import org.anchoranalysis.image.feature.stack.FeatureInputStackDescriptor;

public abstract class FeatureStackSharedObjects extends FeatureSharedObjs<FeatureInputStack> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public FeatureInputDescriptor paramType() throws FeatureCalcException {
		return FeatureInputStackDescriptor.instance;
	}
}
