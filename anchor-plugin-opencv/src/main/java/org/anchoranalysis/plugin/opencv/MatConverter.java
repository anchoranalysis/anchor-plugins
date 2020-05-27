package org.anchoranalysis.plugin.opencv;

/*-
 * #%L
 * anchor-plugin-opencv
 * %%
 * Copyright (C) 2010 - 2019 Owen Feehan, ETH Zurich, University of Zurich, Hoffmann la Roche
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

import java.nio.ByteBuffer;

import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.chnl.Chnl;
import org.anchoranalysis.image.convert.ByteConverter;
import org.anchoranalysis.image.extent.Extent;
import org.anchoranalysis.image.objmask.ObjMask;
import org.anchoranalysis.image.stack.Stack;
import org.anchoranalysis.image.voxel.box.VoxelBox;
import org.anchoranalysis.image.voxel.datatype.VoxelDataTypeUnsignedByte;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

public class MatConverter {

	public static Mat fromObjMask( ObjMask om ) throws CreateException {
		Extent e = om.getBoundingBox().extent(); 
		if (e.getZ()>1) {
			throw new CreateException("Objects with more than 1 z-stack are not supported for OpenCV to Mat conversion (at the moment)");
		}

		return singleChnlMatFromVoxelBox( om.binaryVoxelBox().getVoxelBox() );
	}
		
	public static Mat fromStack( Stack stack ) throws CreateException {
		
		if (!(stack.getNumChnl()==1 || stack.getNumChnl()==3)) {
			throw new CreateException("Stack must have 1 or 3 channels");
		}
		
		if (stack.getDimensions().getZ()>1) {
			throw new CreateException("Stacks with more than 1 z-stack are not supported for OpenCV to Mat conversion (at the moment)");
		}
			
		if (stack.getNumChnl()==3) {
			return makeRGBStack(stack);
		} else {
			// Single channel
			return makeGrayscale( stack.getChnl(0) );
		}
	}
	
	public static Mat makeRGBStack( Stack stack ) throws CreateException {
		if (stack.getNumChnl()!=3) {
			throw new CreateException("Stack must have 3 channels for RGB conversion");
		}
		return matFromRGB( stack.getChnl(0), stack.getChnl(1), stack.getChnl(2) );
	}
	
	
	public static void matToRGB( Mat mat, Stack stack ) throws CreateException {
		if (stack.getNumChnl()!=3) {
			throw new CreateException("Stack must have 3 channels for RGB conversion");
		}
		matToRGB( mat, stack.getChnl(0), stack.getChnl(1), stack.getChnl(2) );
	}
	
	private static Mat makeGrayscale( Chnl chnl ) throws CreateException {
		if (chnl.getVoxelDataType().equals(VoxelDataTypeUnsignedByte.instance)) {
			
			// DEBUG
			//System.out.printf("NumPixels>100=%d%n", chnl.getVoxelBox().any().countGreaterThan(100) );
			
			return singleChnlMatFromVoxelBox( chnl.getVoxelBox().asByte() );
		} else {
			throw new CreateException("Only 8-bit channels are supported");	
		}
	}
		
	private static Mat singleChnlMatFromVoxelBox( VoxelBox<ByteBuffer> vb ) {
		
		assert(vb.extnt().getZ())==1;
		
		
		
		Mat mat = createEmptyMat( vb.extnt(), CvType.CV_8UC1 );
		mat.put(0, 0, vb.getPixelsForPlane(0).buffer().array() );
		
		// TODO
		//System.out.printf("NumPixels>100 (Mat) = %d%n", cntMoreThan(mat,100) );
		
		return mat;
	}
	
	@SuppressWarnings("unused")
	private static int cntMoreThan( Mat mat, int thrshld ) {
				
		int c = 0;
		
		int size = (int) mat.size().area();
		
		byte[] arr = new byte[ size ];
		mat.get(0, 0, arr);
		
		for( int i=0; i<size; i++ ) {
			if( ByteConverter.unsignedByteToInt(arr[i]) > thrshld ) {
				c++;
			}
		}

		return c;
	}
	
	private static Mat matFromRGB( Chnl chnlRed, Chnl chnlGreen, Chnl chnlBlue ) {
		
		Extent e = chnlRed.getDimensions().getExtnt(); 
		assert(e.getZ())==1;
		
		Mat mat = createEmptyMat( chnlRed.getDimensions().getExtnt(), CvType.CV_8UC3 );
		
		ByteBuffer red = bufferFromChnl(chnlRed);
		ByteBuffer green = bufferFromChnl(chnlGreen);
		ByteBuffer blue = bufferFromChnl(chnlBlue);
		
		for( int y=0; y<e.getY(); y++) {
			for( int x=0; x<e.getX(); x++) {
				
				// Note BGR format in OpenCV
				byte[] colArr = new byte[] {
					blue.get(),
					green.get(),
					red.get()
				};
				mat.put(y, x, colArr );
			}
		}
		
		assert( !red.hasRemaining() );
		assert( !green.hasRemaining() );
		assert( !blue.hasRemaining() );
		
		return mat;
	}

	
	private static void matToRGB( Mat mat, Chnl chnlRed, Chnl chnlGreen, Chnl chnlBlue ) {
		
		Extent e = chnlRed.getDimensions().getExtnt(); 
		assert(e.getZ())==1;
		
		ByteBuffer red = bufferFromChnl(chnlRed);
		ByteBuffer green = bufferFromChnl(chnlGreen);
		ByteBuffer blue = bufferFromChnl(chnlBlue);
		
		byte[] arr = new byte[3];
		
		for( int y=0; y<e.getY(); y++) {
			for( int x=0; x<e.getX(); x++) {
								
				mat.get(y, x, arr);
								
				red.put( arr[0] );
				green.put( arr[1] );
				blue.put( arr[2] );
			}
		}
		
		assert( !red.hasRemaining() );
		assert( !green.hasRemaining() );
		assert( !blue.hasRemaining() );
	}
	
	private static ByteBuffer bufferFromChnl( Chnl chnl ) {
		return chnl.getVoxelBox().asByte().getPixelsForPlane(0).buffer();
	}
	
	public static Mat createEmptyMat( Extent e, int type ) {
		return new Mat( e.getY(), e.getX(), type );
	}
}
