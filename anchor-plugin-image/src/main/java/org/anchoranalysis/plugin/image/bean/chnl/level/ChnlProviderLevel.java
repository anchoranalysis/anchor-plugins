package org.anchoranalysis.plugin.image.bean.chnl.level;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.bean.provider.ChnlProvider;
import org.anchoranalysis.image.bean.provider.ChnlProviderOne;
import org.anchoranalysis.image.bean.provider.ObjMaskProvider;
import org.anchoranalysis.image.bean.threshold.CalculateLevel;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.objectmask.ObjectMaskCollection;

import ch.ethz.biol.cell.imageprocessing.chnl.provider.DimChecker;

public abstract class ChnlProviderLevel extends ChnlProviderOne {

	// START BEAN PROPERTIES
	/** The input (an intensity channel in the normal way) */
	@BeanField
	private ObjMaskProvider objs;
	
	@BeanField
	private ChnlProvider chnlOutput;
	
	@BeanField
	private CalculateLevel calculateLevel;
	// END BEAN PROPERTIES
	
	@Override
	public Channel createFromChnl(Channel chnl) throws CreateException {
				
		return createFor(
			chnl,
			objs.create(),
			DimChecker.createSameSize(chnlOutput, "chnlOutput", chnl)
			
		);
	}
	
	protected abstract Channel createFor( Channel chnlIntensity, ObjectMaskCollection objs, Channel chnlOutput ) throws CreateException;
	
	public ObjMaskProvider getObjs() {
		return objs;
	}
	
	
	public void setObjs(ObjMaskProvider objs) {
		this.objs = objs;
	}

	public ChnlProvider getChnlOutput() {
		return chnlOutput;
	}

	public void setChnlOutput(ChnlProvider chnlOutput) {
		this.chnlOutput = chnlOutput;
	}
	
	public CalculateLevel getCalculateLevel() {
		return calculateLevel;
	}

	public void setCalculateLevel(CalculateLevel calculateLevel) {
		this.calculateLevel = calculateLevel;
	}
}
