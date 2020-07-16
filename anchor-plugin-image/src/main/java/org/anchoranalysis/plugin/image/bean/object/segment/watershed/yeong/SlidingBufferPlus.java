package org.anchoranalysis.plugin.image.bean.object.segment.watershed.yeong;

/*-
 * #%L
 * anchor-plugin-image
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

import org.anchoranalysis.core.geometry.Point3i;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.image.voxel.box.VoxelBox;
import org.anchoranalysis.image.voxel.buffer.SlidingBuffer;
import org.anchoranalysis.plugin.image.segment.watershed.encoding.EncodedIntBuffer;
import org.anchoranalysis.plugin.image.segment.watershed.encoding.EncodedVoxelBox;
import org.anchoranalysis.plugin.image.segment.watershed.encoding.SteepestCalc;

/** A sliding-buffer enhanced with other elements of internal state used to "visit" a pixel */
final class SlidingBufferPlus {
	
	private final SteepestCalc steepestCalc;
	
	/* The sliding buffer used for calculating the steepest-calc */
	private final SlidingBuffer<?> slidingBufferSteepestCalc;
		
	private final FindEqualVoxels findEqualVoxels;
	private final EncodedVoxelBox matS;
	private final Optional<MinimaStore> minimaStore;
	
	public SlidingBufferPlus(VoxelBox<?> vbImg, EncodedVoxelBox matS, Optional<ObjectMask> mask, Optional<MinimaStore> minimaStore) {

		this.matS = matS;
		this.minimaStore = minimaStore;
		
		this.slidingBufferSteepestCalc = new SlidingBuffer<>( vbImg );
		
		boolean do3D = vbImg.extent().getZ()>1;
		this.findEqualVoxels = new FindEqualVoxels( vbImg, matS, do3D, mask );
		this.steepestCalc = new SteepestCalc(slidingBufferSteepestCalc,matS.getEncoding(), do3D ,true, mask );
	}
	
	public SlidingBuffer<?> getSlidingBuffer() {	// NOSONAR
		return slidingBufferSteepestCalc;
	}
	
	public int offsetSlice(Point3i point) {
		return slidingBufferSteepestCalc.extent().offsetSlice(point);
	}
	
	public int getG(int indxBuffer) {
		return slidingBufferSteepestCalc.getCenter().getInt(indxBuffer);
	}
	
	public EncodedIntBuffer getSPlane(int z) {
		return matS.getPixelsForPlane(z);
	}
	
	public void maybeAddMinima(Point3i point) {
		if (minimaStore.isPresent()) {
			minimaStore.get().addDuplicated(point);
		}
	}
	
	public void makePlateauAt(Point3i point) {
		new MakePlateauLowerComplete(
			findEqualVoxels.createPlateau(point),
			findEqualVoxels.isDo3D()
		).makeBufferLowerCompleteForPlateau(
			matS,
			minimaStore
		);
	}

	public int calcSteepestDescent(Point3i point, int val, int indxBuffer) {
		return steepestCalc.calcSteepestDescent(point, val, indxBuffer);
	}

	public boolean isPlateau(int code) {
		return matS.isPlateau(code);
	}

	public boolean isMinima(int code) {
		return matS.isMinima(code);
	}
}
