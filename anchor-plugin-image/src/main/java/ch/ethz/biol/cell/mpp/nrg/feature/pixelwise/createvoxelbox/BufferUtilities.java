package ch.ethz.biol.cell.mpp.nrg.feature.pixelwise.createvoxelbox;

import java.nio.ByteBuffer;
import java.util.List;

import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.feature.session.calculator.FeatureCalculatorSingle;
import org.anchoranalysis.feature.session.calculator.FeatureCalculatorSingleFromMulti;
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
