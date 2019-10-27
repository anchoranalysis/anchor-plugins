package ch.ethz.biol.cell.imageprocessing.io.objmask;

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


import ij.process.ImageProcessor;

import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.image.convert.ByteConverter;

public class FloodFillUtils {

	// works on a single plane, returns highest color value assigned
	// posVal is a value defining 'positive' indicating what will get filled
	public static int floodFill2D( ImageProcessor ip, byte posValByte, int startingCol, int minVol ) throws OperationFailedException {
		
		int posValAsInt = ByteConverter.unsignedByteToInt(posValByte);
		
		//ImageProcessor ip = voxelBox.imageProcessor(0);
		
		int sx = ip.getWidth();
		int sy = ip.getHeight();
		
		IJFloodFiller ff = new IJFloodFiller( ip );
		
		//ByteBuffer pixels = voxelBox.getPlaneAccess().getPixelsForPlane(0); 
		
		// Color, we use colors other than our posval at a posVal
		int c = startingCol - 1;
		for (int y=0; y<sy; y++) {
			for (int x=0; x<sx; x++) {
				if ( ip.getPixel(x, y)==posValAsInt ) {
					
					if (c!=posValAsInt) {
						c++;
					}
					
					if (c==255) {
						throw new OperationFailedException("More objects that colors (max of 254 allowed)");
					}
					
					ip.setColor( c );
					//ip.setValue( ++c );
					int filledPixels = ff.fill(x, y);
					
					if (filledPixels < minVol) {
						ip.setColor( 0 );
						int replaceNum = ff.fill(x, y);
						assert( filledPixels==replaceNum );
						c--;
					}
					//System.out.printf("filledPixels=%d\n", filledPixels);
				}
			}
		}
		
		return c;
	}
	

	
}
