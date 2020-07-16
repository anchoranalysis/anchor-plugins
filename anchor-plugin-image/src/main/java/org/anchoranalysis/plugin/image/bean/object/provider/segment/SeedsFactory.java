package org.anchoranalysis.plugin.image.bean.object.provider.segment;



/*
 * #%L
 * anchor-image
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


import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.geometry.ReadableTuple3i;
import org.anchoranalysis.image.extent.BoundingBox;
import org.anchoranalysis.image.extent.ImageDimensions;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.image.seed.SeedCollection;
import org.anchoranalysis.image.seed.SeedObjectMask;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access=AccessLevel.PRIVATE)
class SeedsFactory {
	
	public static SeedCollection createSeedsWithoutMask( ObjectCollection seeds ) {
		// We create a collection of seeds localised appropriately
		// NB: we simply change the object seeds, as it seemingly won't be used again!!!
		SeedCollection out = new SeedCollection();
		for( ObjectMask object : seeds ) {
			out.add(
				createSeed(object)
			);
		}
		return out;
	}
	
	public static SeedCollection createSeedsWithMask(
		ObjectCollection seeds,
		ObjectMask containingMask,
		ReadableTuple3i subtractFromCornerMin,
		ImageDimensions dim
	) throws CreateException {
		// We create a collection of seeds localised appropriately
		// NB: we simply change the object seeds, as it seemingly won't be used again!!!
		SeedCollection out = new SeedCollection();
		for( ObjectMask object : seeds ) {
			out.add(
				createSeedWithinMask(
					object,
					containingMask.getBoundingBox(),
					subtractFromCornerMin,
					dim
				)
			);
		}
		return out;
	}
	
	private static SeedObjectMask createSeed( ObjectMask object ) {
		return new SeedObjectMask(
			object.duplicate()
		);
	}
	
	private static SeedObjectMask createSeedWithinMask(
		ObjectMask object,
		BoundingBox containingBBox,
		ReadableTuple3i subtractFromCornerMin,
		ImageDimensions dim
	) throws CreateException {
		
		ObjectMask seed = object.mapBoundingBox( bbox->
			bbox.shiftBackBy(subtractFromCornerMin)
		);
		
		// If a seed object is partially located outside an object, the above line might fail, so we should test
		return new SeedObjectMask(
			ensureInsideContainer(seed, containingBBox, dim)
		);
	}
	
	private static ObjectMask ensureInsideContainer( ObjectMask seed, BoundingBox containingBBox, ImageDimensions dim ) throws CreateException {
		if (!containingBBox.contains().box( seed.getBoundingBox())) {
			// We only take the part of the seed object that intersects with our bbox
			BoundingBox bboxIntersect = containingBBox
				.intersection()
				.withInside( seed.getBoundingBox(), dim.getExtent() )
				.orElseThrow( ()->
					new CreateException("No bounding box intersection exists between seed and containing bounding-box")
				);
			return seed.region(bboxIntersect,false);
		} else {
			return seed;
		}
	}
}
