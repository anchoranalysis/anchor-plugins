package ch.ethz.biol.cell.mpp.nrg.feature.stack;

import org.anchoranalysis.core.cache.ExecuteException;
import org.anchoranalysis.feature.cache.calculation.CachedCalculation;
import org.anchoranalysis.image.binary.BinaryChnl;
import org.anchoranalysis.image.binary.values.BinaryValues;
import org.anchoranalysis.image.chnl.Chnl;
import org.anchoranalysis.image.feature.objmask.FeatureObjMaskParams;
import org.anchoranalysis.image.feature.stack.FeatureStackParams;
import org.anchoranalysis.image.objmask.ObjMask;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class CalculateDeriveFeatureObjMaskParams extends CachedCalculation<FeatureObjMaskParams, FeatureStackParams> {

	private int nrgIndex;
		
	public CalculateDeriveFeatureObjMaskParams(int nrgIndex) {
		super();
		this.nrgIndex = nrgIndex;
	}

	@Override
	protected FeatureObjMaskParams execute(FeatureStackParams params) throws ExecuteException {
		FeatureObjMaskParams paramsObj = new FeatureObjMaskParams();
		
		ObjMask om = extractObjMask(params);
		paramsObj.setNrgStack( params.getNrgStack() );
		paramsObj.setObjMask( om );
		return paramsObj;
	}
	
	private ObjMask extractObjMask(FeatureStackParams params) {
		Chnl chnl = params.getNrgStack().getChnl(nrgIndex);
		BinaryChnl binary = new BinaryChnl(chnl, BinaryValues.getDefault());
		
		return new ObjMask( binary.binaryVoxelBox() );
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null) { return false; }
		if (obj == this) { return true; }
		if (obj.getClass() != getClass()) {
			return false;
		}
		CalculateDeriveFeatureObjMaskParams rhs = (CalculateDeriveFeatureObjMaskParams) obj;
		return new EqualsBuilder()
             .append(nrgIndex, rhs.nrgIndex)
             .isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().toHashCode();
	}
}
