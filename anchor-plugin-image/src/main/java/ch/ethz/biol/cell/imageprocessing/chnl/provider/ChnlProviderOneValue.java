package ch.ethz.biol.cell.imageprocessing.chnl.provider;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.bean.provider.ChnlProviderOne;
import org.anchoranalysis.image.channel.Channel;

/**
 * A {@link ChnlProviderOne} which has a scalar value field
 * 
 * @author Owen Feehan
 *
 */
public abstract class ChnlProviderOneValue extends ChnlProviderOne {

	// START BEAN PROPERTIES
	@BeanField
	private double value;
	// END BEAN PROPERTIES
	
	@Override
	public Channel createFromChnl(Channel chnl) throws CreateException {
		return createFromChnlValue(chnl, value);
	}
	
	protected abstract Channel createFromChnlValue(Channel chnl, double value ) throws CreateException;
	
	public double getValue() {
		return value;
	}

	public void setValue(double value) {
		this.value = value;
	}
}
