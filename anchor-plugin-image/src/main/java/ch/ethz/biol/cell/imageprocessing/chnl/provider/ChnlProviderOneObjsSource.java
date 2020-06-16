package ch.ethz.biol.cell.imageprocessing.chnl.provider;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.bean.provider.ChnlProviderOne;
import org.anchoranalysis.image.bean.provider.ObjMaskProvider;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.objectmask.ObjectMaskCollection;

public abstract class ChnlProviderOneObjsSource extends ChnlProviderOne {

	// START BEAN PROPERTIES
	@BeanField
	private ObjMaskProvider objs;
	// END BEAN PROPERTIES

	@Override
	public Channel createFromChnl( Channel chnl ) throws CreateException {
		return createFromChnl(
			chnl,
			objs.create()
		);
	}
	
	protected abstract Channel createFromChnl(Channel chnl, ObjectMaskCollection objsSource) throws CreateException;
	
	public ObjMaskProvider getObjs() {
		return objs;
	}

	public void setObjs(ObjMaskProvider objs) {
		this.objs = objs;
	}
}
