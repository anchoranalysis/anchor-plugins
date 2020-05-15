package ch.ethz.biol.cell.imageprocessing.objmask.provider;

import java.util.Optional;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.bean.provider.ObjMaskProvider;
import org.anchoranalysis.image.bean.provider.ObjMaskProviderOne;
import org.anchoranalysis.image.objmask.ObjMaskCollection;

public abstract class ObjMaskProviderContainer extends ObjMaskProviderOne {

	// START BEAN PROPERTIES
	@BeanField @OptionalBean
	private ObjMaskProvider objsContainer;
	// END BEAN PROPERTIES
	
	protected Optional<ObjMaskCollection> containerOptional() throws CreateException {
		if (objsContainer!=null) {
			return Optional.of(
				objsContainer.create()
			);
		} else {
			return Optional.empty();
		}
	}
	
	protected ObjMaskCollection containerRequired() throws CreateException {
		return containerOptional().orElseThrow(
			() -> new CreateException("An objsContainer must be defined for this provider")
		);
	}
	
	public ObjMaskProvider getObjsContainer() {
		return objsContainer;
	}

	public void setObjsContainer(ObjMaskProvider objsContainer) {
		this.objsContainer = objsContainer;
	}
}
