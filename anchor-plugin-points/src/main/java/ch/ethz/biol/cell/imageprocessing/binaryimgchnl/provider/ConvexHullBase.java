package ch.ethz.biol.cell.imageprocessing.binaryimgchnl.provider;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.bean.provider.BinaryImgChnlProviderOne;
import org.anchoranalysis.image.binary.BinaryChnl;
import org.anchoranalysis.image.outline.FindOutline;

public abstract class ConvexHullBase extends BinaryImgChnlProviderOne {

	// START BEAN PROPERTIES
	@BeanField
	private boolean erodeEdges = false;
	// END BEAN PROPERTIES
	
	@Override
	public BinaryChnl createFromChnl( BinaryChnl chnlIn ) throws CreateException {
		return createFromChnl(
			chnlIn,
			FindOutline.outline(chnlIn, true, erodeEdges)
		);
	}
	
	protected abstract BinaryChnl createFromChnl(BinaryChnl chnlIn, BinaryChnl outline) throws CreateException;
		
	public boolean isErodeEdges() {
		return erodeEdges;
	}

	public void setErodeEdges(boolean erodeEdges) {
		this.erodeEdges = erodeEdges;
	}
}
