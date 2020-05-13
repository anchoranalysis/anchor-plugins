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
	private BinaryChnlProvider mask;
	// END BEAN PROPERTIES
	
	@Override
	public ObjMaskCollection createFromObjs(ObjMaskCollection objsIn) throws CreateException {
		return createFromObjs(
			objsIn,
			mask.create()
		);
	}
	
	protected abstract ObjMaskCollection createFromObjs(ObjMaskCollection objsIn, BinaryChnl mask) throws CreateException;

	public BinaryChnlProvider getMask() {
		return mask;
	}

	public void setMask(BinaryChnlProvider mask) {
		this.mask = mask;
	}
}
