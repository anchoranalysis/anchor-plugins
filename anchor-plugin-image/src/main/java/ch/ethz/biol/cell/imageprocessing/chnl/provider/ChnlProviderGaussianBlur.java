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


import net.imglib2.algorithm.gauss3.Gauss3;
import net.imglib2.exception.IncompatibleTypeException;
import net.imglib2.img.NativeImg;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.view.Views;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.Positive;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.bean.provider.ChnlProvider;
import org.anchoranalysis.image.chnl.Chnl;
import org.anchoranalysis.image.convert.ImageUnitConverter;
import org.anchoranalysis.image.convert.ImgLib2Wrap;
import org.anchoranalysis.image.extent.Extent;
import org.anchoranalysis.image.extent.ImageDim;
import org.anchoranalysis.image.extent.ImageRes;
import org.anchoranalysis.image.orientation.DirectionVector;

@SuppressWarnings("unused")
public class ChnlProviderGaussianBlur extends ChnlProvider {


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
	
	@BeanField
	private boolean sigmaInMeters = false;	// Treats sigma if it's microns
	// END BEAN PROPERTIES
	
	private static <T extends NumericType<T>,S> void doBlur( NativeImg<T,S> img, ImageRes sr, double[] sigma ) throws IncompatibleTypeException {
		Gauss3.gauss(sigma, Views.extendMirrorSingle(img), img );
	}
	
	// Assumes XY res are identical
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static Chnl blur( Chnl chnl, double sigma, boolean do3D ) throws CreateException {
		
		Extent e = chnl.getDimensions().getExtnt();
		try {
			if (do3D) {
				double[] sigmaArr = new double[]{ sigma, sigma, sigma/chnl.getDimensions().getRes().getZRelRes() };
				NativeImg img = ImgLib2Wrap.wrap( chnl.getVoxelBox(), true );
				doBlur(img,chnl.getDimensions().getRes(),sigmaArr);
			} else {
				
				double[] sigmaArr = new double[]{ sigma, sigma };
				
				for( int z=0; z<chnl.getDimensions().getZ(); z++) {
					NativeImg img = ImgLib2Wrap.wrap( chnl.getVoxelBox().any().getPixelsForPlane(z), e );
					doBlur(img,chnl.getDimensions().getRes(),sigmaArr);
				}
			}
			return chnl;
		} catch (IncompatibleTypeException e1) {
			throw new CreateException(e1);
		}
	}
	
	@Override
	public Chnl create() throws CreateException {
		
		Chnl chnl = chnlProvider.create();
		
		double sigmaToUse = calcSigma( chnl.getDimensions() );
		
		return blur( chnl, sigmaToUse, do3D );
	}
	
	private double calcSigma( ImageDim dim ) throws CreateException {
		
		double sigmaToUse = sigma;
		
		if (sigmaInMeters) {
			// Then we reconcile our sigma in microns against the Pixel Size XY (Z is taken care of later)
			sigmaToUse = ImageUnitConverter.convertFromMeters( sigma, dim.getRes() );
			
			getLogger().getLogReporter().logFormatted("Converted sigmaInMeters=%f into sigma=%f", sigma, sigmaToUse);
		} 
		
		if (sigmaToUse > dim.getX() || sigmaToUse > dim.getY()) {
			throw new CreateException("The calculated sigma is FAR TOO LARGE. It is larger than the entire channel it is applied to");
		}
		
		return sigmaToUse;
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


	public boolean isSigmaInMeters() {
		return sigmaInMeters;
	}


	public void setSigmaInMeters(boolean sigmaInMeters) {
		this.sigmaInMeters = sigmaInMeters;
	}


}
