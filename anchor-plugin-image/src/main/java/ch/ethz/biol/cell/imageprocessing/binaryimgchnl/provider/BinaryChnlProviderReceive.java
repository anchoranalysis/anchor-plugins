package ch.ethz.biol.cell.imageprocessing.binaryimgchnl.provider;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.bean.provider.BinaryChnlProvider;
import org.anchoranalysis.image.bean.provider.BinaryImgChnlProviderOne;
import org.anchoranalysis.image.binary.BinaryChnl;

public abstract class BinaryChnlProviderReceive extends BinaryImgChnlProviderOne {

	// START BEAN PROPERTIES
	@BeanField
	private BinaryChnlProvider receive;
	// END BEAN PROPERTIES

	@Override
	public BinaryChnl createFromChnl(BinaryChnl chnl) throws CreateException {

		BinaryChnl receiveChnl = receive.create();
		return createFromChnlReceive(chnl, receiveChnl);
	}
	
	protected abstract BinaryChnl createFromChnlReceive(BinaryChnl chnl, BinaryChnl receiveChnl) throws CreateException;
		
	public BinaryChnlProvider getReceive() {
		return receive;
	}

	public void setReceive(BinaryChnlProvider receive) {
		this.receive = receive;
	}
	
	
}
