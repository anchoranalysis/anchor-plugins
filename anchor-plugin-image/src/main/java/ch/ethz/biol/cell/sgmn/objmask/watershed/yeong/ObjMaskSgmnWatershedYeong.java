package ch.ethz.biol.cell.sgmn.objmask.watershed.yeong;

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


import java.nio.IntBuffer;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.image.bean.sgmn.objmask.ObjMaskSgmn;
import org.anchoranalysis.image.chnl.Chnl;
import org.anchoranalysis.image.objmask.ObjMask;
import org.anchoranalysis.image.objmask.ObjMaskCollection;
import org.anchoranalysis.image.seed.SeedCollection;
import org.anchoranalysis.image.sgmn.SgmnFailedException;
import org.anchoranalysis.image.voxel.box.VoxelBox;
import org.anchoranalysis.image.voxel.box.factory.VoxelBoxFactory;

import ch.ethz.biol.cell.sgmn.objmask.ObjMaskSgmnFloodFillStack;
import ch.ethz.biol.cell.sgmn.objmask.watershed.encoding.EncodedVoxelBox;


// A 'rainfall' watershed algorithm
//
// See:
//  * 3D watershed based on rainfall-simulation for volume segmentation, Yeong et al.
//    2009 International Conference on Intelligent Human-Machine Systems and Cybernetics
//
//  * An improved watershed algorithm based on efficient computation of shortest paths, Osma-Ruiz et al., Pattern Reconigion(40), 2007
//
//  Does not use seeds (markers)
//  Does not record a watershed lne
//
public class ObjMaskSgmnWatershedYeong extends ObjMaskSgmn {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// START PROPERTIES
	@BeanField
	private ObjMaskSgmn sgmnFloodFill = new ObjMaskSgmnFloodFillStack();
	
	// Exits and just returns the minima. NB TODO There is probably a more efficient way to find the minima. This is a quick fix
	@BeanField
	private boolean exitWithMinima = false;
	// END PROPERTIES
	
	@Override
	public ObjMaskCollection sgmn(Chnl chnl, SeedCollection seeds)
			throws SgmnFailedException {
		
		VoxelBox<IntBuffer> matSVoxelBox = VoxelBoxFactory.getInt().create( chnl.getDimensions().getExtnt() );
		
		EncodedVoxelBox matS = new EncodedVoxelBox(matSVoxelBox );
		
		MinimaStore minimaStore = exitWithMinima ? new MinimaStore() : null;

		if (seeds!=null) {
			try {
				MarkSeeds.doForAll(seeds, matS, minimaStore);
			} catch (OperationFailedException e) {
				throw new SgmnFailedException(e);
			}
		}
		
		new PointPixelsOrMarkAsMinima().doForAll( chnl.getVoxelBox(), matS, minimaStore );
		
		// Special behaviour where we just want to find the minima and nothing more
		if (exitWithMinima) {
			try {
				return minimaStore.createObjMasks();
			} catch (CreateException e) {
				throw new SgmnFailedException(e);
			}
		}
		
		// TODO let's only work on the areas with regions
		new ConvertAllToConnectedComponents().doForAll( matS );

		ObjMaskCollection objsSgmn = CreateObjectsFromLabels.doForAll( matS.getVoxelBox() );
		
		return objsSgmn;
	}
	

	
	@Override
	public ObjMaskCollection sgmn(Chnl chnl, ObjMask objMask,
			SeedCollection seeds) throws SgmnFailedException {
		
		VoxelBox<IntBuffer> matSVoxelBox = VoxelBoxFactory.getInt().create( chnl.getDimensions().getExtnt() );

		EncodedVoxelBox matS = new EncodedVoxelBox(matSVoxelBox );
		
		MinimaStore minimaStore = exitWithMinima ? new MinimaStore() : null;
		
		if (seeds!=null) {
			
			try {
				if (objMask!=null) {
					//assert (seeds.doSeedsIntersectWithContainingMask(objMask));
					
					MarkSeeds.doForMask(seeds, matS, minimaStore, objMask);
				} else {
					MarkSeeds.doForAll(seeds, matS, minimaStore);
				}
			} catch (OperationFailedException e) {
				throw new SgmnFailedException(e);
			}
		}
		
		new PointPixelsOrMarkAsMinima().doForMask( chnl.getVoxelBox().any(), matS, objMask, minimaStore );

		// Special behaviour where we just want to find the minima and nothing more
		if (exitWithMinima) {
			try {
				return minimaStore.createObjMasks();
			} catch (CreateException e) {
				throw new SgmnFailedException(e);
			}
		}
		
		// TODO let's only work on the areas with regions
		new ConvertAllToConnectedComponents().doForMask( matS, objMask );
		
		ObjMaskCollection objsSgmn = CreateObjectsFromLabels.doForMask( matS.getVoxelBox(), objMask );
		
		return objsSgmn;
	}

	public boolean isExitWithMinima() {
		return exitWithMinima;
	}

	public void setExitWithMinima(boolean exitWithMinima) {
		this.exitWithMinima = exitWithMinima;
	}

	public ObjMaskSgmn getSgmnFloodFill() {
		return sgmnFloodFill;
	}

	public void setSgmnFloodFill(ObjMaskSgmn sgmnFloodFill) {
		this.sgmnFloodFill = sgmnFloodFill;
	}
}
