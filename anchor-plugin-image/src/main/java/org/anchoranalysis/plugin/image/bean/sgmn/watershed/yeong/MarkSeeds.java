package org.anchoranalysis.plugin.image.bean.sgmn.watershed.yeong;

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


import java.nio.ByteBuffer;
import java.util.Optional;

import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.geometry.Point3i;
import org.anchoranalysis.image.binary.values.BinaryValuesByte;
import org.anchoranalysis.image.extent.Extent;
import org.anchoranalysis.image.objmask.ObjMask;
import org.anchoranalysis.image.seed.Seed;
import org.anchoranalysis.image.seed.SeedCollection;

import ch.ethz.biol.cell.sgmn.objmask.watershed.encoding.EncodedVoxelBox;


class MarkSeeds {
		
	private MarkSeeds() {}

	public static void apply( SeedCollection seeds, EncodedVoxelBox matS, Optional<MinimaStore> minimaStore, Optional<ObjMask> containingMask ) throws OperationFailedException {
			
		if (containingMask.isPresent() && !matS.extnt().equals(containingMask.get().getBoundingBox().extnt())) {
			throw new OperationFailedException("Extnt of matS does not match containingMask");
		}
		
		for( Seed s : seeds ) {
			
			SeedSlidingBuffer seedBuffer = new SeedSlidingBuffer(
				s.createMask()
			);
			seedBuffer.throwExceptionIfNotConnected();
		
			ConnectedComponentWriter ccWriter = new ConnectedComponentWriter(matS, minimaStore);
			if (containingMask.isPresent()) {
				markSeedWithMask(seedBuffer, ccWriter, containingMask.get());
			} else {
				markSeedWithoutMask(seedBuffer, ccWriter);
			}
		}
	}
	
	private static final class SeedSlidingBuffer {
		
		private ObjMask obj;
				
		public final Point3i crnrMin;
		public final Point3i crnrMax;
		public final byte maskOn = BinaryValuesByte.getDefault().getOnByte(); 
		
		/**
		 * A buffer for accessing a seed
		 * 
		 * @param obj the object representing a seed
		 */
		public SeedSlidingBuffer(ObjMask obj) {
			super();
			this.obj = obj;
			this.crnrMin = obj.getBoundingBox().getCrnrMin();
			this.crnrMax = obj.getBoundingBox().calcCrnrMax();
		}
		
		public void throwExceptionIfNotConnected() throws OperationFailedException {
			if (!obj.checkIfConnected()) {
				throw new OperationFailedException("Seed must be a single connected-component");
			}
			
		}
		
		/**
		 * A z-slice buffer given a global z-slice coordinate
		 * 
		 * @param zGlobal global z-value
		 * @return a buffer for a particular slice for the seed-object only
		 */
		public ByteBuffer bufferRelative(int zGlobal) {
			return obj.getVoxelBox().getPixelsForPlane(
				zGlobal - crnrMin.getZ()
			).buffer();
		}
		
		@FunctionalInterface
		public interface ProcessPoint {
			void process(Point3i pnt);
		}
		
		public void callForEachPoint( ByteBuffer bb, int z, ProcessPoint process ) {
			int index = 0;
			for( int y=crnrMin.getY(); y<=crnrMax.getY(); y++) {
				for( int x=crnrMin.getX(); x<=crnrMax.getX(); x++) {
					
					if (bb.get(index)==maskOn) {
						process.process(
							new Point3i(x,y,z)
						);
					}
					index++;
				}
			}
		}
	}
	
	private static void markSeedWithoutMask( SeedSlidingBuffer seed, ConnectedComponentWriter writer ) {
				
		for( int z=seed.crnrMin.getZ(); z<=seed.crnrMax.getZ(); z++) {
			
			ByteBuffer bb = seed.bufferRelative(z);
			
			seed.callForEachPoint(
				bb,
				z,
				pnt -> writer.writePoint(pnt)
			);
		}
	}
	
	private static void markSeedWithMask( SeedSlidingBuffer seed, ConnectedComponentWriter writer, ObjMask containingMask) {
		
		Extent extntContainingMask = containingMask.getVoxelBox().extnt();
				
		for( int z=seed.crnrMin.getZ(); z<=seed.crnrMax.getZ(); z++) {
			
			ByteBuffer bb = seed.bufferRelative(z);
			ByteBuffer bbContainingMask = containingMask.getVoxelBox().getPixelsForPlane(z).buffer();
						
			seed.callForEachPoint(
				bb,
				z,
				pnt -> {
					int offsetContainingMask = extntContainingMask.offset(pnt.getX(), pnt.getY());
					
					// We skip if our containing mask doesn't include it
					if (bbContainingMask.get(offsetContainingMask)==seed.maskOn) {
						writer.writePoint(pnt);
					}
				}
			);
		}
	}
}
