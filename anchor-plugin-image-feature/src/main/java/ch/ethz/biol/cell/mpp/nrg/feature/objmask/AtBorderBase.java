package ch.ethz.biol.cell.mpp.nrg.feature.objmask;

import org.anchoranalysis.feature.cache.SessionInput;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.extent.BoundingBox;
import org.anchoranalysis.image.extent.ImageDim;
import org.anchoranalysis.image.feature.bean.objmask.FeatureObjMask;
import org.anchoranalysis.image.feature.objmask.FeatureInputSingleObj;

public abstract class AtBorderBase extends FeatureObjMask {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@Override
	public double calc(SessionInput<FeatureInputSingleObj> input) throws FeatureCalcException {
		
		if (isInputAtBorder(input.get())) {
			return 1.0;
		} else {
			return 0.0;
		}
	}
	
	private boolean isInputAtBorder( FeatureInputSingleObj input ) {
		return isBoundingBoxAtBorder(
			input.getObjMask().getBoundingBox(),
			input.getNrgStack().getDimensions()
		);
	}
	
	protected abstract boolean isBoundingBoxAtBorder( BoundingBox boundingBox, ImageDim dim );
}
