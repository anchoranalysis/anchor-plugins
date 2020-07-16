/* (C)2020 */
package org.anchoranalysisplugin.io.test.image;

import java.nio.ByteBuffer;
import java.util.Random;
import org.anchoranalysis.core.geometry.Point3i;
import org.anchoranalysis.image.extent.BoundingBox;
import org.anchoranalysis.image.extent.Extent;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.image.object.ObjectCollectionFactory;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.image.voxel.buffer.VoxelBuffer;

class ObjectCollectionFixture {

    // Maximum extent of a scene
    private static final Extent SCENE_EXTENT = new Extent(1000, 900, 40);

    /** How frequently do we change value randomly when iterating over pixel values */
    private static final double PROBABILITY_CHANGE_VALUE = 0.2;

    private static final Random RANDOM = new Random();

    /**
     * Creates an object-collection containing between a random number of objects (uniformly sampled
     * from a range)
     *
     * <p>Each mask has a position and extent that is randomly-sampled, and contains voxels that are
     * each randomly on or off.
     *
     * @param minNumberObjects minimum number of objects (inclusive)
     * @param maxNumberObjects maximum number of objects (exclusive)
     * @return
     */
    public ObjectCollection createMockObjects(int minNumberObjects, int maxNumberObjects) {
        int numberObjects = randomMinMax(minNumberObjects, maxNumberObjects);
        return ObjectCollectionFactory.fromRepeated(numberObjects, this::mockObject);
    }

    private ObjectMask mockObject() {
        Extent e = randomExtent();
        Point3i crnr = randomCorner(e);
        return mockObject(crnr, e);
    }

    private ObjectMask mockObject(Point3i crnr, Extent e) {

        ObjectMask object = new ObjectMask(new BoundingBox(crnr, e));

        int volumeXY = e.getVolumeXY();
        for (int z = 0; z < e.getZ(); z++) {

            VoxelBuffer<ByteBuffer> vb = object.getVoxelBox().getPixelsForPlane(z);
            ByteBuffer bb = vb.buffer();

            int prevVal = 0;

            for (int i = 0; i < volumeXY; i++) {
                prevVal = randomMaybeChangeVal(prevVal);
                bb.put((byte) prevVal);
            }
        }

        // Switch samples on an off with uniform randomness
        return object;
    }

    /** Randomly returns 0 or 255 with equal probability. prevVal must be 0 or 255 */
    private static int randomMaybeChangeVal(int prevVal) {
        if (RANDOM.nextDouble() > PROBABILITY_CHANGE_VALUE) {
            return prevVal;
        } else {
            return (255 - prevVal);
        }
    }

    /** Randomly returns an extent within a scene */
    private static Extent randomExtent() {
        int x = randomTotal(SCENE_EXTENT.getX() - 1) + 1;
        int y = randomTotal(SCENE_EXTENT.getY() - 1) + 1;
        int z = randomTotal(SCENE_EXTENT.getZ() - 1) + 1;
        return new Extent(x, y, z);
    }

    /** A random starting corner, making sure there's enough room for the extent */
    private static Point3i randomCorner(Extent extent) {
        int x = randomSubtract(SCENE_EXTENT.getX(), extent.getX());
        int y = randomSubtract(SCENE_EXTENT.getY(), extent.getY());
        int z = randomSubtract(SCENE_EXTENT.getZ(), extent.getZ());
        return new Point3i(x, y, z);
    }

    private static int randomTotal(int total) {
        return RANDOM.nextInt(total);
    }

    private static int randomMinMax(int min, int max) {
        return RANDOM.nextInt(max - min) + min;
    }

    private static int randomSubtract(int total, int sub) {
        return randomTotal(total - sub);
    }
}
