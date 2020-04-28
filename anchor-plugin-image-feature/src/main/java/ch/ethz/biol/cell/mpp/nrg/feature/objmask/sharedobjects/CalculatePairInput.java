package ch.ethz.biol.cell.mpp.nrg.feature.objmask.sharedobjects;

import org.anchoranalysis.feature.cache.calculation.FeatureCalculation;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.binary.BinaryChnl;
import org.anchoranalysis.image.feature.objmask.FeatureInputSingleObj;
import org.anchoranalysis.image.feature.objmask.pair.FeatureInputPairObjs;
import org.anchoranalysis.image.objmask.ObjMask;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class CalculatePairInput extends FeatureCalculation<FeatureInputPairObjs, FeatureInputSingleObj> {

	private BinaryChnl chnl;
		
	public CalculatePairInput(BinaryChnl chnl) {
		super();
		this.chnl = chnl;
	}

	@Override
	protected FeatureInputPairObjs execute(FeatureInputSingleObj input) throws FeatureCalcException {

		ObjMask objFromBinary = new ObjMask(
			chnl.binaryVoxelBox()
		);
		
		return new FeatureInputPairObjs(
			input.getObjMask(),
			objFromBinary,
			input.getNrgStackOptional()
		);
	}
	
	@Override
	public boolean equals(Object other) {
		return other instanceof CalculatePairInput;
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().toHashCode();
	}
}
