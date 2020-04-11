package pixelscore;

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

import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.core.log.LogErrorReporter;
import org.anchoranalysis.feature.bean.Feature;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.feature.init.FeatureInitParams;
import org.anchoranalysis.feature.session.FeatureSession;
import org.anchoranalysis.feature.session.SequentialSession;
import org.anchoranalysis.feature.shared.SharedFeatureSet;
import org.anchoranalysis.image.feature.pixelwise.score.PixelScoreFeatureCalcParams;
import org.anchoranalysis.image.voxel.buffer.VoxelBuffer;

public class PixelScoreSession extends FeatureSession {

	private SequentialSession<PixelScoreFeatureCalcParams> delegate;
	
	public PixelScoreSession( Feature<PixelScoreFeatureCalcParams> feature ) {
		delegate = new SequentialSession<>(feature);
	}
	
	public void start(FeatureInitParams featureInitParams, SharedFeatureSet<PixelScoreFeatureCalcParams> sharedFeatures, LogErrorReporter logErrorReporter ) throws InitException {
		delegate.start(featureInitParams, sharedFeatures, logErrorReporter);
	}
	
	public double calc( List<VoxelBuffer<?>> bbList, int offset )
			throws FeatureCalcException {
				
		PixelScoreFeatureCalcParams params = createParams(bbList, offset);
		return delegate.calcOne( params ).get(0);
	}
	
	private static PixelScoreFeatureCalcParams createParams( List<VoxelBuffer<?>> bbList, int offset ) {
		
		PixelScoreFeatureCalcParams params = new PixelScoreFeatureCalcParams( bbList.size() );
		
		for( int c=0; c<bbList.size(); c++) {
			int pxl = bbList.get(c).getInt( offset );
			params.setPxl(c,pxl);
		}
		
		return params;
	}

	public double calc( VoxelBuffer<?> bufferPrimary, ByteBuffer[] buffersAdd, int offset )
			throws FeatureCalcException {
		PixelScoreFeatureCalcParams params = createParams(bufferPrimary, buffersAdd, offset);
		return delegate.calcOne( params ).get(0);
	}
	
	private static PixelScoreFeatureCalcParams createParams( VoxelBuffer<?> bufferPrimary, ByteBuffer[] buffersAdd, int offset ) {
		PixelScoreFeatureCalcParams params = new PixelScoreFeatureCalcParams( 1 + buffersAdd.length );
		
		int val = bufferPrimary.getInt(offset);
		params.setPxl(0, val);
		
		int i = 1;
		for( ByteBuffer bbAdd : buffersAdd) {
			params.setPxl(i++, bbAdd.getInt(offset) );
		}
		return params;
	}

}
