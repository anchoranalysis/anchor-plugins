package org.anchoranalysis.plugin.mpp.bean.proposer.points;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.geometry.Point3d;
import org.anchoranalysis.core.geometry.Point3i;
import org.anchoranalysis.image.binary.BinaryChnl;
import org.anchoranalysis.image.binary.values.BinaryValuesByte;
import org.anchoranalysis.image.binary.voxel.BinaryVoxelBox;
import org.anchoranalysis.image.extent.BoundingBox;
import org.anchoranalysis.image.extent.Extent;
import org.anchoranalysis.image.voxel.box.VoxelBox;

class PointsFromInsideHelper {

	// TODO horribly long, refactor
	public static List<Point3i> convexOnly(
		BinaryChnl chnl,
		BinaryChnl chnlFilled,
		BoundingBox bbox,
		Point3d pntRoot,
		PointListForConvex pntsConvexRoot,
		int skipAfterSuccessiveEmptySlices
	) throws CreateException {

		assert( chnl.getDimensions().contains(bbox) );
	
		int startZ = (int) Math.floor(pntRoot.getZ());
		
		
		BinaryVoxelBox<ByteBuffer> binaryVoxelBox = chnlFilled.binaryVoxelBox();
		
		List<Point3i> listOut = new ArrayList<>();
		
		BinaryValuesByte bvb = chnl.getBinaryValues().createByte();
		
		VoxelBox<ByteBuffer> vb = chnl.getChnl().getVoxelBox().asByte();
		
		
		// Stays as -1 until we reach a non-empty slice
		int successiveEmptySlices = -1;
			
		Extent e = vb.extent();
		Point3i crnrMin = bbox.getCrnrMin();
		Point3i crnrMax = bbox.calcCrnrMax();
		
		
		for( int z=startZ; z<=crnrMax.getZ(); z++) {
			
			ByteBuffer bb = vb.getPixelsForPlane(z).buffer();
			
			boolean addedToSlice = false;
			for( int y=crnrMin.getY(); y<=crnrMax.getY(); y++) {
				for( int x=crnrMin.getX(); x<=crnrMax.getX(); x++) {
					
					int offset = e.offset(x, y);
					if (bb.get(offset)==bvb.getOnByte()) {
						
						Point3i pnt = new Point3i(x,y,z); 
						
						//System.out.printf("For %s: ",pnt);
						if( pntsConvexRoot.convexWithAtLeastOnePoint(pnt, binaryVoxelBox)) {
							//System.out.printf(" passed\n");
							addedToSlice = true;
							listOut.add( pnt );
						} else {
							//System.out.printf(" failed\n");
						}

					}
					
				}
			}
			
			if (!addedToSlice) {
				successiveEmptySlices = 0;
				
			// We don't increase the counter until we've been inside a non-empty slice
			} else if (successiveEmptySlices!=-1){	
				successiveEmptySlices++;
				if (successiveEmptySlices >= skipAfterSuccessiveEmptySlices) {
					break;
				}
				
			}
		}
		
		// Exit early if we start on the first slice
		if (startZ==0) {
			return listOut;
		}
		
		for( int z=(startZ-1); z>=crnrMin.getZ(); z--) {
			
			ByteBuffer bb = vb.getPixelsForPlane(z).buffer();
			
			boolean addedToSlice = false;
			for( int y=crnrMin.getY(); y<=crnrMax.getY(); y++) {
				for( int x=crnrMin.getX(); x<=crnrMax.getX(); x++) {
					
					int offset = e.offset(x, y);
					if (bb.get(offset)==bvb.getOnByte()) {
						
						Point3i pnt = new Point3i(x,y,z);
						
						//System.out.printf("For %s: ",pnt);
						if( pntsConvexRoot.convexWithAtLeastOnePoint(pnt, binaryVoxelBox)) {
							addedToSlice = true;
							listOut.add( pnt );
						}
					}
					
				}
			}
			
			if (!addedToSlice) {
				successiveEmptySlices = 0;
				
			// We don't increase the counter until we've been inside a non-empty slice
			} else if (successiveEmptySlices!=-1){	
				successiveEmptySlices++;
				if (successiveEmptySlices >= skipAfterSuccessiveEmptySlices) {
					break;
				}
				
			}
		}
		
		
		return listOut;
	}
}
