package org.anchoranalysis.plugin.io.multifile.buffer;

/*-
 * #%L
 * anchor-plugin-io
 * %%
 * Copyright (C) 2010 - 2020 Owen Feehan
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

import java.util.Optional;

import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.channel.factory.ChannelFactory;
import org.anchoranalysis.image.extent.ImageDim;
import org.anchoranalysis.image.extent.IncorrectImageSizeException;
import org.anchoranalysis.image.stack.Stack;
import org.anchoranalysis.image.voxel.box.VoxelBox;
import org.anchoranalysis.image.voxel.box.VoxelBoxWrapper;
import org.anchoranalysis.image.voxel.buffer.VoxelBuffer;
import org.anchoranalysis.image.voxel.datatype.VoxelDataType;
import org.anchoranalysis.plugin.io.multifile.SizeExtnts;

class MultiBuffer {
	
	
	
	// Time, Channel, Slices
	@SuppressWarnings("rawtypes")
	private VoxelBuffer[][][] buffers;
	
	private SizeExtnts size;
	
	public MultiBuffer( Stack stackArbitrarySlice, SizeExtnts size ) {
		this.size = size;
		
		size.populateMissingFromArbitrarySlice(stackArbitrarySlice);
		buffers = new VoxelBuffer<?>[size.getRangeT().getSize()][size.getRangeC().getSize()][size.getRangeZ().getSize()];
	}
			
	public void populateWithSpecifiedChnl(Stack stackForFile, int chnlNum, Optional<Integer> sliceNum, int timeIndex) {
		// Specific Channel Number, but no specific Slice Number
		Channel chnl = stackForFile.getChnl(0);
		VoxelBox<?> vb = chnl.getVoxelBox().any();
		
		int chnlIndexRslvd = size.getRangeC().index(chnlNum);
		int timeIndexRslvd = size.getRangeT().index(timeIndex);
		
		if( sliceNum.isPresent()) {
			copyFirstSliceForChnl(timeIndexRslvd, chnlIndexRslvd, vb, sliceNum.get());
			
		} else {
			copyAllSlicesForChnl(timeIndexRslvd, chnlIndexRslvd, vb);
		}
	}
	
	public void populateWithSpecifiedSlice(Stack stackForFile, int sliceNum, int timeIndex) {
		
		int timeIndexRslvd = size.getRangeT().index(timeIndex);
		
		for( int c=0; c<stackForFile.getNumChnl(); c++ ) {
			Channel chnl = stackForFile.getChnl(c);
			copyFirstSliceForChnl(timeIndexRslvd, c, chnl.getVoxelBox().any(), sliceNum);
		}
	}
			
	public void populateNoSpecifics(Stack stackForFile, int timeIndex) {
		
		int timeIndexRslvd = size.getRangeT().index(timeIndex);
		
		// No specific Channel Number, and no specific Slice Number
		// Then we have to guess the channel
		for( int c=0; c<stackForFile.getNumChnl(); c++ ) {
			Channel chnl = stackForFile.getChnl(c);
			copyAllSlicesForChnl(timeIndexRslvd, c, chnl.getVoxelBox().any() );
		}	
	}

	public Stack createStackForIndex( int t, ImageDim dim, VoxelDataType dataType) {
		
		Stack stack = new Stack();
		
		for( int c=0; c<size.getRangeC().getSize(); c++) {
			
			Channel chnl = ChannelFactory.instance().createEmptyUninitialised(dim,dataType);
			copyAllBuffersTo(t, c, chnl.getVoxelBox());
			
			try {
				stack.addChnl(chnl);
			} catch (IncorrectImageSizeException e) {
				assert false;
			}
		}
		return stack;
	}
	
	@SuppressWarnings("unchecked")
	private void copyAllBuffersTo(int t, int c, VoxelBoxWrapper vb) {
		for( int z=0; z<size.getRangeZ().getSize(); z++) {
			vb.any().setPixelsForPlane(z, buffers[t][c][z]);
		}
	}
	
	private void copyFirstSliceForChnl( int t, int c, VoxelBox<?> vb, int sliceNum ) {
		buffers[t][c][size.getRangeZ().index(sliceNum)] = vb.getPixelsForPlane(0);
	}
	
	private void copyAllSlicesForChnl( int t, int c, VoxelBox<?> vb ) {
		for( int z=0; z<vb.extent().getZ(); z++) {
			buffers[t][c][z] = vb.getPixelsForPlane(z);
		}
	}
}
