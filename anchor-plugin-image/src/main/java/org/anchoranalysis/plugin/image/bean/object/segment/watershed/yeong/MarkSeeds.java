package org.anchoranalysis.plugin.image.bean.object.segment.watershed.yeong;

/*
 * #%L
 * anchor-plugin-image
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


import java.util.Optional;

import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.image.bean.nonbean.error.SegmentationFailedException;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.image.seed.Seed;
import org.anchoranalysis.image.seed.SeedCollection;
import org.anchoranalysis.image.voxel.iterator.IterateVoxels;
import org.anchoranalysis.image.voxel.iterator.ProcessVoxel;
import org.anchoranalysis.plugin.image.segment.watershed.encoding.EncodedVoxelBox;


class MarkSeeds {
		
	private MarkSeeds() {}

	public static void apply( SeedCollection seeds, EncodedVoxelBox matS, Optional<MinimaStore> minimaStore, Optional<ObjectMask> containingMask ) throws SegmentationFailedException {
			
		if (containingMask.isPresent() && !matS.extent().equals(containingMask.get().getBoundingBox().extent())) {
			throw new SegmentationFailedException("Extent of matS does not match containingMask");
		}
		
		for( Seed s : seeds ) {
			
			ObjectMask mask = s.createMask();
			
			throwExceptionIfNotConnected(mask);
		
			IterateVoxels.overMasks(
				mask,
				containingMask,
				createPointProcessor(matS, minimaStore)
			);
		}
	}
	
	private static ProcessVoxel createPointProcessor(EncodedVoxelBox matS, Optional<MinimaStore> minimaStore ) {
		ConnectedComponentWriter ccWriter = new ConnectedComponentWriter(matS, minimaStore);
		return ccWriter::writePoint;
	}
	
	private static void throwExceptionIfNotConnected( ObjectMask obj ) throws SegmentationFailedException {
		try {
			if (!obj.checkIfConnected()) {
				throw new SegmentationFailedException("Seed must be a single connected-component");
			}
		} catch (OperationFailedException e) {
			throw new SegmentationFailedException("Cannot determine if a seed is a connected-component", e);
		}
	}
}