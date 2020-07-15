package org.anchoranalysis.plugin.image.bean.object.provider.morphological;

import java.util.Optional;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.extent.Extent;
import org.anchoranalysis.image.extent.ImageDimensions;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.plugin.image.bean.object.provider.ObjectCollectionProviderWithOptionalDimensions;

import lombok.Getter;
import lombok.Setter;

public abstract class ObjectCollectionProviderMorphological extends ObjectCollectionProviderWithOptionalDimensions {

	// START BEAN PROPERTIES
	@BeanField @Getter @Setter
	private boolean do3D = false;
	
	@BeanField @Getter @Setter
	private int iterations = 1;
	// END BEAN PROPERTIES
	
	@Override
	public ObjectCollection createFromObjects(ObjectCollection objects) throws CreateException {
		Optional<Extent> extent = calcExtent();
		return objects.stream().map(
			objectMask -> applyMorphologicalOperation(objectMask, extent)
		);
	}
	
	protected abstract ObjectMask applyMorphologicalOperation(ObjectMask object, Optional<Extent> extent) throws CreateException;
	
	protected Optional<Extent> calcExtent() throws CreateException {
		return createDims().map(
			ImageDimensions::getExtent	
		);
	}
}
