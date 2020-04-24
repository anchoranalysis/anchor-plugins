package org.anchoranalysis.plugin.image.feature.bean.obj.single.intensity;

import java.nio.ByteBuffer;

import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.geometry.Point3i;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.chnl.Chnl;
import org.anchoranalysis.image.convert.ByteConverter;
import org.anchoranalysis.image.extent.BoundingBox;
import org.anchoranalysis.image.histogram.Histogram;
import org.anchoranalysis.image.histogram.HistogramFactoryUtilities;
import org.anchoranalysis.image.objmask.ObjMask;
import org.anchoranalysis.image.voxel.VoxelIntensityList;
import org.anchoranalysis.image.voxel.box.VoxelBox;
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

	
	public static double calcStdDev( Chnl chnl, ObjMask objMask, boolean ignoreZero, double emptyValue ) {
		Histogram hist = HistogramFactoryUtilities.createHistogramIgnoreZero(chnl,objMask,ignoreZero);
		
		if (hist.getTotalCount()==0) {
			return emptyValue;
		}
		
		return new VoxelStatisticsFromHistogram(hist).stdDev();
	}
		
	public static double calcQuantileIntensityObjMask( Chnl chnl, ObjMask om, double quantile ) {
		
		Histogram h = HistogramFactoryUtilities.create(chnl, om);
		return h.quantile(quantile);
	}
	
	
	public static double calcVarianceObjMask( Chnl chnl, ObjMask om ) {
		
		VoxelBox<ByteBuffer> vbIntens = chnl.getVoxelBox().asByte();
		
		BoundingBox bbox = om.getBoundingBox();
		
		Point3i crnrMin = bbox.getCrnrMin();
		Point3i crnrMax = bbox.calcCrnrMax();
		
		VoxelIntensityList list = new VoxelIntensityList();
		
		for( int z=crnrMin.getZ(); z<=crnrMax.getZ(); z++) {
			
			ByteBuffer bbIntens = vbIntens.getPixelsForPlane( z ).buffer();
			ByteBuffer bbMask = om.getVoxelBox().getPixelsForPlane( z - crnrMin.getZ() ).buffer();
			
			int offsetMask = 0;
			for( int y=crnrMin.getY(); y<=crnrMax.getY(); y++) {
				for( int x=crnrMin.getX(); x<=crnrMax.getX(); x++) {
				
					if (bbMask.get(offsetMask)==om.getBinaryValuesByte().getOnByte()) {
						int offsetIntens = vbIntens.extnt().offset(x, y);
						
						list.add(
							ByteConverter.unsignedByteToInt( bbIntens.get(offsetIntens) )
						);
					}
							
					offsetMask++;
				}
			}
		}
		
		return list.variance( list.mean() );
	}
}
