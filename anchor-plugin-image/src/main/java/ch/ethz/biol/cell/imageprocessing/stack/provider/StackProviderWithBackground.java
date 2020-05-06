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
import org.anchoranalysis.image.chnl.Chnl;
import org.anchoranalysis.image.stack.DisplayStack;

public abstract class StackProviderWithBackground extends StackProvider {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// START BEAN PROPERTIES
	
	// Either chnlProviderBackground or stackProviderBackground should be non-null
	//  but not both
	@BeanField @OptionalBean
	private ChnlProvider chnlProviderBackground;
	
	@BeanField @OptionalBean
	private StackProvider stackProviderBackground;
	
	@BeanField @OptionalBean
	private ChnlProvider chnlProviderBackgroundMIP;
	// END BEAN PROPERTIES
	
	
	@Override
	public void checkMisconfigured(BeanInstanceMap defaultInstances) throws BeanMisconfiguredException {
		super.checkMisconfigured(defaultInstances);
		if (chnlProviderBackground==null && stackProviderBackground==null) {
			throw new BeanMisconfiguredException("Either chnlProviderBackground or stackProviderBackground should be set");
		}
		
		if (chnlProviderBackground!=null && stackProviderBackground!=null) {
			throw new BeanMisconfiguredException("Only one of chnlProviderBackground and stackProviderBackground should be set");
		}
	}

	protected DisplayStack backgroundStack(boolean do3D) throws CreateException {
		if (stackProviderBackground!=null) {
			return DisplayStack.create(
				stackProviderBackground.createStack()
			);
			
		} else {
			return DisplayStack.create(
				backgroundChnl(do3D )
			);
		}
	}

	private Chnl backgroundChnl(boolean do3D) throws CreateException {
		if (do3D) {
			return chnlProviderBackground.create();
		} else {
			
			if (chnlProviderBackgroundMIP!=null) {
				return chnlProviderBackgroundMIP.create();
			} else {
				return chnlProviderBackground.create().maxIntensityProj();
			}
		}
	}
	
	public ChnlProvider getChnlProviderBackground() {
		return chnlProviderBackground;
	}

	public void setChnlProviderBackground(ChnlProvider chnlProviderBackground) {
		this.chnlProviderBackground = chnlProviderBackground;
	}
	

	public StackProvider getStackProviderBackground() {
		return stackProviderBackground;
	}

	public void setStackProviderBackground(StackProvider stackProviderBackground) {
		this.stackProviderBackground = stackProviderBackground;
	}
	

	public ChnlProvider getChnlProviderBackgroundMIP() {
		return chnlProviderBackgroundMIP;
	}

	public void setChnlProviderBackgroundMIP(ChnlProvider chnlProviderBackgroundMIP) {
		this.chnlProviderBackgroundMIP = chnlProviderBackgroundMIP;
	}
}
