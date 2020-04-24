package org.anchoranalysis.plugin.image.feature.bean.obj.single.intensity;

import java.util.function.Function;

import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.chnl.Chnl;
import org.anchoranalysis.image.histogram.Histogram;
import org.anchoranalysis.image.histogram.HistogramFactoryUtilities;
import org.anchoranalysis.image.objmask.ObjMask;
import org.anchoranalysis.image.voxel.statistics.VoxelStatisticsFromHistogram;

import ch.ethz.biol.cell.mpp.nrg.feature.objmask.ValueAndIndex;

class StatsHelper {

	/**
	 * Calculates the mean-intensity of a masked-part of each slice, and returns the maximum value across all sices
	 * 
	 * @param chnl
	 * @param om
	 * @param excludeZero
	 * @return
	 * @throws FeatureCalcException
	 */
	public static ValueAndIndex calcMaxSliceMean( Chnl chnl, ObjMask om, boolean excludeZero ) throws FeatureCalcException {
		
		double max = Double.NEGATIVE_INFINITY;
		int index = -1;
		
		for( int z=0; z<om.getBoundingBox().extnt().getZ(); z++ ) {
			
			ObjMask omSlice;
			try {
				omSlice = om.extractSlice(z, true);
			} catch (OperationFailedException e) {
				throw new FeatureCalcException(e);
			}
			
			// We adjust the z coordiante to point to the channel
			int oldZ = omSlice.getBoundingBox().getCrnrMin().getZ();
			omSlice.getBoundingBox().getCrnrMin().setZ( oldZ + om.getBoundingBox().getCrnrMin().getZ() );
			
			if (omSlice.hasPixelsGreaterThan(0)) {
				double mean = IntensityMeanCalculator.calcMeanIntensityObjMask(chnl, omSlice, excludeZero);
				
				if (mean>max) {
					index = z;
					max = mean;
				}
			}
		}
		
		return new ValueAndIndex(max,index);
	}
	
	
	/**
	 * Calculates the mean-intensity of a certain number of (highest or lowest-intesntiy) pixels from the masked part of a chanel
	 * 
	 * <p>This number of pixels can either taken from the highest or lowest part of the histogram</p>
	 * 
	 * @param chnl
	 * @param om
	 * @param numPixels the number of pixels to be considered (either the highest-intensity pixels, or lowest-intensity pixel)
	 * @param highest iff TRUE the highest-intensity pixels are used in the mask, otherwise the lowest-intensity pixels are used
	 * @return
	 */
	public static double calcMeanNumPixels( Chnl chnl, ObjMask om, int numPixels, boolean highest ) {
		
		Histogram h = HistogramFactoryUtilities.create(chnl, om);
		
		Histogram hCut = highest ? h.extractPixelsFromRight(numPixels) : h.extractPixelsFromLeft(numPixels);
	
		return hCut.mean();
	}

	
	public static double calcStatistic(
		Chnl chnl,
		ObjMask objMask,
		boolean ignoreZero,
		double emptyValue,
		Function<VoxelStatisticsFromHistogram,Double> funcExtractStatistic
	) {
		Histogram hist = HistogramFactoryUtilities.createHistogramIgnoreZero(chnl,objMask,ignoreZero);
		
		if (hist.getTotalCount()==0) {
			return emptyValue;
		}
		
		return funcExtractStatistic.apply(
			new VoxelStatisticsFromHistogram(hist)
		);
	}
}
