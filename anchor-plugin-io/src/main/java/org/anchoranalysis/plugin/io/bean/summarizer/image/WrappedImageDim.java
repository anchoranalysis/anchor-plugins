package org.anchoranalysis.plugin.io.bean.summarizer.image;

/*-
 * #%L
 * anchor-plugin-io
 * %%
 * Copyright (C) 2010 - 2020 Owen Feehan, ETH Zurich, University of Zurich, Hoffmann la Roche
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

import org.anchoranalysis.image.extent.ImageDimensions;

import lombok.AllArgsConstructor;

// Wrap with a nicer toString() representation
@AllArgsConstructor
class WrappedImageDim implements Comparable<WrappedImageDim> {
	
	private final ImageDimensions dimensions;
	
	@Override
	public String toString() {
		// Whether we display in 3d form or 2d is dependent on if there's more than 1 z-slice
		if (dimensions.getZ()>1) {
			return String.format("%dx%dx%d", dimensions.getX(), dimensions.getY(), dimensions.getZ() );
		} else {
			return String.format("%dx%d", dimensions.getX(), dimensions.getY() );
		}
	}

	public int hashCode() {
		return dimensions.hashCode();
	}

	@Override
	public boolean equals(Object obj) {

		if (obj == null) { return false; }
		if (obj == this) { return true; }
		if (obj.getClass() != getClass()) {
			return false;
		}
		
		WrappedImageDim objCast = (WrappedImageDim) obj;
		return dimensions.equals(objCast.dimensions);
	}

	@Override
	public int compareTo(WrappedImageDim other) {
		// Order by volume, smaller first
		return Long.compare(dimensions.getVolume(), other.dimensions.getVolume() );
	}
}
