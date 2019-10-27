package ch.ethz.biol.cell.mpp.probmap.provider;

/*-
 * #%L
 * anchor-plugin-mpp
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

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.geometry.Point3d;
import org.anchoranalysis.core.random.RandomNumberGenerator;
import org.anchoranalysis.image.bean.provider.ImageDimProvider;
import org.anchoranalysis.image.binary.BinaryChnl;
import org.anchoranalysis.image.binary.values.BinaryValues;
import org.anchoranalysis.image.chnl.Chnl;
import org.anchoranalysis.image.chnl.factory.ChnlFactoryByte;
import org.anchoranalysis.image.extent.ImageDim;

import ch.ethz.biol.cell.mpp.pair.IUpdatableMarkSet;
import ch.ethz.biol.cell.mpp.probmap.ProbMap;
import ch.ethz.biol.cell.mpp.probmap.ProbMapUtilities;

public class ProbMapProviderCreateUniform extends ProbMapProvider {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5827024413979756723L;
	
	// START BEAN PROPERTIES
	@BeanField
	private ImageDimProvider dimProvider;
	// END BEAN PROPERTIES
		
	public ProbMapProviderCreateUniform() {
	}

	@Override
	public String getBeanDscr() {
		return getBeanName();
	}
	
	@Override
	public ProbMap create() throws CreateException {
		return new ProbMapUniform( dimProvider.create() );
	}
	
	
	public static class ProbMapUniform extends ProbMap { 
	
		private ImageDim dim;
		
		public ProbMapUniform(ImageDim dim) {
			super();
			this.dim = dim;
		}

		@Override
		public ImageDim getDimensions() {
			return dim;
		}
	
		@Override
		public Point3d sample(RandomNumberGenerator re) {
			return ProbMapUtilities.getUniformRandomPnt( re, getDimensions() );
		}
	
		@Override
		public BinaryChnl visualization() {
	
			BinaryValues bv = BinaryValues.getDefault();
			
			Chnl chnl = new ChnlFactoryByte().createEmptyInitialised(dim);
			chnl.getVoxelBox().any().setAllPixelsTo( bv.getOnInt() );
			return new BinaryChnl(chnl, bv);
		}

		@Override
		public IUpdatableMarkSet updater() {
			return null;
		}
	}


	public ImageDimProvider getDimProvider() {
		return dimProvider;
	}

	public void setDimProvider(ImageDimProvider dimProvider) {
		this.dimProvider = dimProvider;
	}
}
