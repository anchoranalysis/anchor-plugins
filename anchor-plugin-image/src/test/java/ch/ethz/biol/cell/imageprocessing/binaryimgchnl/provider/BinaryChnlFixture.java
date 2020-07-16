/* (C)2020 */
package ch.ethz.biol.cell.imageprocessing.binaryimgchnl.provider;

import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.geometry.Point3i;
import org.anchoranalysis.image.binary.mask.Mask;
import org.anchoranalysis.image.binary.voxel.BinaryVoxelBoxFactory;
import org.anchoranalysis.image.extent.BoundingBox;
import org.anchoranalysis.image.extent.Extent;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.test.image.ChnlFixture;

/**
 * Creates {@link Mask} instantiations for tests.
 *
 * @author Owen Feehan
 */
class BinaryChnlFixture {

    public static final int WIDTH = 40;
    public static final int HEIGHT = 7;
    public static final int DEPTH = 3;

    private BinaryChnlFixture() {}

    public static Mask createWithRectangle(Point3i crnr, boolean do3D) throws CreateException {

        Mask chnl = new Mask(BinaryVoxelBoxFactory.instance().create(extent(do3D)));

        chnl.binaryVoxelBox().setPixelsCheckMaskOn(createRectange(crnr, do3D));

        return chnl;
    }

    public static Extent extent(boolean do3D) {
        return do3D ? ChnlFixture.MEDIUM_3D : ChnlFixture.MEDIUM_2D;
    }

    public static int depth(boolean do3D) {
        return do3D ? DEPTH : 1;
    }

    /** Creates a rectangle (2d) or cuboid (3d) */
    private static ObjectMask createRectange(Point3i crnr, boolean do3D) {
        ObjectMask object =
                new ObjectMask(new BoundingBox(crnr, new Extent(WIDTH, HEIGHT, depth(do3D))));
        object.binaryVoxelBox().setAllPixelsToOn();
        return object;
    }
}
