package ch.ethz.biol.cell.imageprocessing.chnl.provider;

import java.nio.ByteBuffer;

import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.bean.provider.ChnlProviderTwo;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.channel.factory.ChannelFactory;
import org.anchoranalysis.image.voxel.box.VoxelBox;
import org.anchoranalysis.image.voxel.datatype.VoxelDataTypeUnsignedByte;

/**
 * Takes the two channels and creates a NEW third channel whose pixels are a function of the two channels
 * 
 * <p>Both the two input channels and the output channel are identically-sized.</p>
 * 
 * @author Owen Feehan
 *
 */
public abstract class ChnlProviderTwoVoxelMapping extends ChnlProviderTwo {

	@Override
	protected Channel process(Channel chnl1, Channel chnl2) throws CreateException {

		Channel chnlOut = ChannelFactory.instance().createEmptyInitialised(
			chnl1.getDimensions(),
			VoxelDataTypeUnsignedByte.instance
		);
		
		processVoxelBox( chnlOut.getVoxelBox().asByte(), chnl1.getVoxelBox().asByte(), chnl2.getVoxelBox().asByte() );
		
		return chnlOut;
	}
	
	protected abstract void processVoxelBox( VoxelBox<ByteBuffer> vbOut, VoxelBox<ByteBuffer> vbIn1, VoxelBox<ByteBuffer> vbIn2);
}
