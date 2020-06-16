package ch.ethz.biol.cell.imageprocessing.objmask.provider;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.bean.provider.ChnlProvider;
import org.anchoranalysis.image.bean.provider.ObjMaskProviderOne;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.objectmask.ObjectMaskCollection;

public abstract class ObjMaskProviderOneChnlSource extends ObjMaskProviderOne {

	// START BEAN PROPERTIES
	@BeanField
	private ChnlProvider chnl;
	// END BEAN PROPERTIES
	
	@Override
	public ObjectMaskCollection createFromObjs( ObjectMaskCollection objsSrc ) throws CreateException {

		return createFromObjs(
			objsSrc,
			chnl.create()
		);
	}
	
	protected abstract ObjectMaskCollection createFromObjs( ObjectMaskCollection objsSrc, Channel chnlSrc ) throws CreateException;

	public ChnlProvider getChnl() {
		return chnl;
	}

	public void setChnl(ChnlProvider chnl) {
		this.chnl = chnl;
	}
}
