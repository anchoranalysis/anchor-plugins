package ch.ethz.biol.cell.mpp.nrg.feature.pixelwise.createvoxelbox;

/*-
 * #%L
 * anchor-plugin-image
 * %%
 * Copyright (C) 2010 - 2020 Owen Feehan
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

import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.feature.session.calculator.FeatureCalculatorSingle;
import org.anchoranalysis.image.feature.pixelwise.score.PixelScoreFeatureCalcParams;
import org.anchoranalysis.image.voxel.buffer.VoxelBuffer;

class BufferUtilities {

	public static void putScoreForOffset(
		FeatureCalculatorSingle<PixelScoreFeatureCalcParams> session,
		List<VoxelBuffer<?>> bbList,
		ByteBuffer bbOut,
		int offset
	) throws FeatureCalcException {
		double score = session.calcOne(
			createParams(bbList, offset)
		);
		
		int scoreInt = (int) Math.round(score * 255);
		bbOut.put(offset, (byte) scoreInt );
	}
	
	private static PixelScoreFeatureCalcParams createParams( List<VoxelBuffer<?>> bbList, int offset ) {
		
		PixelScoreFeatureCalcParams params = new PixelScoreFeatureCalcParams( bbList.size() );
		
		for( int c=0; c<bbList.size(); c++) {
			int pxl = bbList.get(c).getInt( offset );
			params.setPxl(c,pxl);
		}
		
		return params;
	}
}