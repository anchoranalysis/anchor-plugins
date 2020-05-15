package ch.ethz.biol.cell.imageprocessing.chnl.provider;

/*
 * #%L
 * anchor-plugin-image
 * %%
 * Copyright (C) 2016 ETH Zurich, University of Zurich, Owen Feehan
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


import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.name.provider.NamedProviderGetException;
import org.anchoranalysis.image.bean.provider.ChnlProvider;
import org.anchoranalysis.image.chnl.Chnl;
import org.anchoranalysis.image.stack.Stack;

public class ChnlProviderStackReference extends ChnlProvider {

	// START
	@BeanField
	private int chnlIndex = 0;
	
	@BeanField
	private String stackProviderID;
	// END
	
	private Chnl chnl;

	@Override
	public Chnl create() throws CreateException {
		
		try {
			if (chnl==null) {
				Stack stack = getSharedObjects().getStackCollection().getException(stackProviderID);
				
				chnl = stack.getChnl(chnlIndex);
				if (chnl==null) {
					throw new CreateException( String.format("chnl %d cannot be found", chnlIndex));
				}
			}
		} catch (NamedProviderGetException e) {
			throw new CreateException(e);
		}
		
		
		return chnl;
	}

	public int getChnlIndex() {
		return chnlIndex;
	}

	public void setChnlIndex(int chnlIndex) {
		this.chnlIndex = chnlIndex;
	}

	public String getStackProviderID() {
		return stackProviderID;
	}

	public void setStackProviderID(String stackProviderID) {
		this.stackProviderID = stackProviderID;
	}

	public Chnl getChnl() {
		return chnl;
	}

	public void setChnl(Chnl chnl) {
		this.chnl = chnl;
	}

}
