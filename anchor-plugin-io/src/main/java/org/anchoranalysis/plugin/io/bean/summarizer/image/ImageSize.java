package org.anchoranalysis.plugin.io.bean.summarizer.image;

/*-
 * #%L
 * anchor-plugin-io
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

import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.image.extent.ImageDim;
import org.anchoranalysis.image.io.RasterIOException;
import org.anchoranalysis.image.io.input.NamedChnlsInputAsStack;
import org.anchoranalysis.plugin.io.bean.summarizer.Summarizer;
import org.anchoranalysis.plugin.io.summarizer.FrequencyMap;

/**
 * Summarizes the size of images.
 * 
 * If there's more than one image in the series, the size of each is considered.
 * 
 **/
public class ImageSize extends Summarizer<NamedChnlsInputAsStack> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	// Whether at least one image encountered is 3D?
	private boolean atLeastOne3D = false;
	
	// Wrap with a nicer toString() representation
	private class WrappedImageDim implements Comparable<WrappedImageDim> {
		
		private ImageDim dim;
		
		public WrappedImageDim(ImageDim dim) {
			super();
			this.dim = dim;
		}

		@Override
		public String toString() {
			// Whether we display in 3d form or 2d is dependent on whether we've seen any 3d images
			if (atLeastOne3D) {
				return String.format("%dx%dx%d", dim.getX(), dim.getY(), dim.getZ() );
			} else {
				return String.format("%dx%d", dim.getX(), dim.getY() );
			}
		}

		public int hashCode() {
			return dim.hashCode();
		}

		@Override
		public boolean equals(Object obj) {

			if (obj == null) { return false; }
			if (obj == this) { return true; }
			if (obj.getClass() != getClass()) {
				return false;
			}
			
			WrappedImageDim objCast = (WrappedImageDim) obj;
			return dim.equals(objCast.dim);
		}

		@Override
		public int compareTo(WrappedImageDim othr) {
			// Order by volume, smaller first
			return Integer.compare(dim.getVolume(), othr.dim.getVolume() );
		}
	}
		
	private FrequencyMap<WrappedImageDim> map = new FrequencyMap<>();
	
	@Override
	public void add( NamedChnlsInputAsStack img ) throws OperationFailedException {

		try {
			int numSeries = img.numSeries();
			for( int i=0; i<numSeries; i++ ) {
			
				ImageDim dim = img.dim(0);
				
				if (dim.getZ() > 1) {
					atLeastOne3D = true;
				}
				
				map.incrCount( new WrappedImageDim(dim) );
			}
			
		} catch (RasterIOException exc) {
			throw new OperationFailedException(exc);
		}
	}
	
	// Describes all the extensions found
	@Override
	public String describe() {
		return map.describe("size");
	}
}
