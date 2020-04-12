package ch.ethz.biol.cell.mpp.nrg.feature.pair;

import org.anchoranalysis.anchor.mpp.feature.nrg.elem.NRGElemPairCalcParams;
import org.anchoranalysis.anchor.mpp.mark.GlobalRegionIdentifiers;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.cache.ExecuteException;
import org.anchoranalysis.feature.cache.CacheableParams;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import ch.ethz.biol.cell.mpp.nrg.cachedcalculation.OverlapCalculationMaskGlobal;

public abstract class OverlapMaskSingleRegion extends OverlapMaskBase {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// START BEAN PROPERTIES
	@BeanField
	private int regionID = GlobalRegionIdentifiers.SUBMARK_INSIDE;
	// END BEAN PROPERTIES
		
	protected double overlapWithGlobalMask( CacheableParams<NRGElemPairCalcParams> params ) throws FeatureCalcException {
		try {
			return params.calc(
				new OverlapCalculationMaskGlobal(regionID, getNrgIndex(), (byte) getMaskValue())
			);
			
		} catch (ExecuteException e) {
			throw new FeatureCalcException(e);
		}							
	}
	
	public int getRegionID() {
		return regionID;
	}

	public void setRegionID(int regionID) {
		this.regionID = regionID;
	}
}
