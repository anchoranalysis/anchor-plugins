package org.anchoranalysis.plugin.mpp.experiment.bean.objs;

import java.nio.ByteBuffer;

import org.anchoranalysis.image.binary.values.BinaryValuesByte;
import org.anchoranalysis.image.extent.BoundingBox;
import org.anchoranalysis.image.objmask.ObjMask;
import org.anchoranalysis.image.voxel.box.VoxelBox;
import org.anchoranalysis.image.voxel.box.factory.VoxelBoxFactory;
import org.anchoranalysis.io.output.error.OutputWriteFailedException;

class BBoxUtilities {

	public static ObjMask createObjMaskForBBox( ObjMask om, BoundingBox maybeBiggerBBox ) throws OutputWriteFailedException {

		assert(maybeBiggerBBox!=null);
		if (!om.getBoundingBox().equals(maybeBiggerBBox)) {
			VoxelBox<ByteBuffer> vbLarge = VoxelBoxFactory.getByte().create( maybeBiggerBBox.extnt() );
			
			BoundingBox bbLocal = new BoundingBox( om.getBoundingBox() );
			bbLocal.setCrnrMin( bbLocal.relPosTo(maybeBiggerBBox) );
			
			ObjMask omRel = new ObjMask( bbLocal, om.binaryVoxelBox() );

			BinaryValuesByte bvb = BinaryValuesByte.getDefault();
			vbLarge.setPixelsCheckMask(omRel, bvb.getOnByte() );
			
			return new ObjMask( maybeBiggerBBox, vbLarge, bvb );
		} else {
			return om;
		}
	}
}
