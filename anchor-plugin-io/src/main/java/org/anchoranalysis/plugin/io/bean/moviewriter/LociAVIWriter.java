package org.anchoranalysis.plugin.io.bean.moviewriter;

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

import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.extent.ImageDimensions;
import org.anchoranalysis.image.io.bean.moviewriter.MovieWriter;
import org.anchoranalysis.image.io.movie.MovieOutputHandle;
import org.anchoranalysis.image.stack.rgb.RGBStack;
import org.anchoranalysis.image.voxel.datatype.VoxelDataTypeUnsignedByte;

import ome.xml.model.enums.DimensionOrder;
import ome.xml.model.enums.EnumerationException;
import ome.xml.model.enums.PixelType;
import ome.xml.model.primitives.PositiveInteger;
import loci.common.services.DependencyException;
import loci.common.services.ServiceException;
import loci.common.services.ServiceFactory;
import loci.formats.FormatException;
import loci.formats.FormatTools;
import loci.formats.meta.IMetadata;
import loci.formats.out.AVIWriter;
import loci.formats.services.OMEXMLService;

public class LociAVIWriter extends MovieWriter {

	private static class OpenOutput implements MovieOutputHandle {

		private int indexIncr = 0;

		private AVIWriter writer;
		
		public void init( Path filePath, ImageDimensions dim, int numFrames, int numChnl, int framesPerSecond ) throws IOException {
		
			int pixelType = FormatTools.UINT8;
			
			writer = new AVIWriter();
			writer.setInterleaved(true);
			writer.setFramesPerSecond(framesPerSecond);
			
			try {
				writer.setMetadataRetrieve( createMetadata(dim, 3, pixelType, numFrames) );
				writer.setId( filePath.toString() );
				
			} catch (EnumerationException | ServiceException | DependencyException | FormatException e) {
				throw new IOException(e);
			}
		}
		
		@Override
		public void close() throws IOException {
			writer.close();			
		}

		@Override
		public void add(RGBStack stack) throws IOException {
			
			// We only support unsigned byte stacks for now
			if (!stack.allChnlsHaveType(VoxelDataTypeUnsignedByte.INSTANCE)) {
				throw new IOException("Only unsigned 8-bit stacks are supported");
			}
		
			ImageDimensions sd = stack.getChnl(0).getDimensions();
		
			// Now we write the frame to our avi
			Channel imgChnlRed = stack.getChnl(0).duplicate();
			Channel imgChnlGreen = stack.getChnl(1).duplicate();
			Channel imgChnlBlue = stack.getChnl(2).duplicate();
			
			ByteBuffer byteArrRed = imgChnlRed.getVoxelBox().asByte().getPlaneAccess().getPixelsForPlane(0).buffer();
			ByteBuffer byteArrGreen = imgChnlGreen.getVoxelBox().asByte().getPlaneAccess().getPixelsForPlane(0).buffer();
			ByteBuffer byteArrBlue = imgChnlBlue.getVoxelBox().asByte().getPlaneAccess().getPixelsForPlane(0).buffer();
			
			ByteBuffer byteArrCmb = ByteBuffer.allocate( byteArrRed.capacity() * 3 );
			
			while (byteArrRed.hasRemaining()) {
				assert( byteArrGreen.hasRemaining() );
				assert( byteArrBlue.hasRemaining() );
				
				byteArrCmb.put( byteArrRed.get() );
				byteArrCmb.put( byteArrGreen.get() );
				byteArrCmb.put( byteArrBlue.get() );
			}
			assert( !byteArrRed.hasRemaining() );
			
			try {
				writer.saveBytes(indexIncr++, byteArrCmb.array(), 0, 0, sd.getX(), sd.getY() );
			} catch (FormatException | IOException e) {
				throw new IOException(e);
			}
		}
		
	}

	@Override
	public MovieOutputHandle writeMovie(Path filePath, ImageDimensions dim, int numFrames, int numChnl, int framesPerSecond ) throws IOException {
		
		OpenOutput out = new OpenOutput();
		out.init( filePath, dim, numFrames, numChnl, framesPerSecond );
		return out;
	}
	
	
	private static IMetadata createMetadata( ImageDimensions sd, int num_chnl, int pixelType, int t ) throws EnumerationException, ServiceException, DependencyException {
		
		ServiceFactory factory = new ServiceFactory();
	    OMEXMLService service = factory.getInstance(OMEXMLService.class);
	    IMetadata meta = service.createOMEXMLMetadata();
	
	    meta.createRoot();
	    
	    int seriesNum = 0;
	    
	    meta.setImageID( String.format("Image:%d",seriesNum), seriesNum);
	    meta.setPixelsID( String.format("Pixels:%d",seriesNum),seriesNum);
	    meta.setPixelsBinDataBigEndian(Boolean.TRUE, seriesNum, 0);
	    meta.setPixelsDimensionOrder(DimensionOrder.XYCZT, seriesNum);
	    //meta.setPixelsDimensionOrder(DimensionOrder.XYZTC, z);
	    meta.setPixelsType( PixelType.fromString(FormatTools.getPixelTypeString(pixelType)), seriesNum);
	    meta.setPixelsSizeX(new PositiveInteger( sd.getX() ), seriesNum);
	    meta.setPixelsSizeY(new PositiveInteger( sd.getY() ), seriesNum);
	    meta.setPixelsSizeZ(new PositiveInteger( sd.getZ() ), seriesNum);
	    meta.setPixelsSizeC(new PositiveInteger( num_chnl ), seriesNum);
	    meta.setPixelsSizeT(new PositiveInteger(t), seriesNum);
	    
	  //  meta.setPixelsPhysicalSizeX( new Double(sd.getX()) , seriesNum);
//	    meta.setPixelsPhysicalSizeY( new Double(sd.getY()) , seriesNum);
	//    meta.setPixelsPhysicalSizeY( new Double(sd.getZ()) , seriesNum);

	    //for (int z=0; z<sd.getZ(); z++) {
	    int z = 0;

	    //
	    //setPixelsBinDataBigEndian(
	    
	    //meta.setPixelsPhysicalSizeX( sd.XRes * sd.X, 0);
	    //meta.setPixelsPhysicalSizeY( sd.YRes * sd.Y, 0);
	    //meta.setPixelsPhysicalSizeZ( sd.ZRes * sd.Z, 0);
	    //meta.setPixelsPhysicalSizeX( sd.XRes, 0);
	    //meta.setPixelsPhysicalSizeY( sd.YRes, 0);
	    //meta.setPixelsPhysicalSizeZ( sd.ZRes, 0);
	    // for (int i=0; i<num_chnl; i++) {
		    for (int i=0; i<num_chnl; i++) {
		    	meta.setChannelID( String.format("Channel:%d:%d",z,i),z, i);
		    	meta.setChannelSamplesPerPixel(new PositiveInteger(num_chnl), z, i);
			}
	    //}
		
		return meta;
	}


	@Override
	public String getDefaultFileExt() {
		return "avi";
	}

}
