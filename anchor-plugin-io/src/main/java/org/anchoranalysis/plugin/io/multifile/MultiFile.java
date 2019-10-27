package org.anchoranalysis.plugin.io.multifile;

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

import java.nio.file.Path;

import org.anchoranalysis.image.io.RasterIOException;
import org.anchoranalysis.image.stack.Stack;
import org.anchoranalysis.image.stack.TimeSequence;
import org.anchoranalysis.plugin.io.multifile.buffer.MultiBufferSized;

// We try to guess these parameters from the fileBag, and if not, from after we add the first image
class MultiFile {
	
	private SizeExtnts size;
	
	private DataTypeChecker dataType = new DataTypeChecker();
	
	// What we populate before creating the final stack, buffers is sorted by chnl and then by slice
	private MultiBufferSized buffers;
	
	
	public MultiFile( ParsedFilePathBag fileBag ) {
		super();
		size = new SizeExtnts(fileBag);
	}

	public void add( Stack stackForFile, Integer chnlNum, Integer sliceNum, Integer timeIndex, Path filePath ) throws RasterIOException {
		
		dataType.check( stackForFile );
		
		if( buffers==null) {
			buffers = new MultiBufferSized( stackForFile, size );
		}
		assert( size.hasNecessaryExtnts() );
		
		checkChnlNum(stackForFile, chnlNum, filePath);
		checkSliceNum(stackForFile, sliceNum, filePath);
	
		buffers.populateFrom(stackForFile, chnlNum, sliceNum, timeIndex);
	}
	
	public TimeSequence createSequence() {
		return buffers.createSequence(dataType.getDataType());
	}
		
	private void checkSliceNum( Stack stackForFile, Integer sliceNum, Path filePath ) throws RasterIOException {
		if (sliceNum!=null) {
			if (stackForFile.getDimensions().getZ()!=1) {
				throw new RasterIOException( String.format("A sliceNum %d is specified, but the file '%s' has more than one slice",sliceNum, filePath) );
			}
		} else {
			if (stackForFile.getDimensions().getZ()!=size.getRangeZ().getSize()) {
				throw new RasterIOException( String.format("File '%s' has an incorrect number of slices",filePath) );
			}
		}
	}

		
	private void checkChnlNum( Stack stackForFile, Integer chnlNum, Path filePath ) throws RasterIOException {
		if (chnlNum!=null) {
			if( stackForFile.getNumChnl() !=1) {
				throw new RasterIOException( String.format("A chnlNum %d is specified, but the file '%s' has more than one channel",chnlNum, filePath) );
			}
		} else {
			if (stackForFile.getNumChnl() != size.getRangeC().getSize()) {
				throw new RasterIOException( String.format("File '%s' has an incorrect number of channels",filePath) );
			}				
		}
	}
	
	public int numChnl() {
		return size.getRangeC().getSize();
	}
	
	public int numFrames() {
		return size.getRangeT().getSize();
	}

	public boolean numFramesDefined() {
		return size.getRangeT().hasSizeDefined();
	}
	
	public boolean numChnlDefined() {
		return size.getRangeC().hasSizeDefined();
	}
}
