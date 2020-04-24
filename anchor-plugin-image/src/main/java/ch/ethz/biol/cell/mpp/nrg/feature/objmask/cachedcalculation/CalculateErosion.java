package ch.ethz.biol.cell.mpp.nrg.feature.objmask.cachedcalculation;

import org.anchoranalysis.feature.cache.calculation.CacheableCalculation;
import org.anchoranalysis.feature.cache.calculation.CalculationResolver;
import org.anchoranalysis.feature.cache.calculation.ResolvedCalculation;
import org.anchoranalysis.feature.cache.calculation.ResolvedCalculationMap;
import org.anchoranalysis.image.feature.objmask.FeatureInputSingleObj;
import org.anchoranalysis.image.objmask.ObjMask;
import org.apache.commons.lang.builder.HashCodeBuilder;

public class CalculateErosion extends CalculateObjMask {

	private CalculateErosion(
		int iterations,
		ResolvedCalculationMap<ObjMask,FeatureInputSingleObj,Integer> map
	) {
		super(iterations,map);
	}
	
	private CalculateErosion( CalculateObjMask src ) {
		super(src);
	}
	
	public static CacheableCalculation<ObjMask,FeatureInputSingleObj> create(
		CalculationResolver<FeatureInputSingleObj> cache,
		int iterations,
		boolean do3D
	) {
		ResolvedCalculationMap<ObjMask,FeatureInputSingleObj,Integer> map = cache.search(
			new CalculateErosionMap(do3D)
		);
		
		return new CalculateErosion(iterations, map);
	}
	
	public static ResolvedCalculation<ObjMask,FeatureInputSingleObj> createFromCacheRslvd(
		CalculationResolver<FeatureInputSingleObj> cache,
		int iterations,
		boolean do3D
	) {
		return cache.search(
			create(cache, iterations, do3D)
		);
	}
	
	@Override
	public boolean equals(final Object obj){
	    if(obj instanceof CalculateErosion){
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
