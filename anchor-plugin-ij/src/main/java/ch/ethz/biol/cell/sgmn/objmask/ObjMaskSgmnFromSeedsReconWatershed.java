package ch.ethz.biol.cell.sgmn.objmask;

/*
 * #%L
 * anchor-plugin-ij
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


import ij.plugin.filter.EDM;
import ij.plugin.filter.MaximumFinder;
import ij.process.ByteProcessor;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;

import java.nio.ByteBuffer;
import java.util.Optional;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.Positive;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.image.bean.sgmn.binary.BinarySgmn;
import org.anchoranalysis.image.bean.sgmn.binary.BinarySgmnParameters;
import org.anchoranalysis.image.bean.sgmn.objmask.ObjMaskSgmn;
import org.anchoranalysis.image.binary.values.BinaryValuesByte;
import org.anchoranalysis.image.chnl.Chnl;
import org.anchoranalysis.image.chnl.factory.ChnlFactory;
import org.anchoranalysis.image.convert.IJWrap;
import org.anchoranalysis.image.objmask.ObjMask;
import org.anchoranalysis.image.objmask.ObjMaskCollection;
import org.anchoranalysis.image.seed.SeedCollection;
import org.anchoranalysis.image.sgmn.SgmnFailedException;
import org.anchoranalysis.image.stack.region.chnlconverter.ChnlConverterToUnsignedByte;
import org.anchoranalysis.image.stack.region.chnlconverter.ConversionPolicy;
import org.anchoranalysis.image.voxel.box.VoxelBox;
import org.anchoranalysis.image.voxel.box.VoxelBoxByte;
import org.anchoranalysis.image.voxel.box.VoxelBoxFloat;
import org.anchoranalysis.image.voxel.box.thresholder.VoxelBoxThresholder;
import org.anchoranalysis.image.voxel.datatype.IncorrectVoxelDataTypeException;
import org.anchoranalysis.image.voxel.datatype.VoxelDataTypeUnsignedByte;
import org.anchoranalysis.image.voxel.datatype.VoxelDataTypeFloat;

import ch.ethz.biol.cell.imageprocessing.voxelbox.pixelsforplane.PixelsFromByteProcessor;
import ch.ethz.biol.cell.imageprocessing.voxelbox.pixelsforplane.PixelsFromFloatProcessor;
import ch.ethz.biol.cell.sgmn.objmask.watershed.minimaimposition.MinimaImpositionGrayscaleReconstruction;

// TODO review. Do we use this? If so, it needs to be refactored.
public class ObjMaskSgmnFromSeedsReconWatershed extends ObjMaskSgmn {
	
	/* START Bean properties */
	@BeanField
	private BinarySgmn sgmnBinary;
	
	@BeanField
	private ObjMaskSgmn sgmnObj;
	
	@BeanField @Positive
	private int minBoundingBoxVolumeVoxels = 200;
	/* END Bean properties */
	
	@Override
	public ObjMaskCollection sgmn(
		Chnl chnl,
		Optional<ObjMask> mask,
		Optional<SeedCollection> seeds
	) throws SgmnFailedException {
		
		if (!seeds.isPresent()) {
			throw new SgmnFailedException("This operation requires seeds to be passed as parameters");
		}
		
		if (mask.isPresent()) {
			throw new SgmnFailedException("A mask is not supported by this operation.");
		}
		
		BinarySgmnParameters params = new BinarySgmnParameters(
			chnl.getDimensions().getRes()
		);
		
		Chnl chnlNucWorking = chnl;
		sgmnBinary.sgmn(
			chnlNucWorking.getVoxelBox(),
			params,
			Optional.empty()
		);
	

		
		EDM edmPlugin = new EDM();
		
		ImageProcessor ip = IJWrap.imageProcessorByte(chnlNucWorking.getVoxelBox().asByte().getPlaneAccess(), 0);
		FloatProcessor fp = edmPlugin.makeFloatEDM( ip, 0, false );

		MaximumFinder mf = new MaximumFinder();
				
		
		Chnl edm;
		{
			PixelsFromFloatProcessor pixelsForPlane = new PixelsFromFloatProcessor(fp);
			VoxelBoxFloat vb = new VoxelBoxFloat(pixelsForPlane);
			
			edm = ChnlFactory
					.instance()
					.get(VoxelDataTypeFloat.instance)
					.create(
						vb,
						chnlNucWorking.getDimensions().getRes()
					);
		}
		
		MinimaImpositionGrayscaleReconstruction minimaImposition = new MinimaImpositionGrayscaleReconstruction();
		
		Chnl edmAsByte = new ChnlConverterToUnsignedByte().convert(edm, ConversionPolicy.CHANGE_EXISTING_CHANNEL);
		Chnl edmRecon;
		try {
			edmRecon = seeds.get().size()>0 ? minimaImposition.imposeMinima(edmAsByte, seeds.get(), null) : new ChnlConverterToUnsignedByte().convert(edm,ConversionPolicy.CHANGE_EXISTING_CHANNEL);
			// Let's impose minima via morphological reconstruction
		} catch (OperationFailedException e) {
			throw new SgmnFailedException(e);
		}
		
		ImageProcessor processor = IJWrap.imageProcessorByte(edmRecon.getVoxelBox().asByte().getPlaneAccess(), 0);
		ByteProcessor bp = mf.findMaxima( processor, 0.5, 0, MaximumFinder.SEGMENTED, false, false );
		
		
		
		Chnl chnlWorking;
		{
			VoxelBox<ByteBuffer> vb = new VoxelBoxByte( new PixelsFromByteProcessor(bp) );
			
			chnlWorking = ChnlFactory
					.instance()
					.get(VoxelDataTypeUnsignedByte.instance)
					.create(vb, edmRecon.getDimensions().getRes() );
			
			// lets do a flood fill on dupp
			try {
				VoxelBoxThresholder.thresholdForLevel(chnlWorking.getVoxelBox().asByte(), 0, BinaryValuesByte.getDefault() );
			} catch (IncorrectVoxelDataTypeException | CreateException e) {
				throw new SgmnFailedException(e);
			}
		}
		
		// We threshold our working channel
		return sgmnObj.sgmn(chnlWorking, Optional.empty(), Optional.empty());
	}

	public int getMinBoundingBoxVolumeVoxels() {
		return minBoundingBoxVolumeVoxels;
	}

	public void setMinBoundingBoxVolumeVoxels(int minBoundingBoxVolumeVoxels) {
		this.minBoundingBoxVolumeVoxels = minBoundingBoxVolumeVoxels;
	}

	public BinarySgmn getSgmnBinary() {
		return sgmnBinary;
	}

	public void setSgmnBinary(BinarySgmn sgmnBinary) {
		this.sgmnBinary = sgmnBinary;
	}

	public ObjMaskSgmn getSgmnObj() {
		return sgmnObj;
	}

	public void setSgmnObj(ObjMaskSgmn sgmnObj) {
		this.sgmnObj = sgmnObj;
	}
}
