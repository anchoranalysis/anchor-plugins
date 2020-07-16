/* (C)2020 */
package org.anchoranalysis.plugin.image.bean.object.provider.split;

import java.nio.ByteBuffer;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.Positive;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.geometry.Point3i;
import org.anchoranalysis.image.bean.provider.ObjectCollectionProviderUnary;
import org.anchoranalysis.image.extent.BoundingBox;
import org.anchoranalysis.image.extent.Extent;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.image.voxel.box.VoxelBox;
import org.anchoranalysis.image.voxel.box.factory.VoxelBoxFactory;

// Chops objects up into squares (or mostly squares, sometimes rectangles at the very end)
// Only chops in X and Y, Z is unaffected
public class SplitIntoSquares extends ObjectCollectionProviderUnary {

    // START BEAN PROPERTIES
    @BeanField @Positive @Getter @Setter private int squareSize = 10;

    @BeanField @Getter @Setter private int minNumVoxels = 1;
    // END BEAN PROPERTIES

    @Override
    public ObjectCollection createFromObjects(ObjectCollection objectCollection)
            throws CreateException {
        return objectCollection.stream().flatMap(this::splitObject);
    }

    // We want to add in any remaining space at the end into the last object, so we never have a
    // rectangle
    //  smaller than our squareSDize
    private ObjectCollection splitObject(ObjectMask object) {

        ObjectCollection out = new ObjectCollection();

        Extent e = object.getBoundingBox().extent();

        int numX = numSquaresAlongDimension(e.getX());
        int numY = numSquaresAlongDimension(e.getY());

        for (int y = 0; y < numY; y++) {

            int startY = y * squareSize;

            int endY = Math.min(startY + squareSize, e.getY());

            // Special treatment for last square
            if (y == (numY - 1)) {
                endY = Math.min(endY + squareSize, e.getY());
            }

            int extentY = endY - startY;

            for (int x = 0; x < numX; x++) {

                int startX = x * squareSize;
                int endX = Math.min(startX + squareSize, e.getX());

                // Special treatment for last square
                if (x == (numX - 1)) {
                    endX = Math.min(endX + squareSize, e.getX());
                }

                createSquare(object, startX, startY, endX - startX, extentY).ifPresent(out::add);
            }
        }

        return out;
    }

    private Optional<ObjectMask> createSquare(
            ObjectMask objToSplit, int startX, int startY, int extentX, int extentY) {

        Extent extentNew = new Extent(extentX, extentY, objToSplit.getVoxelBox().extent().getZ());
        BoundingBox srcBox = new BoundingBox(new Point3i(startX, startY, 0), extentNew);

        // A voxel-box for the new square
        VoxelBox<ByteBuffer> vbNew = VoxelBoxFactory.getByte().create(extentNew);

        // Copy in mask-values from the source
        objToSplit.getVoxelBox().copyPixelsTo(srcBox, vbNew, new BoundingBox(extentNew));

        // We only add the square if there's at least one voxel in it
        if (!acceptSquare(vbNew, objToSplit.getBinaryValues().getOnInt())) {
            return Optional.empty();
        }

        return Optional.of(
                new ObjectMask(
                        srcBox.shiftBy(objToSplit.getBoundingBox().cornerMin()),
                        vbNew,
                        objToSplit.getBinaryValuesByte()));
    }

    private boolean acceptSquare(VoxelBox<ByteBuffer> vbNew, int maskOnValue) {
        // We only add the square if there's at least one voxel in it
        if (minNumVoxels == 1) {
            return vbNew.hasEqualTo(maskOnValue);
        } else {
            int cntOn = vbNew.countEqual(maskOnValue);
            return (cntOn >= minNumVoxels);
        }
    }

    private int numSquaresAlongDimension(int extent) {
        int num = extent / squareSize;
        if (num != 0) {
            return num;
        } else {
            // We force at least one, to catch the remainder
            return 1;
        }
    }
}
