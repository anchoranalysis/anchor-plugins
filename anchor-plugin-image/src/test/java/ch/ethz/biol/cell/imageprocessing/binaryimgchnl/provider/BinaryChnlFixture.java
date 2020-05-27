package ch.ethz.biol.cell.imageprocessing.binaryimgchnl.provider;

import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.geometry.Point3i;
import org.anchoranalysis.image.binary.BinaryChnl;
import org.anchoranalysis.image.binary.voxel.BinaryVoxelBoxFactory;
import org.anchoranalysis.image.extent.BoundingBox;
import org.anchoranalysis.image.extent.Extent;
import org.anchoranalysis.image.objmask.ObjMask;
import org.anchoranalysis.test.image.ChnlFixture;

/**
 * Creates {@link BinaryChnl} instantiations for tests.
 * 
 * @author Owen Feehan
 *
 */
class BinaryChnlFixture {

	public static final int WIDTH = 40;
	public static final int HEIGHT = 7;
	public static final int DEPTH = 3;
	
	private BinaryChnlFixture() {}
	
	public static BinaryChnl createWithRectangle( Point3i crnr, boolean use3D ) throws CreateException {

		BinaryChnl chnl = new BinaryChnl(
			BinaryVoxelBoxFactory.instance().create(
				extent(use3D)
			)
		);
		
		chnl.binaryVoxelBox().setPixelsCheckMaskOn(
			createRectange(crnr, use3D)
		);
		
		return chnl;
	}

	public static Extent extent(boolean use3D) {
		return use3D ? ChnlFixture.MEDIUM_3D : ChnlFixture.MEDIUM_2D;
	}
	
	public static int depth(boolean use3D) {
		return use3D ? DEPTH : 1;
	}
	
	/** Creates a rectangle (2d) or cuboid (3d) */
	private static ObjMask createRectange( Point3i crnr, boolean use3D ) {
		ObjMask om = new ObjMask(
			new BoundingBox(
				crnr,
				new Extent(
					WIDTH,
					HEIGHT,
					depth(use3D)
				)
			)
		);
		om.binaryVoxelBox().setAllPixelsToOn();
		return om;
	}
}