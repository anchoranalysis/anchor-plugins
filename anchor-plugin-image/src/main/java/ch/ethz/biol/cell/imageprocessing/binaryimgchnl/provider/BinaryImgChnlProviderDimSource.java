package ch.ethz.biol.cell.imageprocessing.binaryimgchnl.provider;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.bean.provider.BinaryChnlProvider;
import org.anchoranalysis.image.bean.provider.ImageDimProvider;
import org.anchoranalysis.image.binary.BinaryChnl;
import org.anchoranalysis.image.extent.ImageDim;

import ch.ethz.biol.cell.imageprocessing.dim.provider.GuessDimFromInputImage;

public abstract class BinaryImgChnlProviderDimSource extends BinaryChnlProvider {

	// START BEAN PROPERTIES
	@BeanField
	private ImageDimProvider dim = new GuessDimFromInputImage();
	// END BEAN PROPERTIES
	
	@Override
	public BinaryChnl create() throws CreateException {
		return createFromSource(
			dim.create()
		);
	}
	
	protected abstract BinaryChnl createFromSource(ImageDim dimSource) throws CreateException;

	public ImageDimProvider getDim() {
		return dim;
	}

	public void setDim(ImageDimProvider dim) {
		this.dim = dim;
	}

}
