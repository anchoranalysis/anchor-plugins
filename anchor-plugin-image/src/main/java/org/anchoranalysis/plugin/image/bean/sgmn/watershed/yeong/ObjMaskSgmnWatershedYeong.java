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


import java.nio.IntBuffer;
import java.util.Optional;

import org.anchoranalysis.bean.OptionalFactory;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.image.bean.sgmn.objmask.ObjMaskSgmn;
import org.anchoranalysis.image.chnl.Chnl;
import org.anchoranalysis.image.extent.Extent;
import org.anchoranalysis.image.objmask.ObjMask;
import org.anchoranalysis.image.objmask.ObjMaskCollection;
import org.anchoranalysis.image.seed.SeedCollection;
import org.anchoranalysis.image.sgmn.SgmnFailedException;
import org.anchoranalysis.image.voxel.box.VoxelBox;
import org.anchoranalysis.image.voxel.box.factory.VoxelBoxFactory;

import ch.ethz.biol.cell.sgmn.objmask.watershed.encoding.EncodedVoxelBox;


/**
 *  A 'rainfall' watershed algorithm
 * 
 * <div>
 *  See:
 *  <ul>
 *  <li>3D watershed based on rainfall-simulation for volume segmentation, Yeong et al.
 *    2009 International Conference on Intelligent Human-Machine Systems and Cybernetics</li>
 *  <li>An improved watershed algorithm based on efficient computation of shortest paths, Osma-Ruiz et al., Pattern Reconigion(40), 2007</li>
 *  </ul>
 *  </div>
 *  
 *  <div>
 *  Note:
 *  <ul>
 *  <li>Does not record a watershed line</li> 
 *  </ul>
 *  </div>
 * 
 * @author Owen Feehan
 */
public class ObjMaskSgmnWatershedYeong extends ObjMaskSgmn {

	// START PROPERTIES
	// Exits and just returns the minima. NB TODO There is probably a more efficient way to find the minima. This is a quick fix
	@BeanField
	private boolean exitWithMinima = false;
	// END PROPERTIES
	
	@Override
	public ObjMaskCollection sgmn(Chnl chnl, Optional<ObjMask> mask,
			Optional<SeedCollection> seeds) throws SgmnFailedException {

		EncodedVoxelBox matS = createS(chnl.getDimensions().getExtnt());
		
		Optional<MinimaStore> minimaStore = OptionalFactory.create(
			exitWithMinima,
			() -> new MinimaStore()
		);
		
		maskSeedsIfPresent(seeds, matS, minimaStore, mask);
		
		PointPixelsOrMarkAsMinima.apply( chnl.getVoxelBox().any(), matS, mask, minimaStore);

		// Special behaviour where we just want to find the minima and nothing more
		if (minimaStore.isPresent()) {
			try {
				return minimaStore.get().createObjMasks();
			} catch (CreateException e) {
				throw new SgmnFailedException(e);
			}
		}
		
		// TODO let's only work on the areas with regions
		ConvertAllToConnectedComponents.apply(matS, mask);
		
		return CreateObjectsFromLabels.apply(matS.getVoxelBox(), mask);
	}
	
	/** Create 'S' matrix */
	private EncodedVoxelBox createS(Extent extent) {
		VoxelBox<IntBuffer> matSVoxelBox = VoxelBoxFactory.instance().getInt().create(extent);
		return new EncodedVoxelBox(matSVoxelBox);
	}
	
	private void maskSeedsIfPresent(Optional<SeedCollection> seeds, EncodedVoxelBox matS, Optional<MinimaStore> minimaStore, Optional<ObjMask> mask) throws SgmnFailedException {
		if (seeds.isPresent()) {
			try {
				MarkSeeds.apply(seeds.get(), matS, minimaStore, mask);
			} catch (OperationFailedException e) {
				throw new SgmnFailedException(e);
			}
		}
	}

	public boolean isExitWithMinima() {
		return exitWithMinima;
	}

	public void setExitWithMinima(boolean exitWithMinima) {
		this.exitWithMinima = exitWithMinima;
	}
}
