package org.anchoranalysis.plugin.image.bean.chnl.level;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.bean.provider.ChnlProvider;
import org.anchoranalysis.image.bean.provider.ObjMaskProvider;
import org.anchoranalysis.image.chnl.Chnl;
import org.anchoranalysis.image.objmask.ObjMaskCollection;

public abstract class ChnlProviderLevel extends ChnlProvider {

	// START BEAN PROPERTIES
	@BeanField
	private ChnlProvider chnlProviderIntensity;
	
	@BeanField
	private ObjMaskProvider objs;
	
	@BeanField
	private ChnlProvider chnlProviderOutput;
	// END BEAN PROPERTIES
	
	@Override
	public Chnl create() throws CreateException {
		return createFor(
			chnlProviderIntensity.create(),
			objs.create(),
			chnlProviderOutput.create()
			
		);
	}
	
	protected abstract Chnl createFor( Chnl chnlIntensity, ObjMaskCollection objs, Chnl chnlOutput ) throws CreateException;
	
	public ChnlProvider getChnlProviderOutput() {
		return chnlProviderOutput;
	}
		
	public void setChnlProviderOutput(ChnlProvider chnlProviderOutput) {
		this.chnlProviderOutput = chnlProviderOutput;
	}
		
	public ChnlProvider getChnlProviderIntensity() {
		return chnlProviderIntensity;
	}
		
	public void setChnlProviderIntensity(ChnlProvider chnlProviderIntensity) {
		this.chnlProviderIntensity = chnlProviderIntensity;
	}
	
	public ObjMaskProvider getObjs() {
		return objs;
	}
	
	
	public void setObjs(ObjMaskProvider objs) {
		this.objs = objs;
	}
}
