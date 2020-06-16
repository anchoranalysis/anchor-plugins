package ch.ethz.biol.cell.sgmn.objmask;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.anchoranalysis.image.convert.ByteConverter;
import org.anchoranalysis.image.extent.BoundingBox;
import org.anchoranalysis.image.objectmask.ObjectCollection;
import org.anchoranalysis.image.points.PointRange;
import org.anchoranalysis.image.voxel.box.VoxelBox;

class CreateFromLabels {

	private CreateFromLabels() {}
	
	/**
	 * Creates an object-mask collection from a voxel-box that is labelled with unique integers (sequentially increasing)
	 * 
	 * @param voxels voxels each labelled with an integer (sequentially increasing from 1) to represent an object
	 * @param minLabel minimum label-value inclusive
	 * @param maxLabel maximum label-value inclusive
	 * @param minBBoxVolume
	 * @return
	 */
	public static ObjectCollection create(
		VoxelBox<ByteBuffer> voxels,
		int minLabel,
		int maxLabel,
		int minBBoxVolume
	) {
		int numPixel[] = new int[maxLabel-minLabel+1];

		return createFromLabels(
			calcBoundingBoxes( voxels, minLabel, maxLabel, numPixel ),
			voxels,
			minBBoxVolume
		);
	}
	
	private static List<BoundingBox> calcBoundingBoxes( VoxelBox<ByteBuffer> bufferAccess, int minC, int maxC, int[] numPixel ) {
		
		List<PointRange> list = new ArrayList<>(maxC-minC+1);
		
		for (int i=minC; i<=maxC; i++) {
			list.add( new PointRange() );
		}
		
		for (int z=0; z<bufferAccess.getPlaneAccess().extent().getZ(); z++) {
			
			ByteBuffer pixel = bufferAccess.getPlaneAccess().getPixelsForPlane(z).buffer();
			
			for (int y=0; y<bufferAccess.getPlaneAccess().extent().getY(); y++) {
				for (int x=0; x<bufferAccess.getPlaneAccess().extent().getX(); x++) {
					
					int col = ByteConverter.unsignedByteToInt(pixel.get());
					
					if (col==0) {
						continue;
					}
					
					list.get(col-1).add( x,y,z );
					numPixel[col-1]++;
				}
			}
		}

		/** Convert to bounding-boxes after filtering any empty point-ranges */
		return list
			.stream()
			.filter( p -> !p.isEmpty() )
			.map(PointRange::deriveBoundingBoxNoCheck)
			.collect(Collectors.toList());
	}
	
	private static ObjectCollection createFromLabels(
		List<BoundingBox> bboxList,
		VoxelBox<ByteBuffer> bufferAccess,
		int smallVolumeThreshold
	) {
		
		ObjectCollection list = new ObjectCollection();
		
		int col = 0;
		for (BoundingBox bbox : bboxList) {
			col++;
			
			if ( bbox.extent().getVolumeXY() < smallVolumeThreshold ) {
				continue;
			}
			
			list.add(
				bufferAccess.equalMask(bbox, col)
			);
		}
				
		return list;
	}
}
