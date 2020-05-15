package ch.ethz.biol.cell.imageprocessing.chnl.provider;

import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.chnl.Chnl;
import org.anchoranalysis.image.extent.Extent;
import org.anchoranalysis.image.voxel.box.VoxelBox;
import org.anchoranalysis.image.voxel.buffer.VoxelBuffer;

public abstract class ChnlProviderConditionallyWriteScalar extends ChnlProviderOneValue {
	
	@Override
	public Chnl createFromChnlValue(Chnl chnl, double value) throws CreateException {
		processVoxelBox( chnl.getVoxelBox().any(), value );
		return chnl;
	}
	
	protected abstract boolean predicateBufVal( int bufVal, int value );
	
	private void processVoxelBox( VoxelBox<?> vb, double value ) {

		int valueInt = (int) Math.floor(value);
		
		Extent e = vb.extnt();
		for (int z=0; z<e.getZ(); z++) {
			
			VoxelBuffer<?> buf = vb.getPixelsForPlane(z);
			
			for( int i=0; i<e.getVolumeXY(); i++) {
				
				int bufVal = buf.getInt(i); 
				
				if (predicateBufVal(bufVal, valueInt)) {
					buf.putInt(i,valueInt);
				}
			}
		}
	}
}
