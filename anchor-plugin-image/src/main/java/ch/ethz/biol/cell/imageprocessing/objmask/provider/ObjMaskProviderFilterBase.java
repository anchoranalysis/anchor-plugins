package ch.ethz.biol.cell.imageprocessing.objmask.provider;

import java.util.Optional;

import org.anchoranalysis.bean.OptionalFactory;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.image.bean.objmask.filter.ObjMaskFilter;
import org.anchoranalysis.image.bean.provider.ObjMaskProvider;
import org.anchoranalysis.image.extent.ImageDim;
import org.anchoranalysis.image.objectmask.ObjectMaskCollection;

public abstract class ObjMaskProviderFilterBase extends ObjMaskProviderDimensionsOptional {

	// START BEAN PROPERTIES
	@BeanField
	private ObjMaskFilter filter;
	
	@BeanField @OptionalBean
	private ObjMaskProvider objsRejected;	// The rejected objects are put here (OPTIONAL)
	// END BEAN PROPERTIES
	
	@Override
	public ObjectMaskCollection createFromObjs(ObjectMaskCollection in) throws CreateException {
		return createFromObjs(
			in,
			OptionalFactory.create(objsRejected),
			createDims()
		);
	}
	
	protected void filter(ObjectMaskCollection objs, Optional<ImageDim> dim, Optional<ObjectMaskCollection> objsRejected) throws CreateException {
		try {
			filter.filter(objs, dim, objsRejected);
		} catch (OperationFailedException e) {
			throw new CreateException(e);
		}
	}
	
	protected abstract ObjectMaskCollection createFromObjs(
		ObjectMaskCollection in,
		Optional<ObjectMaskCollection> omcRejected,
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
