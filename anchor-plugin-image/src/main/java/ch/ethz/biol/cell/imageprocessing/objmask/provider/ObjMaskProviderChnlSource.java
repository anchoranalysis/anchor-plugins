package ch.ethz.biol.cell.imageprocessing.objmask.provider;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.bean.provider.ChnlProvider;
import org.anchoranalysis.image.bean.provider.ObjMaskProvider;
import org.anchoranalysis.image.chnl.Chnl;
import org.anchoranalysis.image.objmask.ObjMaskCollection;

public abstract class ObjMaskProviderChnlSource extends ObjMaskProvider {

	// START BEAN PROPERTIES
	@BeanField
	private ChnlProvider chnl;
	// END BEAN PROPERTIES
	
	@Override
	public ObjMaskCollection create() throws CreateException {
		return createFromChnl(
			chnl.create()
		);
	}
	
	protected abstract ObjMaskCollection createFromChnl( Chnl chnlSrc ) throws CreateException;

	public ChnlProvider getChnl() {
		return chnl;
	}

	public void setChnl(ChnlProvider chnl) {
		this.chnl = chnl;
	}
}
