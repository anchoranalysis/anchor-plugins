package org.anchoranalysis.plugin.image.feature.bean.obj.single.intensity;

import java.nio.ByteBuffer;

import org.anchoranalysis.core.geometry.Point3i;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.chnl.Chnl;
import org.anchoranalysis.image.extent.BoundingBox;
import org.anchoranalysis.image.objmask.ObjMask;
import org.anchoranalysis.image.voxel.box.VoxelBoxWrapper;
import org.anchoranalysis.image.voxel.buffer.VoxelBuffer;

public class IntensityMeanCalculator {

	private IntensityMeanCalculator() {}
	
	public static double calcMeanIntensityObjMask( Chnl chnl, ObjMask om ) throws FeatureCalcException {
		return calcMeanIntensityObjMask(chnl, om, false);
	}
	
	public static double calcMeanIntensityObjMask( Chnl chnl, ObjMask om, boolean excludeZero ) throws FeatureCalcException {
		
		if (!chnl.getDimensions().getExtnt().contains(om.getBoundingBox())) {
			throw new FeatureCalcException(
				String.format(
					"The object's bounding-box (%s) is not contained within the dimensions of the channel %s",
					om.getBoundingBox(),
					chnl.getDimensions().getExtnt()
				)
			);
		}
		
		VoxelBoxWrapper vbIntens = chnl.getVoxelBox();
		
		BoundingBox bbox = om.getBoundingBox();
		
		Point3i crnrMin = bbox.getCrnrMin();
		Point3i crnrMax = bbox.calcCrnrMax();
		
		double sum = 0.0;
		int cnt = 0;
		
		for( int z=crnrMin.getZ(); z<=crnrMax.getZ(); z++) {
			
			VoxelBuffer<?> bbIntens = vbIntens.any().getPixelsForPlane( z );
			ByteBuffer bbMask = om.getVoxelBox().getPixelsForPlane( z - crnrMin.getZ() ).buffer();
			
			int offsetMask = 0;
			for( int y=crnrMin.getY(); y<=crnrMax.getY(); y++) {
				for( int x=crnrMin.getX(); x<=crnrMax.getX(); x++) {
				
					if (bbMask.get(offsetMask)==om.getBinaryValuesByte().getOnByte()) {
						int offsetIntens = vbIntens.any().extnt().offset(x, y);
						
						int val = bbIntens.getInt(offsetIntens);
						
						if(excludeZero && val==0) {
							offsetMask++;
							continue;
						}
						
						sum += val;
						cnt++;
					}
							
					offsetMask++;
				}
			}
		}
		
		if (cnt==0) {
			throw new FeatureCalcException("There are 0 pixels in the object-mask");
		}
		
		return sum/cnt;
	}
}
