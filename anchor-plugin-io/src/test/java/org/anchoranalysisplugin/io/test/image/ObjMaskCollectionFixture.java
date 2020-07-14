package org.anchoranalysisplugin.io.test.image;

/*-
 * #%L
 * anchor-test-image
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
import java.util.Random;
import org.anchoranalysis.core.geometry.Point3i;
import org.anchoranalysis.image.extent.BoundingBox;
import org.anchoranalysis.image.extent.Extent;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.image.object.ObjectCollectionFactory;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.image.voxel.buffer.VoxelBuffer;

class ObjMaskCollectionFixture {
	
	// Maximum extent of a scene
	private static final Extent SCENE_EXTENT = new Extent(1000,900, 40);
	
	/** How frequently do we change value randomly when iterating over pixel values */
	private static final double PROBABILITY_CHANGE_VALUE = 0.2;
	
	private static final Random RANDOM = new Random();
	
	/**
	 * Creates an ObjMaskCollection containing between minNumObjs and maxNumObjs
	 * (range randomly sampled)
	 * 
	 * Each mask has a position and extent that is randomly-sampled, and contains voxels
	 *  that are each randomly on or off.
	 * 
	 * @param minNumObjs
	 * @param maxNumObjs
	 * @return
	 */
	public ObjectCollection createMockObjs( int minNumObjs, int maxNumObjs ) {
				
		int numObjs = randomMinMax( minNumObjs, maxNumObjs );
		
		return ObjectCollectionFactory.fromRepeated(numObjs, this::mockObj);
	}
	
	private ObjectMask mockObj() {
		Extent e = randomExtent();
		Point3i crnr = randomCrnr(e);
		return mockObj(crnr, e);
	}
		
	private ObjectMask mockObj( Point3i crnr, Extent e ) {
		
		ObjectMask object = new ObjectMask(
			new BoundingBox(crnr, e)
		);
		
		int volumeXY = e.getVolumeXY();
		for( int z=0; z<e.getZ(); z++) {
			
			VoxelBuffer<ByteBuffer> vb = object.getVoxelBox().getPixelsForPlane(z);
			ByteBuffer bb = vb.buffer();
			
			int prevVal = 0;
			
			for( int i=0; i<volumeXY; i++) {
				prevVal = randomMaybeChangeVal(prevVal);
				bb.put(
					(byte) prevVal
				);
			}
		}
		
		
		
		// Switch samples on an off with uniform randomness
		
		return object;
	}
	
	/** Randomly returns 0 or 255 with equal probability. prevVal must be 0 or 255 */
	private static int randomMaybeChangeVal( int prevVal ) {
		if (RANDOM.nextDouble() > PROBABILITY_CHANGE_VALUE) {
			return prevVal;
		} else {
			return (255-prevVal);
		}
	}
	
	/** Randomly returns an extent within a scene */
	private static Extent randomExtent() {
		int x = randomTotal( SCENE_EXTENT.getX()-1 ) + 1;
		int y = randomTotal( SCENE_EXTENT.getY()-1 ) + 1;
		int z = randomTotal( SCENE_EXTENT.getZ()-1 ) + 1;
		return new Extent(x, y, z);
	}
	
	/** A random starting corner, making sure there's enough room for the extent */
	private static Point3i randomCrnr( Extent e) {
		int x = randomSub( SCENE_EXTENT.getX(), e.getX() );
		int y = randomSub( SCENE_EXTENT.getY(), e.getY() );
		int z = randomSub( SCENE_EXTENT.getZ(), e.getZ() );
		return new Point3i(x, y, z);
	}
	
	private static int randomTotal( int total ) {
		return RANDOM.nextInt( total  );
	}
	
	private static int randomMinMax( int min, int max ) {
		return RANDOM.nextInt( max-min ) + min;
	}
	
	private static int randomSub( int total, int sub ) {
		return randomTotal( total - sub );
	}
}
