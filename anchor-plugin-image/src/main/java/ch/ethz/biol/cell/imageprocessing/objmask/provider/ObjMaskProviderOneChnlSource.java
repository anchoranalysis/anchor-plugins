package ch.ethz.biol.cell.imageprocessing.objmask.provider;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.bean.provider.ChnlProvider;
import org.anchoranalysis.image.bean.provider.ObjMaskProviderOne;
import org.anchoranalysis.image.chnl.Chnl;
import org.anchoranalysis.image.objmask.ObjMaskCollection;

public abstract class ObjMaskProviderOneChnlSource extends ObjMaskProviderOne {

	// START BEAN PROPERTIES
	@BeanField
	private ChnlProvider chnl;
	// END BEAN PROPERTIES
	
	@Override
	public ObjMaskCollection createFromObjs( ObjMaskCollection objsSrc ) throws CreateException {

		return createFromObjs(
			objsSrc,
			chnl.create()
		);
	}
	
	protected abstract ObjMaskCollection createFromObjs( ObjMaskCollection objsSrc, Chnl chnlSrc ) throws CreateException;

	public ChnlProvider getChnl() {
		return chnl;
	}

	public void setChnl(ChnlProvider chnl) {
		this.chnl = chnl;
	}
}
