package ch.ethz.biol.cell.imageprocessing.objmask.provider;

import java.util.Optional;

import org.anchoranalysis.bean.ProviderNullableCreator;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.image.bean.objmask.filter.ObjMaskFilter;
import org.anchoranalysis.image.bean.provider.ObjMaskProvider;
import org.anchoranalysis.image.extent.ImageDim;
import org.anchoranalysis.image.objmask.ObjMaskCollection;

public abstract class ObjMaskProviderFilterBase extends ObjMaskProviderDimensionsOptional {

	// START BEAN PROPERTIES
	@BeanField
	private ObjMaskFilter filter;
	
	@BeanField @OptionalBean
	private ObjMaskProvider objsRejected;	// The rejected objects are put here (OPTIONAL)
	// END BEAN PROPERTIES
	
	@Override
	public ObjMaskCollection createFromObjs(ObjMaskCollection in) throws CreateException {
		return createFromObjs(
			in,
			ProviderNullableCreator.createOptional(objsRejected),
			createDims()
		);
	}
	
	protected void filter(ObjMaskCollection objs, Optional<ImageDim> dim, Optional<ObjMaskCollection> objsRejected) throws CreateException {
		try {
			filter.filter(objs, dim, objsRejected);
		} catch (OperationFailedException e) {
			throw new CreateException(e);
		}
	}
	
	protected abstract ObjMaskCollection createFromObjs(
		ObjMaskCollection in,
		Optional<ObjMaskCollection> omcRejected,
		Optional<ImageDim> dim
	) throws CreateException;


	public ObjMaskProvider getObjsRejected() {
		return objsRejected;
	}

	public void setObjsRejected(ObjMaskProvider objsRejected) {
		this.objsRejected = objsRejected;
	}

	public ObjMaskFilter getFilter() {
		return filter;
	}

	public void setFilter(ObjMaskFilter filter) {
		this.filter = filter;
	}
}
