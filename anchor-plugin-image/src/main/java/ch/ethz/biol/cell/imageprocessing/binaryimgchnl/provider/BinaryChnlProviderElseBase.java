package ch.ethz.biol.cell.imageprocessing.binaryimgchnl.provider;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.bean.provider.BinaryChnlProvider;
import org.anchoranalysis.image.bean.provider.BinaryChnlProviderOne;
import org.anchoranalysis.image.binary.BinaryChnl;

public abstract class BinaryChnlProviderElseBase extends BinaryChnlProviderOne {

	// START BEAN PROPERTIES
	@BeanField
	private BinaryChnlProvider binaryChnlElse;
	// END BEAN PROPERTIES
	
	@Override
	protected BinaryChnl createFromChnl(BinaryChnl chnl) throws CreateException {
		if (condition(chnl)) {
			return chnl;
		} else {
			return binaryChnlElse.create();
		}
	}
	
	/** If this evaluates true, the binary-channel will be returned as-is, otherwise {@link binaryChnlElse} is returned */
	protected abstract boolean condition(BinaryChnl chnl) throws CreateException;
	
	public BinaryChnlProvider getBinaryChnlElse() {
		return binaryChnlElse;
	}

	public void setBinaryChnlElse(BinaryChnlProvider binaryChnlElse) {
		this.binaryChnlElse = binaryChnlElse;
	}
}
