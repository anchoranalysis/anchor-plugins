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
import java.util.List;

import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.core.geometry.Point3i;
import org.anchoranalysis.core.log.LogErrorReporter;
import org.anchoranalysis.core.params.KeyValueParams;
import org.anchoranalysis.core.random.RandomNumberGenerator;
import org.anchoranalysis.feature.bean.Feature;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.feature.calc.params.FeatureCalcParams;
import org.anchoranalysis.feature.session.SessionFactory;
import org.anchoranalysis.feature.session.calculator.FeatureCalculatorSingle;
import org.anchoranalysis.feature.session.calculator.FeatureCalculatorSingleFromMulti;
import org.anchoranalysis.feature.shared.SharedFeatureSet;
import org.anchoranalysis.image.extent.Extent;
import org.anchoranalysis.image.feature.pixelwise.PixelwiseFeatureInitParams;
import org.anchoranalysis.image.feature.pixelwise.score.PixelScoreFeatureCalcParams;
import org.anchoranalysis.image.histogram.Histogram;
import org.anchoranalysis.image.histogram.HistogramFactoryUtilities;
import org.anchoranalysis.image.objmask.ObjMask;
import org.anchoranalysis.image.voxel.box.VoxelBox;
import org.anchoranalysis.image.voxel.box.VoxelBoxList;
import org.anchoranalysis.image.voxel.box.VoxelBoxWrapper;
import org.anchoranalysis.image.voxel.box.factory.VoxelBoxFactory;
import org.anchoranalysis.image.voxel.buffer.VoxelBuffer;

public class CreateVoxelBoxFromPixelwiseFeatureWithMask {

	private VoxelBoxList listVoxelBox;
	private KeyValueParams keyValueParams;
	private RandomNumberGenerator re;
	private List<Histogram> listAdditionalHistograms;

	private PixelwiseFeatureInitParams paramsInit;
	
	// Constructor
	public CreateVoxelBoxFromPixelwiseFeatureWithMask(VoxelBoxList listVoxelBox, RandomNumberGenerator re, KeyValueParams keyValueParams, List<Histogram> listAdditionalHistograms ) {
		super();
		this.listVoxelBox = listVoxelBox;
		this.keyValueParams = keyValueParams;
		this.listAdditionalHistograms = listAdditionalHistograms;
	}
	
	// objMask can be null
	public VoxelBox<ByteBuffer> createVoxelBoxFromPixelScore( Feature<PixelScoreFeatureCalcParams> pixelScore, ObjMask objMask, LogErrorReporter logger ) throws CreateException {
	
		// Sets up the Feature
		try {
			init( pixelScore, objMask );
			
			Extent e = listVoxelBox.getFirstExtnt();
			
			// We make our index buffer
			VoxelBox<ByteBuffer> vbOut = VoxelBoxFactory.getByte().create(e);
			
			if (objMask!=null) {
				setPixelsWithMask( vbOut, objMask, pixelScore, logger  );
			} else {
				setPixelsWithoutMask( vbOut, pixelScore, logger  );
			}
			return vbOut;
			
		} catch (InitException | FeatureCalcException e) {
			throw new CreateException(e);
		}
	}
	
	

	// Sets up the feature
	private void init( Feature<PixelScoreFeatureCalcParams> pixelScore, ObjMask objMask ) throws InitException {
		paramsInit = new PixelwiseFeatureInitParams(re);
		if (keyValueParams!=null) {
			paramsInit.setKeyValueParams(keyValueParams);
		}
		for( VoxelBoxWrapper voxelBox : listVoxelBox) {
			paramsInit.addListHist( HistogramFactoryUtilities.createWithMask(voxelBox.any(), objMask) );
		}
		
		for( Histogram hist : listAdditionalHistograms ) {
			paramsInit.addListHist( hist );
		}
	}

	private void setPixelsWithoutMask( VoxelBox<ByteBuffer> vbOut, Feature<PixelScoreFeatureCalcParams>pixelScore, LogErrorReporter logger ) throws FeatureCalcException, InitException {
		
		FeatureCalculatorSingle<PixelScoreFeatureCalcParams> session = createStartSession(pixelScore, logger);
		
		Extent e = vbOut.extnt();
		
		for( int z=0;z<e.getZ(); z++) {
			
			List<VoxelBuffer<?>> bbList = listVoxelBox.bufferListForSlice(z);

			ByteBuffer bbOut = vbOut.getPixelsForPlane(z).buffer();
			
			for( int y=0;y<=e.getY(); y++) {
				for( int x=0; x<e.getX(); x++) {
					
					int offset = e.offset(x, y);
					BufferUtilities.putScoreForOffset(session, bbList, bbOut, offset);
				}
					
			}
		}
	}

	private void setPixelsWithMask( VoxelBox<ByteBuffer> vbOut, ObjMask objMask, Feature<PixelScoreFeatureCalcParams> pixelScore, LogErrorReporter logger ) throws FeatureCalcException, InitException {
		
		FeatureCalculatorSingle<PixelScoreFeatureCalcParams> session = createStartSession(pixelScore, logger);
		
		byte maskOn = objMask.getBinaryValuesByte().getOnByte();
		Extent e = vbOut.extnt();
		Extent eMask = objMask.binaryVoxelBox().extnt();
		
		Point3i crnrMin = objMask.getBoundingBox().getCrnrMin();
		Point3i crnrMax = objMask.getBoundingBox().calcCrnrMax();
		
		for( int z=crnrMin.getZ();z<=crnrMax.getZ(); z++) {
			
			List<VoxelBuffer<?>> bbList = listVoxelBox.bufferListForSlice(z);

			int zRel = z-crnrMin.getZ();
			
			ByteBuffer bbMask = objMask.getVoxelBox().getPixelsForPlane(zRel).buffer();
			ByteBuffer bbOut = vbOut.getPixelsForPlane(z).buffer();
			
			for( int y=crnrMin.getY();y<=crnrMax.getY(); y++) {
				for( int x=crnrMin.getX();x<=crnrMax.getX(); x++) {
					
					int offset = e.offset(x, y);
					
					int offsetMask = eMask.offset(x-crnrMin.getX(),y-crnrMin.getY());
					
					if (bbMask.get(offsetMask)==maskOn) {
						BufferUtilities.putScoreForOffset(session, bbList, bbOut, offset);
					}
				}
					
			}
		}
	}
	
	
	private <T extends FeatureCalcParams> FeatureCalculatorSingle<T> createStartSession(Feature<T> pixelScore, LogErrorReporter logger) throws FeatureCalcException {
		return SessionFactory.createAndStart(
			pixelScore,
			paramsInit,
			new SharedFeatureSet<>(),
			logger
		);
	}


}