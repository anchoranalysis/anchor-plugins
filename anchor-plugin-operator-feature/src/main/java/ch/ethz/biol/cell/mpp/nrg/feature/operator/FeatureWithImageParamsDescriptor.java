package ch.ethz.biol.cell.mpp.nrg.feature.operator;

import org.anchoranalysis.feature.input.descriptor.FeatureInputDescriptor;

public class FeatureWithImageParamsDescriptor extends FeatureInputDescriptor {

	public static final FeatureWithImageParamsDescriptor instance = new FeatureWithImageParamsDescriptor();
	
	private FeatureWithImageParamsDescriptor() {
		
	}
	
	@Override
	public boolean isCompatibleWithEverything() {
		return false;
	}


}
