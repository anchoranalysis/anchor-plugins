package ch.ethz.biol.cell.mpp.nrg.feature.objmask.cachedcalculation;

/*
 * #%L
 * anchor-plugin-image
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


import org.anchoranalysis.core.cache.ExecuteException;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.feature.cache.CacheSession;
import org.anchoranalysis.feature.cachedcalculation.CachedCalculation;
import org.anchoranalysis.feature.cachedcalculation.CachedCalculationCastParams;
import org.anchoranalysis.image.feature.objmask.FeatureObjMaskParams;
import org.anchoranalysis.image.objmask.ObjMask;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * 1. Constructs a 'shell' around an object by eroding iterationsErosion times
 * 2. Erodes the result a further iterationsErosionSecond times
 * 3. Returns: Region 1 less Region 2
 * 
 * @author Owen Feehan
 *
 */
public class CalculateShellTwoStage extends CachedCalculationCastParams<ObjMask,FeatureObjMaskParams> {

	private CachedCalculation<ObjMask> ccErosion;
	private CachedCalculation<ObjMask> ccFurther;
			
	private CalculateShellTwoStage(
		CachedCalculation<ObjMask> ccErosion,
		CachedCalculation<ObjMask> ccFurther
	) {
		super();
		this.ccErosion = ccErosion;
		this.ccFurther = ccFurther;
	}
	
	
	@Override
	public boolean equals(final Object obj){
	    if(obj instanceof CalculateShellTwoStage){
	        final CalculateShellTwoStage other = (CalculateShellTwoStage) obj;
	        return new EqualsBuilder()
	            .append(ccErosion, other.ccErosion)
	            .append(ccFurther, other.ccFurther)
	            .isEquals();
	    } else{
	        return false;
	    }
	}
	
	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(ccErosion).append(ccFurther).toHashCode();
	}
	

	@Override
	public String toString() {
		return String.format(
			"%s ccErosion=%s, ccFurther=%s",
			super.toString(),
			ccErosion.toString(),
			ccFurther.toString()
		);
	}
	
	
	
	
	
	public static CachedCalculation<ObjMask> createFromCache(
		CacheSession cache,
		int iterationsErosion,
		int iterationsFurther,
		boolean do3D
	) {
		
		if (iterationsErosion==0) {
			// Special case when iterationsErosion==0, we consider it instead as a ashell
			return CalculateShellObjMask.createFromCache(
				cache,
				0,
				iterationsFurther,
				0,
				do3D,
				false
			);
		}
		
		
		CachedCalculation<ObjMask> ccErosion = CalculateErosion.createFromCache(
			cache, iterationsErosion, do3D
		);
		assert ccErosion instanceof CalculateErosion;


		CachedCalculation<ObjMask> ccFurther = CalculateErosion.createFromCache(
			cache, iterationsErosion + iterationsFurther, do3D
		);
		assert ccErosion instanceof CalculateErosion;
		
		return cache.search(
			new CalculateShellTwoStage(
				ccErosion,
				ccFurther
			)	
		);
	}

	@Override
	protected ObjMask execute( FeatureObjMaskParams params ) throws ExecuteException {
		
		try {
			ObjMask om = params.getObjMask();
			return createShellObjMask( params, om, ccErosion, ccFurther );
		} catch (CreateException e) {
			throw new ExecuteException(e);
		}	
	}
	
	private static ObjMask createShellObjMask(
		FeatureObjMaskParams params,
		ObjMask om,
		CachedCalculation<ObjMask> ccErosion,
		CachedCalculation<ObjMask> ccFurther
	) throws CreateException {
		
		// We duplicate as we don't want to mess up the existing cache entry
		ObjMask omEroded;
		try {
			omEroded = ccErosion.getOrCalculate(params).duplicate();
		} catch (ExecuteException e) {
			throw new CreateException(e.getCause());
		}
		assert om.getBoundingBox().contains(omEroded.getBoundingBox());
		
		if (!omEroded.hasPixelsGreaterThan(0)) {
			throw new CreateException("Object disappears when doing the first erosion");
		}
		
		
		ObjMask omErodedSecond;
		try {
			omErodedSecond = ccFurther.getOrCalculate(params).duplicate();
		} catch (ExecuteException e) {
			throw new CreateException(e.getCause());
		}
		
		
		// Maybe apply a second erosion
//		ObjMask omErodedSecond = MorphologicalErosion.createErodedObjMask(
//			omEroded,
//			null,
//			do3D,
//			iterationsFurther,
//			true,
//			null
//		);
		
		assert omEroded.getBoundingBox().contains(omErodedSecond.getBoundingBox());
						
		ObjMask relMask = omErodedSecond.relMaskTo( omEroded.getBoundingBox() );
		
		omEroded.binaryVoxelBox().setPixelsCheckMaskOff( relMask );
	
		return omEroded;
	}


	@Override
	public CachedCalculation<ObjMask> duplicate() {
		return new CalculateShellTwoStage(
			ccErosion.duplicate(),
			ccFurther.duplicate()
		);
	}
}