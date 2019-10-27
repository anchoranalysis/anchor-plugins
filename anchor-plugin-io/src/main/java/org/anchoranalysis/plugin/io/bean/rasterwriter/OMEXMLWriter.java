package org.anchoranalysis.plugin.io.bean.rasterwriter;

/*
 * #%L
 * anchor-plugin-io
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




import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Path;

import org.anchoranalysis.image.chnl.Chnl;
import org.anchoranalysis.image.extent.ImageDim;
import org.anchoranalysis.image.io.RasterIOException;
import org.anchoranalysis.image.io.bean.rasterwriter.RasterWriter;
import org.anchoranalysis.image.io.generator.raster.series.ImgStackSeries;
import org.anchoranalysis.image.stack.Stack;
import org.anchoranalysis.image.voxel.box.VoxelBox;

import ome.xml.model.enums.EnumerationException;
import ome.xml.model.enums.PixelType;
import loci.common.services.DependencyException;
import loci.common.services.ServiceException;
import loci.formats.FormatException;
import loci.formats.IFormatWriter;
import loci.formats.ImageWriter;


// Writes a stack to the file system in some manner
public class OMEXMLWriter extends RasterWriter {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// A default extension
	@Override
	public String dfltExt() {
		return "ome";
	}

	@Override
	public void writeTimeSeriesStackByte(ImgStackSeries<ByteBuffer> stackSeries,
			Path filePath, boolean makeRGB)
			throws RasterIOException {
		throw new RasterIOException("Writing time-series is unsupported");
	}

	private void writeSeperateChnl( IFormatWriter writer, Stack stack ) throws FormatException, IOException, RasterIOException {
		
		int cnt = 0;
		for( int c=0; c<stack.getNumChnl(); c++) {
			Chnl chnl = stack.getChnl(c);
			VoxelBox<ByteBuffer> vb = chnl.getVoxelBox().asByte();
			
			for (int z=0; z<stack.getDimensions().getZ(); z++) {
				writer.saveBytes(cnt++, vb.getPixelsForPlane(z).buffer().array() );
				
			}
		}
	}
	
	private void writeRGB( IFormatWriter writer, Stack stack ) throws FormatException, IOException, RasterIOException {
			
		Chnl chnlRed = stack.getChnl(0);
		Chnl chnlGreen = stack.getChnl(1);
		Chnl chnlBlue = stack.getChnl(2);
		
		VoxelBox<ByteBuffer> vbRed = chnlRed.getVoxelBox().asByte();
		VoxelBox<ByteBuffer> vbGreen = chnlGreen.getVoxelBox().asByte();
		VoxelBox<ByteBuffer> vbBlue = chnlBlue.getVoxelBox().asByte();
		
		for (int z=0; z<stack.getDimensions().getZ(); z++) {
	
			ByteBuffer red = vbRed.getPixelsForPlane(z).buffer();
			ByteBuffer green = vbGreen.getPixelsForPlane(z).buffer();
			ByteBuffer blue = vbBlue.getPixelsForPlane(z).buffer();
			
			ByteBuffer merged = ByteBuffer.allocate( red.capacity() + green.capacity() + blue.capacity() );
			merged.put( red );	
			merged.put( green );
			merged.put( blue );					
			
			writer.saveBytes(z, merged.array() );
			
		}
	}
	
	
	// Key interface method
	@Override
	public void writeStackByte( Stack stack, Path filePath, boolean makeRGB ) throws RasterIOException {
		
		if (!(stack.getNumChnl()==1 || stack.getNumChnl()==3)) {
			throw new RasterIOException("Stack must have 1 or 3 channels");
		}
		
		ImageDim sd = stack.getDimensions();

		PixelType pixelType = PixelType.UINT8;

		Stack stackCast = (Stack) stack;
		
		try (ImageWriter writer = new ImageWriter()) {
			writer.setInterleaved(false);
			writer.setMetadataRetrieve( MetadataUtilities.createMetadata(sd, stack.getNumChnl(), pixelType, makeRGB, false ) );
			writer.setId( filePath.toString() );
					
			if (!writer.canDoStacks() && sd.getZ() > 1 ) {
				throw new RasterIOException("The writer must support stacks for Z > 1");
			}
			
			if (makeRGB && stack.getNumChnl()==3) {
				writeRGB(writer, stackCast);
			} else {
				writeSeperateChnl(writer, stackCast);
			}
		
		} catch (IOException | FormatException | EnumerationException | ServiceException | DependencyException e) {
			throw new RasterIOException(e);
		}
	}
	
	
	@Override
	public void writeStackShort(Stack stack, Path filePath,
			boolean makeRGB) throws RasterIOException {
		throw new RasterIOException("Writing ShortBuffer stack not yet implemented");
	}
}
