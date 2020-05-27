package ch.ethz.biol.cell.imageprocessing.chnl.provider;

import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.chnl.Chnl;
import org.anchoranalysis.image.extent.Extent;
import org.anchoranalysis.image.voxel.box.VoxelBox;
import org.anchoranalysis.image.voxel.buffer.VoxelBuffer;

public abstract class ChnlProviderConditionallyWriteScalar extends ChnlProviderOneValue {
	
	@Override
	public Chnl createFromChnlValue(Chnl chnl, double value) throws CreateException {
		processVoxelBox(
			chnl.getVoxelBox().any(),
			value
		);
		return chnl;
	}
	
	/** Whether to overwrite the current voxel-value with the constant? */
	protected abstract boolean shouldOverwriteVoxelWithConstant( int voxel, int constant );
	
	private void processVoxelBox( VoxelBox<?> vb, double value ) {

		int constant = (int) Math.floor(value);
		
		Extent e = vb.extent();
		int volumeXY = e.getVolumeXY();
		for (int z=0; z<e.getZ(); z++) {
			
			VoxelBuffer<?> buf = vb.getPixelsForPlane(z);
			
			for( int i=0; i<volumeXY; i++) {
				
				int voxel = buf.getInt(i); 
				
				if (shouldOverwriteVoxelWithConstant(voxel, constant)) {
					buf.putInt(i,constant);
				}
			}
		}
	}
}
