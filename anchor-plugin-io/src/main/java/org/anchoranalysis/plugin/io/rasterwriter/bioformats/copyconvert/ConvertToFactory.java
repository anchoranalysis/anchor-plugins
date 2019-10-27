package org.anchoranalysis.plugin.io.rasterwriter.bioformats.copyconvert;

/*-
 * #%L
 * anchor-plugin-io
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

import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.voxel.datatype.VoxelDataType;
import org.anchoranalysis.image.voxel.datatype.VoxelDataTypeByte;
import org.anchoranalysis.image.voxel.datatype.VoxelDataTypeFloat;
import org.anchoranalysis.image.voxel.datatype.VoxelDataTypeInt;
import org.anchoranalysis.image.voxel.datatype.VoxelDataTypeShort;
import org.anchoranalysis.plugin.io.rasterwriter.bioformats.copyconvert.tobyte.ConvertToByte;
import org.anchoranalysis.plugin.io.rasterwriter.bioformats.copyconvert.tobyte.ConvertToByte_From16Bit;
import org.anchoranalysis.plugin.io.rasterwriter.bioformats.copyconvert.tobyte.ConvertToByte_From32BitFloat;
import org.anchoranalysis.plugin.io.rasterwriter.bioformats.copyconvert.tobyte.ConvertToByte_From32BitUnsignedInt;
import org.anchoranalysis.plugin.io.rasterwriter.bioformats.copyconvert.tobyte.ConvertToByte_From8Bit_Interleaving;
import org.anchoranalysis.plugin.io.rasterwriter.bioformats.copyconvert.tobyte.ConvertToByte_From8Bit_NoInterleaving;
import org.anchoranalysis.plugin.io.rasterwriter.bioformats.copyconvert.tofloat.ConvertToFloat;
import org.anchoranalysis.plugin.io.rasterwriter.bioformats.copyconvert.tofloat.ConvertToFloat_From32BitFloat;
import org.anchoranalysis.plugin.io.rasterwriter.bioformats.copyconvert.tofloat.ConvertToFloat_From8Bit;
import org.anchoranalysis.plugin.io.rasterwriter.bioformats.copyconvert.toshort.ConvertToShort;
import org.anchoranalysis.plugin.io.rasterwriter.bioformats.copyconvert.toshort.ConvertToShort_FromShort;

import loci.formats.FormatTools;
import loci.formats.IFormatReader;

public class ConvertToFactory {

	public static ConvertTo<?> create( IFormatReader reader, VoxelDataType targetDataType, int effectiveBitsPerPixel ) throws CreateException {

		boolean interleaved = reader.isInterleaved();
		int bitsPerPixel = maybeCorrectBitsPerPixel( reader.getBitsPerPixel() );
		
		if (interleaved) {
			return createFromInterleaved(
				targetDataType,
				bitsPerPixel
			);
		} else {
			return createFromNonInterleaved(
				reader,
				targetDataType,
				bitsPerPixel,
				effectiveBitsPerPixel
			);
		}

	}
	
	private static ConvertTo<?> createFromInterleaved(
		VoxelDataType targetDataType,
		int bitsPerPixel	
	) throws CreateException {
		if (targetDataType.equals(VoxelDataTypeByte.instance) && bitsPerPixel==8) {
			return new ConvertToByte_From8Bit_Interleaving();
		} else {
			throw new CreateException("For interleaved formats only 8-bits are supported");
		}				
	}
	
	private static ConvertTo<?> createFromNonInterleaved(
		IFormatReader reader,
		VoxelDataType targetDataType,
		int bitsPerPixel,
		int effectiveBitsPerPixel
	) throws CreateException {
		
		boolean littleEndian = reader.isLittleEndian();
		boolean floatingPoint = FormatTools.isFloatingPoint( reader.getPixelType() );
		
		if (targetDataType.equals(VoxelDataTypeByte.instance)) {
			return toByte( bitsPerPixel, effectiveBitsPerPixel, littleEndian, floatingPoint );
		} else if (targetDataType.equals(VoxelDataTypeShort.instance)) {
			return toShort( bitsPerPixel, littleEndian );
		} else if (targetDataType.equals(VoxelDataTypeFloat.instance)) {
			return toFloat( bitsPerPixel, littleEndian );
		} else if (targetDataType.equals(VoxelDataTypeInt.instance)) {
			// NO INT supported currently
			assert(false);
			return null;
		} else {
			assert(false);
			return null;
		}	
	}
		
	private static ConvertToByte toByte( int bitsPerPixel, int effectiveBitsPerPixel, boolean littleEndian, boolean floatingPoint ) throws CreateException {
		
		if (bitsPerPixel==8) {
			assert(effectiveBitsPerPixel==8);
			return new ConvertToByte_From8Bit_NoInterleaving();
			
		} else if (bitsPerPixel==16) {
			return new ConvertToByte_From16Bit(littleEndian, effectiveBitsPerPixel);
			
		} else if (bitsPerPixel==32) {
			
			if (floatingPoint) {
				return new ConvertToByte_From32BitFloat(littleEndian);
			} else {
				return new ConvertToByte_From32BitUnsignedInt(effectiveBitsPerPixel, littleEndian);
			}
			
		} else {
			return throwBitsPerPixelException("byte", "either 8 bits or 12 bits or 16 bits or 32 bits", bitsPerPixel);
		}
	}
	
	private static ConvertToFloat toFloat(int bitsPerPixel, boolean littleEndian) throws CreateException {
		assert( bitsPerPixel == 8 || bitsPerPixel == 32 );
		
	    if (bitsPerPixel==8) {
	    	return new ConvertToFloat_From8Bit();
	    } else if (bitsPerPixel==32) {
	    	return new ConvertToFloat_From32BitFloat(littleEndian);
	    } else {
	    	return throwBitsPerPixelException("float", "either 8 bits or 32 bits", bitsPerPixel);
	    }
	}
	
	private static <T> T throwBitsPerPixelException(String dataType, String correctBitsDescription, int bitsPerPixel) throws CreateException {
		throw new CreateException(
    		String.format(
    			"Input data for %s must have %s. It currently has %d bitsPerPixel.",
    			dataType,
    			correctBitsDescription,
    			bitsPerPixel
    		)
    	);
	}
	
	private static ConvertToShort toShort(int bitsPerPixel, boolean littleEndian) throws CreateException {

		if (bitsPerPixel==16) {
			return new ConvertToShort_FromShort(littleEndian);
		} else {
			throw new CreateException("Input data has only 8-bits, so it shouldn't be opened as 16-bits (short)");
		}
	}
	
	private static int maybeCorrectBitsPerPixel( int bitsPerPixel ) {
		if (bitsPerPixel==12) {
			return 16;
		} else {
			return bitsPerPixel;
		}
	}
}
