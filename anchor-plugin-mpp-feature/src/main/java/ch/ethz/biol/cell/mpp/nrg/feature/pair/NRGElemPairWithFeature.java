package ch.ethz.biol.cell.mpp.nrg.feature.pair;

import org.anchoranalysis.anchor.mpp.feature.bean.nrg.elem.NRGElemPair;
import org.anchoranalysis.anchor.mpp.feature.nrg.elem.NRGElemIndCalcParams;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.feature.bean.Feature;

public abstract class NRGElemPairWithFeature extends NRGElemPair {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// START BEAN PROPERTIES
	@BeanField
	private Feature<NRGElemIndCalcParams> item;
	// END BEAN PROPERTIES
	
	public Feature<NRGElemIndCalcParams> getItem() {
		return item;
	}

	public void setItem(Feature<NRGElemIndCalcParams> item) {
		this.item = item;
	}
}
