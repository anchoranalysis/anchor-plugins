package ch.ethz.biol.cell.sgmn.objmask.watershed.minimaimposition;

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

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.image.binary.BinaryChnl;
import org.anchoranalysis.image.binary.values.BinaryValuesByte;
import org.anchoranalysis.image.chnl.Chnl;
import org.anchoranalysis.image.chnl.factory.ChnlFactory;
import org.anchoranalysis.image.extent.BoundingBox;
import org.anchoranalysis.image.objmask.ObjMask;
import org.anchoranalysis.image.objmask.ObjMaskCollection;
import org.anchoranalysis.image.objmask.ops.BinaryChnlFromObjs;
import org.anchoranalysis.image.seed.SeedCollection;
import org.anchoranalysis.image.voxel.box.VoxelBox;
import org.anchoranalysis.image.voxel.box.VoxelBoxWrapper;
import org.anchoranalysis.image.voxel.box.factory.VoxelBoxFactory;

import ch.ethz.biol.cell.sgmn.objmask.watershed.minimaimposition.grayscalereconstruction.GrayscaleReconstructionByErosion;

public class MinimaImpositionGrayscaleReconstruction extends MinimaImposition {

	// START BEAN PROPERTIES
	@BeanField
	private GrayscaleReconstructionByErosion grayscaleReconstruction;
	// END BEAN PROPERTIES
	
	private VoxelBoxWrapper createMarkerImageFromGradient(VoxelBox<ByteBuffer> markerMaskVb,
			BinaryValuesByte maskBV,
			VoxelBoxWrapper gradientImage) {
		
		VoxelBoxWrapper out = VoxelBoxFactory.instance().create( gradientImage.any().extnt(), gradientImage.getVoxelDataType() );
		out.any().setAllPixelsTo( (int) gradientImage.getVoxelDataType().maxValue() );
		
		BoundingBox all = new BoundingBox(markerMaskVb.extnt());
		gradientImage.copyPixelsToCheckMask(all, out, all, markerMaskVb, maskBV);
		return out;
	}
	
	
	// containingMask can be null
	@Override
	public Chnl imposeMinima( Chnl chnl, SeedCollection seeds, ObjMask containingMask ) throws OperationFailedException {
		
		if (seeds.size()<1) {
			throw new OperationFailedException("There must be at least one seed");
			//return chnlBef;
		}
		 
		seeds.verifySeedsAreInside( chnl.getDimensions().getExtnt());
		
		ObjMaskCollection masks = seeds.createMasks();
				
		masks.assertObjMasksAreInside( chnl.getDimensions().getExtnt() );
				
		// We need 255 for the landini algorithms to work
		BinaryChnl markerMask;
		try {
			markerMask = BinaryChnlFromObjs.createFromObjs(masks, chnl.getDimensions(), masks.getFirstBinaryValues() );
		} catch (CreateException e) {
			throw new OperationFailedException(e);
		}

		// We duplicate the channel so we are not manipulating the original
		chnl = chnl.duplicate();
		
		VoxelBoxWrapper vbIntensity = chnl.getVoxelBox();
		
		// We set the EDM to 0 at the points of the minima
		for( ObjMask om : masks ) {
			vbIntensity.any().setPixelsCheckMask(om, 0 );
		}
		
		// We set the EDM to 255 outside the channel, otherwise the reconstruction will be messed up
		// Better alternative is to apply the reconstruction only on the ask
		if (containingMask!=null) {
			vbIntensity.any().setPixelsCheckMask(containingMask, (int) vbIntensity.getVoxelDataType().maxValue(), masks.getFirstBinaryValuesByte().getOffByte() );
		}
		
		VoxelBoxWrapper markerForReconstruction = createMarkerImageFromGradient(
			markerMask.getChnl().getVoxelBox().asByte(),
			markerMask.getBinaryValues().createByte(),
			vbIntensity
		);

		VoxelBoxWrapper reconBuffer = grayscaleReconstruction.reconstruction( vbIntensity, markerForReconstruction, containingMask );
		
		return ChnlFactory.instance().create(reconBuffer.any(), chnl.getDimensions().getRes() );
	}

	public GrayscaleReconstructionByErosion getGrayscaleReconstruction() {
		return grayscaleReconstruction;
	}

	public void setGrayscaleReconstruction(
			GrayscaleReconstructionByErosion grayscaleReconstruction) {
		this.grayscaleReconstruction = grayscaleReconstruction;
	}
}
