package ch.ethz.biol.cell.imageprocessing.chnl.provider;

/*
 * #%L
 * anchor-plugin-ij
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
import org.anchoranalysis.bean.annotation.Positive;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.bean.provider.ChnlProvider;
import org.anchoranalysis.image.chnl.Chnl;
import org.anchoranalysis.image.voxel.buffer.VoxelBuffer;

public class ChnlProviderIlluminationCorrectSingle extends ChnlProvider {


	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	// START BEAN PROPERTIES
	@BeanField
	private ChnlProvider chnlProvider;
	
	@BeanField @Positive
	private double sigma = 3;
	
	@BeanField
	private boolean do3D = true;
	// END BEAN PROPERTIES

	@Override
	public Chnl create() throws CreateException {
		
		Chnl chnl = chnlProvider.create();

		Chnl chnlBlurred = chnl.duplicate();
		chnlBlurred = ChnlProviderGaussianBlur.blur(chnlBlurred, sigma, do3D);
		
		int maxPixel = chnlBlurred.getVoxelBox().any().ceilOfMaxPixel();
	
		for( int z=0; z<chnl.getDimensions().getZ(); z++ ) {
			
			VoxelBuffer<?> vb = chnl.getVoxelBox().any().getPixelsForPlane(z);
			
			VoxelBuffer<?> vbBlurred = chnlBlurred.getVoxelBox().any().getPixelsForPlane(z);
			
			for( int y=0; y<chnl.getDimensions().getY(); y++ ) {
				for( int x=0; x<chnl.getDimensions().getX(); x++ ) {
					
					int offset = chnl.getDimensions().getExtnt().offset(x,y);
					
					int valIn = vb.getInt(offset);
					int div = vbBlurred.getInt(offset);
					
					div = Math.max(1, div);
					int valOut = (int) Math.round( ( (double) valIn / div) * maxPixel );
					vb.putInt(offset, valOut);
				}
			}
		}
		return chnl;
	}

	public ChnlProvider getChnlProvider() {
		return chnlProvider;
	}

	public void setChnlProvider(ChnlProvider chnlProvider) {
		this.chnlProvider = chnlProvider;
	}

	public double getSigma() {
		return sigma;
	}

	public void setSigma(double sigma) {
		this.sigma = sigma;
	}

	public boolean isDo3D() {
		return do3D;
	}

	public void setDo3D(boolean do3d) {
		do3D = do3d;
	}
}
