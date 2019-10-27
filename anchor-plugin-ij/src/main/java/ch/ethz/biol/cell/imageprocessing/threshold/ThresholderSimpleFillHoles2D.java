package ch.ethz.biol.cell.imageprocessing.threshold;

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


import java.nio.ByteBuffer;

import ij.Prefs;
import ij.plugin.filter.Binary;
import ij.process.ImageProcessor;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.image.bean.threshold.Thresholder;
import org.anchoranalysis.image.binary.values.BinaryValuesByte;
import org.anchoranalysis.image.binary.voxel.BinaryVoxelBox;
import org.anchoranalysis.image.convert.IJWrap;
import org.anchoranalysis.image.histogram.Histogram;
import org.anchoranalysis.image.objmask.ObjMask;
import org.anchoranalysis.image.voxel.box.VoxelBoxWrapper;
import org.anchoranalysis.image.voxel.box.thresholder.VoxelBoxThresholder;

public class ThresholderSimpleFillHoles2D extends Thresholder {

	/* Start bean properties */

	/**
	 * 
	 */
	private static final long serialVersionUID = -3814067279777552936L;
	
	// START BEAN PROPERTIES

	// Intensity for thresholding
	@BeanField
	private int minIntensity = -1;
	// END BEAN PROPERTIES
	
	



	public ThresholderSimpleFillHoles2D() {
		
	}
	
	
	
	public ThresholderSimpleFillHoles2D(int minIntensity) {
		super();
		this.minIntensity = minIntensity;
	}

	// Consumes input channel?
	@Override
	public BinaryVoxelBox<ByteBuffer> threshold(VoxelBoxWrapper inputBuffer, BinaryValuesByte bvOut,
			Histogram histogram) throws OperationFailedException {

		Prefs.blackBackground = true;
		
		BinaryVoxelBox<ByteBuffer> thresholded;
		try {
			thresholded = VoxelBoxThresholder.thresholdForLevel(inputBuffer, minIntensity, bvOut, false );
		} catch (CreateException e) {
			throw new OperationFailedException(e);
		}
		
		Binary binaryPlugin = new Binary();
		binaryPlugin.setup("fill", null);
		binaryPlugin.setNPasses( 1 );
		
		for( int z=0; z<thresholded.extnt().getZ(); z++) {
			ImageProcessor ip = IJWrap.imageProcessor( new VoxelBoxWrapper(thresholded.getVoxelBox()), z );
			binaryPlugin.run( ip );
		}
		
		return thresholded;
	}



	@Override
	public BinaryVoxelBox<ByteBuffer> threshold(VoxelBoxWrapper inputBuffer, ObjMask objMask, BinaryValuesByte bvOut,
			Histogram histogram) throws OperationFailedException {
		throw new IllegalAccessError("Method not supported");
	}



	@Override
	public int getLastThreshold() {
		return minIntensity;
	}


	public void setMinIntensity(int minIntensity) {
		this.minIntensity = minIntensity;
	}


	
	public int getMinIntensity() {
		return minIntensity;
	}





}
