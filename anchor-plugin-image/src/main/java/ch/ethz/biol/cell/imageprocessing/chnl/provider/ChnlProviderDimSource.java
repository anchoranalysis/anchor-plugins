package ch.ethz.biol.cell.imageprocessing.chnl.provider;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.bean.provider.ChnlProvider;
import org.anchoranalysis.image.bean.provider.ImageDimProvider;
import org.anchoranalysis.image.chnl.Chnl;
import org.anchoranalysis.image.extent.ImageDim;

import ch.ethz.biol.cell.imageprocessing.dim.provider.GuessDimFromInputImage;

public abstract class ChnlProviderDimSource extends ChnlProvider {

	// START BEAN PROPERTIES
	@BeanField
	private ImageDimProvider dim = new GuessDimFromInputImage();
	// END BEAN PROPERTIES
	
	@Override
	public Chnl create() throws CreateException {
		return createFromDim(
			dim.create()
		);
	}
	
	protected abstract Chnl createFromDim(ImageDim dim) throws CreateException;

	public ImageDimProvider getDim() {
		return dim;
	}

	public void setDim(ImageDimProvider dim) {
		this.dim = dim;
	}
}
