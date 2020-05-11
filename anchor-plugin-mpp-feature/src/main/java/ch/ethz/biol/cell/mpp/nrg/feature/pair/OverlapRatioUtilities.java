package ch.ethz.biol.cell.mpp.nrg.feature.pair;

import java.util.function.BiFunction;

import org.anchoranalysis.anchor.mpp.pxlmark.memo.PxlMarkMemo;
import org.anchoranalysis.feature.calc.FeatureCalcException;

class OverlapRatioUtilities {
	
	private OverlapRatioUtilities() {}

	/** Returns {@link Math::max} or {@link Math::min} depending on a flag */
	public static BiFunction<Long,Long,Long> maxOrMin( boolean useMax ) {
		return useMax ? Math::max : Math::min;
	}
	
	public static double calcOverlapRatio( PxlMarkMemo obj1, PxlMarkMemo obj2, double overlap, int regionID, boolean mip, BiFunction<Long,Long,Long> funcAgg ) throws FeatureCalcException {
		
		if (overlap==0.0) {
			return 0.0;
		}
		
		if (mip) {
			return overlap;
		} else {
			double volume = calcVolumeAgg(
				obj1,
				obj2,
				regionID,
				funcAgg
			);
			return overlap / volume;
		}
	}
	
	private static double calcVolumeAgg(PxlMarkMemo obj1, PxlMarkMemo obj2, int regionID, BiFunction<Long,Long,Long> funcAgg) throws FeatureCalcException {
		long size1 = sizeFromMemo(obj1, regionID);
		long size2 = sizeFromMemo(obj2, regionID);
		return funcAgg.apply(size1, size2);
	}
	
	private static long sizeFromMemo( PxlMarkMemo obj, int regionID ) {
		return obj.doOperation().statisticsForAllSlices(0, regionID).size();
	}
}
