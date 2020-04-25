package ch.ethz.biol.cell.mpp.nrg.feature.stack;

import org.anchoranalysis.feature.cache.calculation.ResolvedCalculation;
import org.anchoranalysis.image.feature.objmask.FeatureInputSingleObj;
import org.anchoranalysis.image.feature.stack.FeatureInputStack;
import org.anchoranalysis.image.objmask.ObjMaskCollection;
import org.anchoranalysis.plugin.image.feature.obj.pair.CalculateInputFromDelegate;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class CalculateDeriveObjFromCollection extends CalculateInputFromDelegate<FeatureInputSingleObj, FeatureInputStack, ObjMaskCollection> {

	private int index;

	public CalculateDeriveObjFromCollection(ResolvedCalculation<ObjMaskCollection, FeatureInputStack> ccDelegate,
			int index) {
		super(ccDelegate);
		this.index = index;
	}

	@Override
	protected FeatureInputSingleObj deriveFromDelegate(FeatureInputStack input, ObjMaskCollection delegate) {
		return new FeatureInputSingleObj(
			delegate.get(index),
			input.getNrgStackOptional()
		);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) { return false; }
		if (obj == this) { return true; }
		if (obj.getClass() != getClass()) {
			return false;
		}
		CalculateDeriveObjFromCollection rhs = (CalculateDeriveObjFromCollection) obj;
		return new EqualsBuilder()
             .append(index, rhs.index)
             .isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder()
			.append(index)
			.toHashCode();
	}

}
