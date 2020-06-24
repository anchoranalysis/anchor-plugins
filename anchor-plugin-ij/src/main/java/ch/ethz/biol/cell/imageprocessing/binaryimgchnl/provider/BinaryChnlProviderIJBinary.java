package ch.ethz.biol.cell.imageprocessing.binaryimgchnl.provider;

/*
 * #%L
 * anchor-plugin-ij
 * %%
 * Copyright (C) 2016 ETH Zurich, University of Zurich, Owen Feehan
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */


import ij.Prefs;
import ij.plugin.filter.Binary;
import ij.process.ImageProcessor;

import java.nio.ByteBuffer;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.Positive;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.image.bean.provider.BinaryChnlProviderOne;
import org.anchoranalysis.image.binary.BinaryChnl;
import org.anchoranalysis.image.binary.voxel.BinaryVoxelBox;
import org.anchoranalysis.image.channel.factory.ChannelFactory;
import org.anchoranalysis.image.convert.IJWrap;
import org.anchoranalysis.image.voxel.datatype.VoxelDataTypeUnsignedByte;

public class BinaryChnlProviderIJBinary extends BinaryChnlProviderOne {

	// START BEAN PROPERTIES
	@BeanField
	private String command = "";		// One of: open, close, fill, erode, dilate, skel, outline
	
	@BeanField @Positive
	private int iterations = 1;			// iterations for erode, dilate, open, close
	// END BEAN PROPERTIES
	
	public static void fill( BinaryVoxelBox<ByteBuffer> bvb ) throws OperationFailedException {
		doCommand( bvb, "fill", 1 );
	}

	private static BinaryVoxelBox<ByteBuffer> doCommand( BinaryVoxelBox<ByteBuffer> bvb, String command, int iterations  ) throws OperationFailedException {
		
		if (bvb.getBinaryValues().getOnInt()!=255 || bvb.getBinaryValues().getOffInt()!=0) {
			throw new OperationFailedException("On byte must be 255, and off byte must be 0");
		}
		
		Prefs.blackBackground = true;
		
		// Fills Holes
		Binary binaryPlugin = new Binary();
		binaryPlugin.setup(command, null);
		binaryPlugin.setNPasses( bvb.extent().getZ() );

		for( int i=0; i<iterations; i++) {
		
			// Are we missing a Z slice?
			for (int z=0; z<bvb.extent().getZ(); z++) {
				
				ImageProcessor ip = IJWrap.imageProcessorByte( bvb.getVoxelBox().getPlaneAccess(), z); 
				binaryPlugin.run( ip );
			}
		}
		
		return bvb;
	}
	
	@Override
	public BinaryChnl createFromChnl( BinaryChnl binaryChnl ) throws CreateException {
		
		BinaryVoxelBox<ByteBuffer> bvb = binaryChnl.binaryVoxelBox();
		
		try {
			BinaryVoxelBox<ByteBuffer> bvbOut = doCommand( bvb, command, iterations); 
			return new BinaryChnl(
				bvbOut,
				binaryChnl.getDimensions().getRes(),
				ChannelFactory.instance().get( VoxelDataTypeUnsignedByte.instance )
			);
		} catch (OperationFailedException e) {
			throw new CreateException(e);
		}
	}
	
	public String getCommand() {
		return command;
	}

	public void setCommand(String command) {
		this.command = command;
	}

	public int getIterations() {
		return iterations;
	}

	public void setIterations(int iterations) {
		this.iterations = iterations;
	}
}
