/* (C)2020 */
package org.anchoranalysis.plugin.io.bean.summarizer.image;

import lombok.AllArgsConstructor;
import org.anchoranalysis.image.extent.ImageDimensions;

// Wrap with a nicer toString() representation
@AllArgsConstructor
class WrappedImageDim implements Comparable<WrappedImageDim> {

    private final ImageDimensions dimensions;

    @Override
    public String toString() {
        // Whether we display in 3d form or 2d is dependent on if there's more than 1 z-slice
        if (dimensions.getZ() > 1) {
            return String.format(
                    "%dx%dx%d", dimensions.getX(), dimensions.getY(), dimensions.getZ());
        } else {
            return String.format("%dx%d", dimensions.getX(), dimensions.getY());
        }
    }

    public int hashCode() {
        return dimensions.hashCode();
    }

    @Override
    public boolean equals(Object obj) {

        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }

        WrappedImageDim objCast = (WrappedImageDim) obj;
        return dimensions.equals(objCast.dimensions);
    }

    @Override
    public int compareTo(WrappedImageDim other) {
        // Order by volume, smaller first
        return Long.compare(dimensions.getVolume(), other.dimensions.getVolume());
    }
}
