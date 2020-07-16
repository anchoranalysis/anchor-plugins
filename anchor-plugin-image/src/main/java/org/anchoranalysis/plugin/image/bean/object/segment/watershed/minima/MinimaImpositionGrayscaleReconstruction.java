package org.anchoranalysis.plugin.image.bean.object.segment.watershed.minima;

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

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.image.binary.mask.Mask;
import org.anchoranalysis.image.binary.values.BinaryValuesByte;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.channel.factory.ChannelFactory;
import org.anchoranalysis.image.extent.BoundingBox;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.image.object.ops.BinaryChnlFromObjects;
import org.anchoranalysis.image.seed.SeedCollection;
import org.anchoranalysis.image.voxel.box.VoxelBox;
import org.anchoranalysis.image.voxel.box.VoxelBoxWrapper;
import org.anchoranalysis.image.voxel.box.factory.VoxelBoxFactory;
import org.anchoranalysis.plugin.image.bean.object.segment.watershed.minima.grayscalereconstruction.GrayscaleReconstructionByErosion;

import lombok.Getter;
import lombok.Setter;

public class MinimaImpositionGrayscaleReconstruction extends MinimaImposition {

	// START BEAN PROPERTIES
	@BeanField @Getter @Setter
	private GrayscaleReconstructionByErosion grayscaleReconstruction;
	// END BEAN PROPERTIES
	
	private VoxelBoxWrapper createMarkerImageFromGradient(
		VoxelBox<ByteBuffer> markerMaskVb,
		BinaryValuesByte maskBV,
		VoxelBoxWrapper gradientImage
	) {
		
		VoxelBoxWrapper out = VoxelBoxFactory.instance().create(
			gradientImage.any().extent(),
			gradientImage.getVoxelDataType()
		);
		out.any().setAllPixelsTo( (int) gradientImage.getVoxelDataType().maxValue() );
		
		BoundingBox all = new BoundingBox(markerMaskVb.extent());
		gradientImage.copyPixelsToCheckMask(all, out, all, markerMaskVb, maskBV);
		return out;
	}
	
	
	// containingMask can be null
	@Override
	public Channel imposeMinima( Channel chnl, SeedCollection seeds, Optional<ObjectMask> containingMask ) throws OperationFailedException {
		
		if (seeds.size()<1) {
			throw new OperationFailedException("There must be at least one seed");
		}
		 
		seeds.verifySeedsAreInside( chnl.getDimensions().getExtent());
		
		ObjectCollection masks = seeds.createMasks();
				
		// We need 255 for the landini algorithms to work
		Mask markerMask = BinaryChnlFromObjects.createFromObjects(
			masks,
			chnl.getDimensions(),
			masks.getFirstBinaryValues()
		);

		// We duplicate the channel so we are not manipulating the original
		chnl = chnl.duplicate();
		
		VoxelBoxWrapper vbIntensity = chnl.getVoxelBox();
		
		// We set the EDM to 0 at the points of the minima
		for( ObjectMask object : masks ) {
			vbIntensity.any().setPixelsCheckMask(object, 0 );
		}
		
		// We set the EDM to 255 outside the channel, otherwise the reconstruction will be messed up
		// Better alternative is to apply the reconstruction only on the ask
		if (containingMask.isPresent()) {
			vbIntensity.any().setPixelsCheckMask(
				containingMask.get(),
				(int) vbIntensity.getVoxelDataType().maxValue(), masks.getFirstBinaryValuesByte().getOffByte()
			);
		}
		
		VoxelBoxWrapper markerForReconstruction = createMarkerImageFromGradient(
			markerMask.getChannel().getVoxelBox().asByte(),
			markerMask.getBinaryValues().createByte(),
			vbIntensity
		);

		VoxelBoxWrapper reconBuffer = grayscaleReconstruction.reconstruction(
			vbIntensity,
			markerForReconstruction,
			containingMask
		);
		
		return ChannelFactory.instance().create(reconBuffer.any(), chnl.getDimensions().getRes() );
	}
}
