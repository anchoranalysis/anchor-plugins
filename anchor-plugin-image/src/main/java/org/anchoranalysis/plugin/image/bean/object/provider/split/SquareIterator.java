package org.anchoranalysis.plugin.image.bean.object.provider.split;

import java.util.Iterator;
import java.util.NoSuchElementException;
import org.anchoranalysis.core.geometry.Point3i;
import org.anchoranalysis.image.extent.BoundingBox;
import org.anchoranalysis.image.extent.Extent;

/**
 * Iterates over an extent in successive (mostly) fixed-sized squares in and x any y dimensions
 *
 * <p>Note if the squares don't evenly divide, then some rectangles also exist.
 *
 * @author Owen Feehan
 */
class SquareIterator implements Iterator<BoundingBox> {

    private final Extent extent;

    private final int squareSize;

    private final int extentZ;

    /** Number of corner points in the x-dimension */
    private final int numberX;

    /** Number of corner points in the y-dimension */
    private final int numberY;

    /** Corner point that is successively incremented during iteration */
    private Point3i corner;

    private int extentY;

    /**
     * Constructor
     *
     * @param extent extent to iterate over
     * @param squareSize size of square
     * @param cornerZ corner in z-dimension (which is not split)
     * @param extentZ extent in z-dimension (which is not split)
     */
    public SquareIterator(Extent extent, int squareSize, int cornerZ, int extentZ) {
        super();
        this.extent = extent;
        this.squareSize = squareSize;
        this.extentZ = extentZ;

        numberX = numberSplitsAlongDimension(extent.x(), squareSize);
        numberY = numberSplitsAlongDimension(extent.y(), squareSize);

        corner = new Point3i(0, 0, cornerZ);
        moveToY(0);
    }

    @Override
    public boolean hasNext() {
        return corner.x() < numberX || corner.y() < numberY;
    }

    @Override
    public BoundingBox next() {
        if (corner.x() == numberX) {
            corner.setX(0);
            corner.incrementY();

            if (corner.y() == numberY) {
                throw new NoSuchElementException();
            }

            moveToY(corner.y());
        }

        int startX = corner.x() * squareSize;
        int endX = Math.min(startX + squareSize, extent.x());

        // Special treatment for last square
        if (corner.x() == (numberX - 1)) {
            endX = Math.min(endX + squareSize, extent.x());
        }

        Extent squareExtent = new Extent(endX - startX, extentY, extentZ);

        return new BoundingBox(corner, squareExtent);
    }

    private void moveToY(int y) {
        int startY = y * squareSize;

        int endY = Math.min(startY + squareSize, extent.y());

        // Special treatment for last square
        if (y == (numberY - 1)) {
            endY = Math.min(endY + squareSize, extent.y());
        }

        extentY = endY - startY;
    }

    private static int numberSplitsAlongDimension(int extent, int squareSize) {
        int num = extent / squareSize;
        if (num != 0) {
            return num;
        } else {
            // We force at least one, to catch the remainder
            return 1;
        }
    }
}