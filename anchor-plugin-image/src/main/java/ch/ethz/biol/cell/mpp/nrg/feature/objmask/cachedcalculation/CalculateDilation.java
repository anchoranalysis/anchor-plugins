package ch.ethz.biol.cell.mpp.nrg.feature.objmask.cachedcalculation;

import org.anchoranalysis.feature.cache.calculation.RslvdCachedCalculation;
import org.anchoranalysis.feature.cache.calculation.map.RslvdCachedCalculationMap;
import org.anchoranalysis.feature.session.cache.ICachedCalculationSearch;
import org.anchoranalysis.image.feature.objmask.FeatureObjMaskParams;
import org.anchoranalysis.image.objmask.ObjMask;
import org.apache.commons.lang.builder.HashCodeBuilder;

public class CalculateDilation extends CalculateObjMask {

	private CalculateDilation(
		int iterations,
		RslvdCachedCalculationMap<ObjMask,FeatureObjMaskParams,Integer> map
	) {
		super(iterations,map);
	}
	
	private CalculateDilation( CalculateObjMask src ) {
		super(src);
	}
	
	public static RslvdCachedCalculation<ObjMask,FeatureObjMaskParams> createFromCache(
		ICachedCalculationSearch<FeatureObjMaskParams> cache,
		int iterations,
		boolean do3D
	) {
		RslvdCachedCalculationMap<ObjMask,FeatureObjMaskParams,Integer> map = cache.search(
			new CalculateDilationMap(do3D)
		);
		
		return cache.search(
			new CalculateDilation(iterations, map)
		);
	}

	@Override
	public boolean equals(final Object obj){
	    if(obj instanceof CalculateDilation){
	        return super.equals(obj);
	    } else{
	        return false;
	    }
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().appendSuper( super.hashCode() ).hashCode();
	}
}
