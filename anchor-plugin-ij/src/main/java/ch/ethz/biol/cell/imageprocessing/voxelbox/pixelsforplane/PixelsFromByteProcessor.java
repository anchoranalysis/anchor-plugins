package ch.ethz.biol.cell.imageprocessing.voxelbox.pixelsforplane;

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


import ij.process.ByteProcessor;

import java.nio.ByteBuffer;

import org.anchoranalysis.image.extent.Extent;
import org.anchoranalysis.image.voxel.box.pixelsforplane.IPixelsForPlane;
import org.anchoranalysis.image.voxel.buffer.VoxelBuffer;
import org.anchoranalysis.image.voxel.buffer.VoxelBufferByte;

public class PixelsFromByteProcessor implements IPixelsForPlane<ByteBuffer> {
	
	private ByteProcessor bp;
	private Extent extnt;
	
	public PixelsFromByteProcessor( ByteProcessor bp ) {
		super();
		this.bp = bp;
		this.extnt = new Extent( bp.getWidth(), bp.getHeight(), 1 );
	}

	@Override
	public VoxelBuffer<ByteBuffer> getPixelsForPlane( int z ) {
		return VoxelBufferByte.wrap( (byte[]) bp.getPixels() );
	}
	
	@Override
	public void setPixelsForPlane(int z, VoxelBuffer<ByteBuffer> pixels) {
		assert(z==0);
		bp.setPixels(pixels.buffer().array());
	}

	@Override
	public Extent extent() {
		return extnt;
	}
}