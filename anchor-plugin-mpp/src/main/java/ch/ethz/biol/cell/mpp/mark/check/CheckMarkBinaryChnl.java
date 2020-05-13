package ch.ethz.biol.cell.mpp.mark.check;

import java.util.function.Function;

import org.anchoranalysis.anchor.mpp.feature.bean.mark.CheckMark;
import org.anchoranalysis.anchor.mpp.feature.error.CheckException;
import org.anchoranalysis.anchor.mpp.mark.Mark;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.geometry.Point3d;
import org.anchoranalysis.core.geometry.Point3i;
import org.anchoranalysis.feature.nrg.NRGStackWithParams;
import org.anchoranalysis.image.bean.provider.BinaryChnlProvider;
import org.anchoranalysis.image.binary.BinaryChnl;

public abstract class CheckMarkBinaryChnl extends CheckMark {

	// START BEAN PROPERTIES
	@BeanField
	private BinaryChnlProvider binaryChnl;
	
	@BeanField
	private boolean acceptOutsideScene = false;
	// END BEAN PROPERTIES

	protected BinaryChnl createChnl() throws CheckException {
		try {
			return binaryChnl.create();
		} catch (CreateException e) {
			throw new CheckException(
				String.format("Cannot create binary image (from provider %s)", binaryChnl),
				e 
			);
		}
	}
	
	protected boolean isPointOnBinaryChnl( Point3d cp, NRGStackWithParams nrgStack, Function<Point3d,Point3i> deriveFunc ) throws CheckException {

		if (!nrgStack.getDimensions().contains(cp)) {
			return acceptOutsideScene;
		}
		
		BinaryChnl bi = createChnl();
		return bi.isPointOn(
			deriveFunc.apply(cp)
		);
	}
	
	@Override
	public boolean isCompatibleWith(Mark testMark) {
		return true;
	}
	
	public BinaryChnlProvider getBinaryChnl() {
		return binaryChnl;
	}

	public void setBinaryChnl(BinaryChnlProvider binaryChnl) {
		this.binaryChnl = binaryChnl;
	}
}
