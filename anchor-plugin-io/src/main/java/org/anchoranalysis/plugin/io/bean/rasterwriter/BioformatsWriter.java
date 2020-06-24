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

import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.io.RasterIOException;
import org.anchoranalysis.image.stack.Stack;
import loci.formats.FormatException;
import loci.formats.IFormatWriter;
import loci.formats.out.TiffWriter;


// Writes a stack to the file system in some manner
public class BioformatsWriter extends ByteNoTimeSeriesWriter {

	// A default extension
	@Override
	public String dfltExt() {
		return "tif";
	}

	@Override
	protected IFormatWriter createWriter() throws RasterIOException {
		try {
			TiffWriter writer = new TiffWriter();
			// COMPRESSION CURRENTLY DISABLED
			writer.setCompression("LZW");
			writer.setBigTiff(false);
			writer.setValidBitsPerPixel(8);
			writer.setWriteSequentially(true);	
			return writer;
		} catch (FormatException e) {
			throw new RasterIOException(e);
		}			
	}
	
	@Override
	protected void writeRGB( IFormatWriter writer, Stack stack ) throws FormatException, IOException, RasterIOException {
			
		Channel chnlRed = stack.getChnl(0);
		Channel chnlGreen = stack.getChnl(1);
		Channel chnlBlue = stack.getChnl(2);
		
		int cap = chnlRed.getVoxelBox().any().extent().getVolumeXY();
		int cap3 = cap * 3;
		
		for (int z=0; z<stack.getDimensions().getZ(); z++) {
	
			ByteBuffer red = chnlRed.getVoxelBox().asByte().getPixelsForPlane(z).buffer();
			ByteBuffer green = chnlGreen.getVoxelBox().asByte().getPixelsForPlane(z).buffer();
			ByteBuffer blue = chnlBlue.getVoxelBox().asByte().getPixelsForPlane(z).buffer();
			
			ByteBuffer merged = ByteBuffer.allocate( cap3 );
			merged.put( red );	
			merged.put( green );
			merged.put( blue );					
			
			//writer.saveBytes(z, red.array() );
			writer.saveBytes(z, merged.array() );
			
		}
	}
}
