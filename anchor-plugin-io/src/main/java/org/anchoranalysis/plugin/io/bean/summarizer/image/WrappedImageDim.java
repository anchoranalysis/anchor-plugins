package org.anchoranalysis.plugin.io.bean.summarizer.image;

import org.anchoranalysis.image.extent.ImageDim;

// Wrap with a nicer toString() representation
class WrappedImageDim implements Comparable<WrappedImageDim> {
	
	private ImageDim dim;
	
	public WrappedImageDim(ImageDim dim) {
		super();
		this.dim = dim;
	}

	@Override
	public String toString() {
		// Whether we display in 3d form or 2d is dependent on if there's more than 1 z-slice
		if (dim.getZ()>1) {
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