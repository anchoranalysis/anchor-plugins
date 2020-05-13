package ch.ethz.biol.cell.imageprocessing.binaryimgchnl.provider;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.bean.provider.BinaryChnlProvider;
import org.anchoranalysis.image.bean.provider.ChnlProvider;
import org.anchoranalysis.image.binary.BinaryChnl;
import org.anchoranalysis.image.chnl.Chnl;

public abstract class BinaryImgChnlProviderChnlSource extends BinaryChnlProvider {

	// START BEAN
	@BeanField
	private ChnlProvider chnl;
	// END BEAN
	
	@Override
	public BinaryChnl create() throws CreateException {
		return createFromSource(
			chnl.create()
		);
	}
	
	protected abstract BinaryChnl createFromSource(Chnl chnlSource) throws CreateException;
	
	public ChnlProvider getChnl() {
		return chnl;
	}

	public void setChnl(ChnlProvider chnl) {
		this.chnl = chnl;
	}
}
