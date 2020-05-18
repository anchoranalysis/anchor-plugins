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
		
	private MarkSeeds() {
		super();
	}

	public static void doForAll( SeedCollection seeds, EncodedVoxelBox matS, Optional<MinimaStore> minimaStore ) throws OperationFailedException {
		
		for( Seed s : seeds ) {
			ObjMask om = s.createMask();
			
			if (!om.checkIfConnected()) {
				throw new OperationFailedException("Seed must be a single connected-component");
			}
			
			markSeed(om, matS, minimaStore );
		}
	}
	
	public static void doForMask( SeedCollection seeds, EncodedVoxelBox matS, Optional<MinimaStore> minimaStore, ObjMask containingMask ) throws OperationFailedException {
	
		if (!matS.extnt().equals(containingMask.getBoundingBox().extnt())) {
			throw new OperationFailedException("Extnt of matS does not match containingMask");
		}
		
		for( Seed s : seeds ) {
			ObjMask om = s.createMask();
			
			if (!om.checkIfConnected()) {
				throw new OperationFailedException("Seed must be a single connected-component");
			}
			
			markSeedContainingMask(om, matS, containingMask, minimaStore);
		}
	}
	
	private static void markSeed( ObjMask omSeed, EncodedVoxelBox matS, Optional<MinimaStore> minimaStore ) {
				
		Point3i crnrMin = omSeed.getBoundingBox().getCrnrMin();
		Point3i crnrMax = omSeed.getBoundingBox().calcCrnrMax();
		
		int id = -1;
		
		
		
		byte maskOn = BinaryValuesByte.getDefault().getOnByte(); 
				
		for( int z=crnrMin.getZ(); z<=crnrMax.getZ(); z++) {
			
			ByteBuffer bb = omSeed.getVoxelBox().getPixelsForPlane(z-omSeed.getBoundingBox().getCrnrMin().getZ()).buffer();
			
			int index = 0;
			for( int y=crnrMin.getY(); y<=crnrMax.getY(); y++) {
				for( int x=crnrMin.getX(); x<=crnrMax.getX(); x++) {
					
					
					
					if (bb.get(index)==maskOn) {
						// We write a connected component id based upon the first
						if (id==-1) {
							id = matS.extnt().offset(x, y, z);
							
							if (minimaStore.isPresent()) {
								minimaStore.get().add( new Point3i(x,y,z) );
							}
						}
												
						matS.setPointConnectedComponentID(new Point3i(x,y,z), id);
					}
					index++;
				}
			}
		}
	}
	
	private static void markSeedContainingMask( ObjMask omSeed, EncodedVoxelBox matS, ObjMask containingMask, Optional<MinimaStore> minimaStore ) {
		
		Point3i crnrMin = omSeed.getBoundingBox().getCrnrMin();
		Point3i crnrMax = omSeed.getBoundingBox().calcCrnrMax();
		
		int id = -1;
		
		byte maskOn = BinaryValuesByte.getDefault().getOnByte();
		
		Extent extntContainingMask = containingMask.getVoxelBox().extnt();
		
		Extent extnt = omSeed.getVoxelBox().extnt();
		
		for( int z=crnrMin.getZ(); z<=crnrMax.getZ(); z++) {
			
			ByteBuffer bb = omSeed.getVoxelBox().getPixelsForPlane(z-omSeed.getBoundingBox().getCrnrMin().getZ()).buffer();
			ByteBuffer bbContainingMask = containingMask.getVoxelBox().getPixelsForPlane(z).buffer();
			
			//int index = 0;
			for( int y=crnrMin.getY(); y<=crnrMax.getY(); y++) {
				for( int x=crnrMin.getX(); x<=crnrMax.getX(); x++) {
					
					int index =  extnt.offset(x-crnrMin.getX(), y-crnrMin.getY());
							
					if (bb.get(index)==maskOn) {
						
						int offsetContainingMask = extntContainingMask.offset(x, y);
						
						// We skip if our containing mask doesn't include it
						if (bbContainingMask.get(offsetContainingMask)==maskOn) {
						
							// We write a connected component id based upon the first
							if (id==-1) {
								id = matS.extnt().offset(x, y, z);
								
								if (minimaStore.isPresent()) {
									minimaStore.get().add( new Point3i(x,y,z) );
								}
							}
							
							matS.setPointConnectedComponentID(new Point3i(x,y,z), id);
						}
					}
					//index++;
				}
			}
		}
	}
}
