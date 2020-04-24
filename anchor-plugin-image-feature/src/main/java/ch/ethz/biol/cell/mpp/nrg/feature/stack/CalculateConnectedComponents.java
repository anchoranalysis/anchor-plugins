package ch.ethz.biol.cell.mpp.nrg.feature.stack;

import org.anchoranalysis.core.cache.ExecuteException;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.feature.cache.calculation.CacheableCalculation;
import org.anchoranalysis.image.binary.BinaryChnl;
import org.anchoranalysis.image.binary.values.BinaryValues;
import org.anchoranalysis.image.feature.stack.FeatureInputStack;
import org.anchoranalysis.image.objmask.ObjMaskCollection;
import org.anchoranalysis.image.objmask.factory.CreateFromConnectedComponentsFactory;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;


public class CalculateConnectedComponents extends CacheableCalculation<ObjMaskCollection, FeatureInputStack> {

	private int nrgChnlIndex;
		
	public CalculateConnectedComponents(int nrgChnlIndex) {
		super();
		this.nrgChnlIndex = nrgChnlIndex;
	}

	@Override
	protected ObjMaskCollection execute(FeatureInputStack params) throws ExecuteException {

		BinaryChnl binaryImgChnl = new BinaryChnl(
			params.getNrgStack().getChnl(nrgChnlIndex),
			BinaryValues.getDefault()
		);
		
		try {
			CreateFromConnectedComponentsFactory objMaskCreator = new CreateFromConnectedComponentsFactory();
			objMaskCreator.setMinNumberVoxels(1);
			return objMaskCreator.createConnectedComponents(binaryImgChnl );
			
		} catch (CreateException e) {
			throw new ExecuteException(e);
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) { return false; }
		if (obj == this) { return true; }
		if (obj.getClass() != getClass()) {
			return false;
		}
		CalculateConnectedComponents rhs = (CalculateConnectedComponents) obj;
		return new EqualsBuilder()
             .append(nrgChnlIndex, rhs.nrgChnlIndex)
             .isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder()
			.append(nrgChnlIndex)
			.toHashCode();
	}
}
