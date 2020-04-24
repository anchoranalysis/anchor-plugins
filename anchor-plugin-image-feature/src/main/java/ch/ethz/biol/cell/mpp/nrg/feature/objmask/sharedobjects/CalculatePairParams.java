package ch.ethz.biol.cell.mpp.nrg.feature.objmask.sharedobjects;

import org.anchoranalysis.core.cache.ExecuteException;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.feature.cache.calculation.CacheableCalculation;
import org.anchoranalysis.image.bean.provider.BinaryImgChnlProvider;
import org.anchoranalysis.image.binary.BinaryChnl;
import org.anchoranalysis.image.feature.objmask.FeatureInputSingleObj;
import org.anchoranalysis.image.feature.objmask.pair.FeatureInputPairObjs;
import org.anchoranalysis.image.objmask.ObjMask;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class CalculatePairParams extends CacheableCalculation<FeatureInputPairObjs, FeatureInputSingleObj> {

	private BinaryImgChnlProvider binaryImgChnlProvider;
		
	public CalculatePairParams(BinaryImgChnlProvider binaryImgChnlProvider) {
		super();
		this.binaryImgChnlProvider = binaryImgChnlProvider;
	}

	@Override
	protected FeatureInputPairObjs execute(FeatureInputSingleObj params) throws ExecuteException {

		try {
			BinaryChnl bic = binaryImgChnlProvider.create();
			ObjMask objFromBinary = new ObjMask( bic.binaryVoxelBox() );
			return paramsPairs(params, objFromBinary);
			
		} catch (CreateException e) {
			throw new ExecuteException(e);
		}
	}
		
	private FeatureInputPairObjs paramsPairs( FeatureInputSingleObj params, ObjMask objFromBinary ) {
		FeatureInputPairObjs out = new FeatureInputPairObjs();
		out.setObjMask1( params.getObjMask() );
		out.setObjMask2( objFromBinary );
		out.setNrgStack( params.getNrgStack() );
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
