package ch.ethz.biol.cell.mpp.nrg.feature.objmask.sharedobjects;

import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.feature.cache.calculation.FeatureCalculation;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.bean.provider.BinaryImgChnlProvider;
import org.anchoranalysis.image.binary.BinaryChnl;
import org.anchoranalysis.image.feature.objmask.FeatureInputSingleObj;
import org.anchoranalysis.image.feature.objmask.pair.FeatureInputPairObjs;
import org.anchoranalysis.image.objmask.ObjMask;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class CalculatePairParams extends FeatureCalculation<FeatureInputPairObjs, FeatureInputSingleObj> {

	private BinaryImgChnlProvider binaryImgChnlProvider;
		
	public CalculatePairParams(BinaryImgChnlProvider binaryImgChnlProvider) {
		super();
		this.binaryImgChnlProvider = binaryImgChnlProvider;
	}

	@Override
	protected FeatureInputPairObjs execute(FeatureInputSingleObj input) throws FeatureCalcException {

		try {
			BinaryChnl bic = binaryImgChnlProvider.create();
			return paramsPairs(
				input,
				new ObjMask( bic.binaryVoxelBox() )
			);
			
		} catch (CreateException e) {
			throw new FeatureCalcException(e);
		}
	}
		
	private FeatureInputPairObjs paramsPairs( FeatureInputSingleObj input, ObjMask objFromBinary ) {
		FeatureInputPairObjs out = new FeatureInputPairObjs();
		out.setObjMask1( input.getObjMask() );
		out.setObjMask2( objFromBinary );
		out.setNrgStack( input.getNrgStackOptional() );
		return out;
	}
	
	@Override
	public boolean equals(Object other) {
		return other instanceof CalculatePairParams;
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().toHashCode();
	}
}
