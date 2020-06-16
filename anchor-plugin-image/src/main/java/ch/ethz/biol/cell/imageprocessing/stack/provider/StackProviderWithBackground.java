package ch.ethz.biol.cell.imageprocessing.stack.provider;

/*-
 * #%L
 * anchor-plugin-image
 * %%
 * Copyright (C) 2010 - 2019 Owen Feehan, ETH Zurich, University of Zurich, Hoffmann la Roche
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */

import org.anchoranalysis.bean.BeanInstanceMap;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.bean.error.BeanMisconfiguredException;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.bean.provider.ChnlProvider;
import org.anchoranalysis.image.bean.provider.stack.StackProvider;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.stack.DisplayStack;

public abstract class StackProviderWithBackground extends StackProvider {

	

	// START BEAN PROPERTIES
	
	// Either chnlProviderBackground or stackProviderBackground should be non-null
	//  but not both
	@BeanField @OptionalBean
	private ChnlProvider chnlBackground;
	
	@BeanField @OptionalBean
	private StackProvider stackBackground;
	
	@BeanField @OptionalBean
	private ChnlProvider chnlBackgroundMIP;
	// END BEAN PROPERTIES
	
	
	@Override
	public void checkMisconfigured(BeanInstanceMap defaultInstances) throws BeanMisconfiguredException {
		super.checkMisconfigured(defaultInstances);
		if (chnlBackground==null && stackBackground==null) {
			throw new BeanMisconfiguredException("Either chnlBackground or stackBackground should be set");
		}
		
		if (chnlBackground!=null && stackBackground!=null) {
			throw new BeanMisconfiguredException("Only one of chnlBackground and stackBackground should be set");
		}
	}

	protected DisplayStack backgroundStack(boolean do3D) throws CreateException {
		if (stackBackground!=null) {
			return DisplayStack.create(
				stackBackground.createStack()
			);
			
		} else {
			return DisplayStack.create(
				backgroundChnl(do3D )
			);
		}
	}

	private Channel backgroundChnl(boolean do3D) throws CreateException {
		if (do3D) {
			return chnlBackground.create();
		} else {
			
			if (chnlBackgroundMIP!=null) {
				return chnlBackgroundMIP.create();
			} else {
				return chnlBackground.create().maxIntensityProjection();
			}
		}
	}

	public ChnlProvider getChnlBackground() {
		return chnlBackground;
	}

	public void setChnlBackground(ChnlProvider chnlBackground) {
		this.chnlBackground = chnlBackground;
	}

	public StackProvider getStackBackground() {
		return stackBackground;
	}

	public void setStackBackground(StackProvider stackBackground) {
		this.stackBackground = stackBackground;
	}

	public ChnlProvider getChnlBackgroundMIP() {
		return chnlBackgroundMIP;
	}

	public void setChnlBackgroundMIP(ChnlProvider chnlBackgroundMIP) {
		this.chnlBackgroundMIP = chnlBackgroundMIP;
	}
}
