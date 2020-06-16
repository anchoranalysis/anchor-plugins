package ch.ethz.biol.cell.imageprocessing.chnl.provider;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.bean.provider.BinaryChnlProvider;
import org.anchoranalysis.image.bean.provider.ChnlProvider;
import org.anchoranalysis.image.binary.BinaryChnl;
import org.anchoranalysis.image.channel.Channel;

/**
 * A base class for a {@link ChnlProvider} which also uses a binary-mask, but which doesn't use any other {@link ChnlProvider} as an input.
 * 
 * <p>Note for classes that use both a binary-mask AND another {@link ChnlProvider}, see {@link ChnlProviderOneMask}.
 *  
 * @author Owen Feehan
 *
 */
public abstract class ChnlProviderMask extends ChnlProvider {

	// START BEAN PROPERTIES
	@BeanField
	private BinaryChnlProvider mask;
	// END BEAN PROPERTIES
	
	@Override
	public Channel create() throws CreateException {
		BinaryChnl maskChnl = mask.create();
		return createFromMask(maskChnl);
	}
	
	protected abstract Channel createFromMask(BinaryChnl mask) throws CreateException;

	public BinaryChnlProvider getMask() {
		return mask;
	}

	public void setMask(BinaryChnlProvider mask) {
		this.mask = mask;
	}
}
