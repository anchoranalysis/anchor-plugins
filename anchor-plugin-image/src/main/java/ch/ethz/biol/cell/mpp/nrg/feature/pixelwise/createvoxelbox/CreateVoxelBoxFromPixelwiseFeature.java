package ch.ethz.biol.cell.mpp.nrg.feature.pixelwise.createvoxelbox;

/*
 * #%L
 * anchor-image-feature
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.core.log.LogErrorReporter;
import org.anchoranalysis.core.params.KeyValueParams;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.extent.Extent;
import org.anchoranalysis.image.feature.bean.pixelwise.PixelScore;
import org.anchoranalysis.image.histogram.Histogram;
import org.anchoranalysis.image.histogram.HistogramFactoryUtilities;
import org.anchoranalysis.image.voxel.box.VoxelBox;
import org.anchoranalysis.image.voxel.box.VoxelBoxList;
import org.anchoranalysis.image.voxel.box.VoxelBoxWrapper;
import org.anchoranalysis.image.voxel.box.factory.VoxelBoxFactory;
import org.anchoranalysis.image.voxel.buffer.VoxelBuffer;

public class CreateVoxelBoxFromPixelwiseFeature {

	private VoxelBoxList listVoxelBox;
	private Optional<KeyValueParams> keyValueParams;
	private List<Histogram> listAdditionalHistograms;

	// Constructor
	public CreateVoxelBoxFromPixelwiseFeature(VoxelBoxList listVoxelBox, Optional<KeyValueParams> keyValueParams, List<Histogram> listAdditionalHistograms ) {
		super();
		this.listVoxelBox = listVoxelBox;
		this.keyValueParams = keyValueParams;
		this.listAdditionalHistograms = listAdditionalHistograms;
	}
	
	// objMask can be null
	public VoxelBox<ByteBuffer> createVoxelBoxFromPixelScore( PixelScore pixelScore, LogErrorReporter logger ) throws CreateException {
	
		// Sets up the Feature
		try {
			Extent e = listVoxelBox.getFirstExtnt();
			
			// We make our index buffer
			VoxelBox<ByteBuffer> vbOut = VoxelBoxFactory.instance().getByte().create(e);
			setPixels( vbOut, pixelScore, logger );
			return vbOut;
			
		} catch (InitException | FeatureCalcException e) {
			throw new CreateException(e);
		}
	}
	
	private void setPixels( VoxelBox<ByteBuffer> vbOut, PixelScore pixelScore, LogErrorReporter logErrorReporter ) throws FeatureCalcException, InitException {
		
		pixelScore.init(
			createHistograms(),
			keyValueParams
		);
		
		
		Extent e = vbOut.extnt();
		
		for( int z=0;z<e.getZ(); z++) {
			
			List<VoxelBuffer<?>> bbList = listVoxelBox.bufferListForSlice(z);

			ByteBuffer bbOut = vbOut.getPixelsForPlane(z).buffer();
			
			for( int y=0;y<e.getY(); y++) {
				for( int x=0;x<e.getX(); x++) {
					
					int offset = e.offset(x, y);
					
					BufferUtilities.putScoreForOffset(pixelScore, bbList, bbOut, offset);
				}
					
			}
		}
	}
	
	private List<Histogram> createHistograms() {
		List<Histogram> out = new ArrayList<>();

		for( VoxelBoxWrapper voxelBox : listVoxelBox) {
			out.add( HistogramFactoryUtilities.create(voxelBox) );
		}
		
		for( Histogram hist : listAdditionalHistograms ) {
			out.add( hist );
		}
		
		return out;		
	}
}