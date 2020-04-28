package org.anchoranalysis.plugin.image.calculation;

import org.anchoranalysis.feature.cache.calculation.CalculationResolver;
import org.anchoranalysis.feature.cache.calculation.ResolvedCalculation;
import org.anchoranalysis.feature.cache.calculation.ResolvedCalculationMap;
import org.anchoranalysis.image.feature.objmask.FeatureInputSingleObj;
import org.anchoranalysis.image.objmask.ObjMask;
import org.apache.commons.lang.builder.HashCodeBuilder;

public class CalculateDilation extends CalculateObjMask {

	public static ResolvedCalculation<ObjMask,FeatureInputSingleObj> createFromCache(
		CalculationResolver<FeatureInputSingleObj> cache,
		int iterations,
		boolean do3D
	) {
		ResolvedCalculationMap<ObjMask,FeatureInputSingleObj,Integer> map = cache.search(
			new CalculateDilationMap(do3D)
		);
		
		return cache.search(
			new CalculateDilation(iterations, map)
		);
	}
	
	private CalculateDilation(
		int iterations,
		ResolvedCalculationMap<ObjMask,FeatureInputSingleObj,Integer> map
	) {
		super(iterations,map);
	}
	
	private CalculateDilation( CalculateObjMask src ) {
		super(src);
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
