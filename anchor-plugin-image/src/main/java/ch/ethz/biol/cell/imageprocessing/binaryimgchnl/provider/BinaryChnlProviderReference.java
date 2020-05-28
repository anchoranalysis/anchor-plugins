package ch.ethz.biol.cell.imageprocessing.binaryimgchnl.provider;

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
import org.anchoranalysis.image.bean.provider.BinaryChnlProvider;
import org.anchoranalysis.image.binary.BinaryChnl;

public class BinaryChnlProviderReference extends BinaryChnlProvider {

	// START BEAN PROPERTIES
	@BeanField
	private String id = "";
	
	/** If true the channel is duplicated after it is retrieved, to prevent overwriting exisiting data
	 *  This is a shortcut to avoid embedding beans in a ChnlProviderDuplicate
	 */
	@BeanField
	private boolean duplicate=false;
	// END BEAN PROPERTIES
	
	private BinaryChnl bi;

	
	public BinaryChnlProviderReference() {
		
	}
	
	public BinaryChnlProviderReference(String id) {
		super();
		this.id = id;
	}

	@Override
	public BinaryChnl create() throws CreateException {
		assert( this.isHasBeenInit() );
		if (bi==null) {
			bi = createChnl();
		}
		return bi;
	}
	
	private BinaryChnl createChnl() throws CreateException {
		try {
			BinaryChnl chnl = getSharedObjects().getBinaryImageCollection().getException(id);
			
			if (duplicate) {
				return chnl.duplicate();
			} else {
				return chnl;
			}
		} catch (NamedProviderGetException e) {
			throw new CreateException(e);
		}	
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public boolean isDuplicate() {
		return duplicate;
	}

	public void setDuplicate(boolean duplicate) {
		this.duplicate = duplicate;
	}
}