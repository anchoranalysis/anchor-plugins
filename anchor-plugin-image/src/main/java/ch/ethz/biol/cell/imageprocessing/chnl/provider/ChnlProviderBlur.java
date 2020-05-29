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
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.image.bean.provider.ChnlProviderOne;
import org.anchoranalysis.image.chnl.Chnl;
import org.anchoranalysis.plugin.image.bean.blur.BlurGaussian3D;
import org.anchoranalysis.plugin.image.bean.blur.BlurStrategy;

/**
 * Blurs an image
 * 
 * <p>This is a mutable operation that alters the current image</p>
 * 
 * @author Owen Feehan
 *
 */
public class ChnlProviderBlur extends ChnlProviderOne {

	// START BEAN PROPERTIES
	@BeanField
	private BlurStrategy strategy = new BlurGaussian3D();
	// END BEAN PROPERTIES
	
	@Override
	public Chnl createFromChnl( Chnl chnl ) throws CreateException {
		
		try {
			strategy.blur(
				chnl.getVoxelBox(),
				chnl.getDimensions(),
				getLogger().getLogReporter()
			);
		} catch (OperationFailedException e) {
			throw new CreateException(e);
		}
		
		return chnl;
	}

	public BlurStrategy getStrategy() {
		return strategy;
	}

	public void setStrategy(BlurStrategy strategy) {
		this.strategy = strategy;
	}
}
