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
import org.anchoranalysis.feature.cachedcalculation.CachedCalculation;
import org.anchoranalysis.feature.session.cache.ICachedCalculationSearch;
import org.anchoranalysis.image.extent.ImageDim;
import org.anchoranalysis.image.feature.objmask.FeatureObjMaskParams;
import org.anchoranalysis.image.feature.objmask.pair.FeatureObjMaskPairParams;
import org.anchoranalysis.image.objmask.ObjMask;
import org.anchoranalysis.image.objmask.morph.MorphologicalErosion;
import org.anchoranalysis.image.objmask.ops.ObjMaskMerger;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * Combines two object-masks by:
 *   1. dilating objMask1 by iterations1
 *   2. dilating objMask2 by iterations2
 *   3. finding the intersection of 1. and 2.
 *   4. then eroding by iterationsErosion 
 * 
 *  This is NOT commutative:  f(a,b)==f(b,a)
 * 
 * @author Owen Feehan
 *
 */
public class CalculatePairIntersection extends CachedCalculation<ObjMask,FeatureObjMaskPairParams> {

	private boolean do3D;
	private int iterationsErosion;
	
	private CachedCalculation<ObjMask,FeatureObjMaskParams> ccDilation1;
	private CachedCalculation<ObjMask,FeatureObjMaskParams> ccDilation2;
	
	
	
	public static CachedCalculation<ObjMask,FeatureObjMaskPairParams> createFromCache(
		ICachedCalculationSearch<FeatureObjMaskPairParams> cache,
		ICachedCalculationSearch<FeatureObjMaskParams> cacheDilationObj1,
		ICachedCalculationSearch<FeatureObjMaskParams> cacheDilationObj2,
		int iterations1,
		int iterations2,
		boolean do3D,
		int iterationsErosion
	) throws CreateException {
		
		// We use two additional caches, for the calculations involving the single objects, as these can be expensive, and we want
		//  them also cached
		CachedCalculation<ObjMask,FeatureObjMaskParams> ccDilation1 = CalculateDilation.createFromCache(
			cacheDilationObj1, iterations1, do3D	
		);
		CachedCalculation<ObjMask,FeatureObjMaskParams> ccDilation2 = CalculateDilation.createFromCache(
			cacheDilationObj2, iterations2, do3D	
		);
		return cache.search(
			new CalculatePairIntersection(do3D, iterationsErosion, ccDilation1, ccDilation2 )
		);
	}
		
	private CalculatePairIntersection(
		boolean do3D,
		int iterationsErosion,
		CachedCalculation<ObjMask,FeatureObjMaskParams> ccDilation1,
		CachedCalculation<ObjMask,FeatureObjMaskParams> ccDilation2
	) {
		super();
		this.iterationsErosion = iterationsErosion;
		this.do3D = do3D;
		this.ccDilation1 = ccDilation1;
		this.ccDilation2 = ccDilation2;
	}

	@Override
	protected ObjMask execute( FeatureObjMaskPairParams params ) throws ExecuteException {
	
		ImageDim dim = params.getNrgStack().getDimensions();
		
		ObjMask om1Dilated = ccDilation1.getOrCalculate( params.params1() );
		ObjMask om2Dilated = ccDilation2.getOrCalculate( params.params2() );
		
		ObjMask omIntersection  = om1Dilated.intersect(om2Dilated, dim );
		if (omIntersection==null) {
			return null;
		}
		
		ObjMask omMerged = params.getObjMaskMerged();
		
		try {
			if (iterationsErosion>0) {
				// We merge the two masks, and then erode it, and use this as a mask on the input object
				if (omMerged==null) {
					omMerged = ObjMaskMerger.merge( params.getObjMask1(), params.getObjMask2() );
				}
				
				ObjMask omMergedEroded = MorphologicalErosion.createErodedObjMask(omMerged, dim.getExtnt(), do3D, iterationsErosion, true, null);
				
				omIntersection = omIntersection.intersect(omMergedEroded, dim);
			}
			
			return omIntersection;
			
		} catch (CreateException e) {
			throw new ExecuteException(e);
		}
	}


	@Override
	public CachedCalculation<ObjMask,FeatureObjMaskPairParams> duplicate() {
		return new CalculatePairIntersection(
			do3D,
			iterationsErosion,
			ccDilation1.duplicate(),
			ccDilation2.duplicate()
		);
	}
	
	
	@Override
	public boolean equals(final Object obj){
	    if(obj instanceof CalculatePairIntersection){
	        final CalculatePairIntersection other = (CalculatePairIntersection) obj;
	        return new EqualsBuilder()
	            .append(iterationsErosion, other.iterationsErosion)
	            .append(do3D, other.do3D)
	            .append(ccDilation1, other.ccDilation1)
	            .append(ccDilation2, other.ccDilation2)
	            .isEquals();
	    } else{
	        return false;
	    }
	}
	
	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(iterationsErosion).append(do3D).append( ccDilation1 ).append( ccDilation2 ).toHashCode();
	}
}
