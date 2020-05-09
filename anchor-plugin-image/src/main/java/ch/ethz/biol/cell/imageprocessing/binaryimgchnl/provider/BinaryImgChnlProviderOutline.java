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
import org.anchoranalysis.image.bean.provider.BinaryImgChnlProviderOne;
import org.anchoranalysis.image.binary.BinaryChnl;
import org.anchoranalysis.image.outline.FindOutline;

/**
 * Always creates a new BinaryImgChnl for the result (no need to duplicate input)
 * 
 * @author feehano
 *
 */
public class BinaryImgChnlProviderOutline extends BinaryImgChnlProviderOne {

	// START
	@BeanField
	private boolean force2D = false;
	
	@BeanField
	private boolean erodeEdges = true;
	// END
	

	@Override
	public BinaryChnl createFromChnl( BinaryChnl chnl ) throws CreateException {
		boolean do3D = chnl.getDimensions().getZ() > 1 && !force2D;
		return FindOutline.outline(chnl, do3D, erodeEdges);
	}

	public boolean isForce2D() {
		return force2D;
	}

	public void setForce2D(boolean force2d) {
		force2D = force2d;
	}

	public boolean isErodeEdges() {
		return erodeEdges;
	}

	public void setErodeEdges(boolean erodeEdges) {
		this.erodeEdges = erodeEdges;
	}


}
