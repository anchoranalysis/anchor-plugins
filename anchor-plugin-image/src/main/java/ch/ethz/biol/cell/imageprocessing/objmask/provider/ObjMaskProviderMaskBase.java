package ch.ethz.biol.cell.imageprocessing.objmask.provider;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.bean.provider.BinaryChnlProvider;
import org.anchoranalysis.image.bean.provider.ObjMaskProviderOne;
import org.anchoranalysis.image.binary.BinaryChnl;
import org.anchoranalysis.image.objmask.ObjMaskCollection;

public abstract class ObjMaskProviderMaskBase extends ObjMaskProviderOne {

	// START BEAN PROPERTIES
	@BeanField
	private BinaryChnlProvider maskProvider;
	// END BEAN PROPERTIES
	
	@Override
	public ObjMaskCollection createFromObjs(ObjMaskCollection objsIn) throws CreateException {
		BinaryChnl mask = maskProvider.create();
		return createFromObjs(objsIn, mask);
	}
	
	protected abstract ObjMaskCollection createFromObjs(ObjMaskCollection objsIn, BinaryChnl mask) throws CreateException;

	public BinaryChnlProvider getMaskProvider() {
		return maskProvider;
	}

	public void setMaskProvider(BinaryChnlProvider maskProvider) {
		this.maskProvider = maskProvider;
	}
}
