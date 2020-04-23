package ch.ethz.biol.cell.mpp.nrg.feature.objmask.cachedcalculation;

import java.util.Optional;

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
import org.anchoranalysis.feature.cache.calculation.CachedCalculation;
import org.anchoranalysis.feature.cache.calculation.RslvdCachedCalculation;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.feature.session.cache.ICachedCalculationSearch;
import org.anchoranalysis.image.extent.ImageDim;
import org.anchoranalysis.image.feature.objmask.FeatureInputSingleObj;
import org.anchoranalysis.image.objmask.ObjMask;
import org.anchoranalysis.image.objmask.morph.MorphologicalErosion;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;


public class CalculateShellObjMask extends CachedCalculation<ObjMask,FeatureInputSingleObj> {

	private int iterationsErosionSecond;
	private boolean do3D;
	private boolean inverse;

	private RslvdCachedCalculation<ObjMask,FeatureInputSingleObj> ccDilation;
	private RslvdCachedCalculation<ObjMask,FeatureInputSingleObj> ccErosion;
			
	private CalculateShellObjMask(
		RslvdCachedCalculation<ObjMask,FeatureInputSingleObj> ccDilation,
		RslvdCachedCalculation<ObjMask,FeatureInputSingleObj> ccErosion,
		int iterationsErosionSecond,
		boolean do3D,
		boolean inverse
	) {
		super();
		this.ccDilation = ccDilation;
		this.ccErosion = ccErosion;
		this.iterationsErosionSecond = iterationsErosionSecond;
		this.do3D = do3D;
		this.inverse = inverse;
	}
	
	
	@Override
	public boolean equals(final Object obj){
	    if(obj instanceof CalculateShellObjMask){
	        final CalculateShellObjMask other = (CalculateShellObjMask) obj;
	        return new EqualsBuilder()
	            .append(ccDilation, other.ccDilation)
	            .append(ccErosion, other.ccErosion)
	            .append(iterationsErosionSecond, other.iterationsErosionSecond)
	            .append(do3D, other.do3D)
	            .append(inverse, other.inverse)
	            .isEquals();
	    } else{
	        return false;
	    }
	}
	
	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(ccDilation).append(ccErosion).append(iterationsErosionSecond).append(do3D).append(inverse).toHashCode();
	}
	

	@Override
	public String toString() {
		return String.format(
			"%s ccDilation=%s, ccErosion=%s, do3D=%s, inverse=%s, iterationsErosionSecond=%d",
			super.toString(),
			ccDilation.toString(),
			ccErosion.toString(),
			do3D ? "true" : "false",
			inverse ? "true" : "false",
			iterationsErosionSecond
		);
	}
	
	
	public static CachedCalculation<ObjMask,FeatureInputSingleObj> createFromCache(
		ICachedCalculationSearch<FeatureInputSingleObj> params,
		int iterationsDilation,
		int iterationsErosion,
		int iterationsErosionSecond,
		boolean do3D,
		boolean inverse
	) throws FeatureCalcException {
		RslvdCachedCalculation<ObjMask,FeatureInputSingleObj> ccDilation = CalculateDilation.createFromCache(
			params, iterationsDilation, do3D	
		);
		RslvdCachedCalculation<ObjMask,FeatureInputSingleObj> ccErosion = CalculateErosion.createFromCacheRslvd(
			params, iterationsErosion, do3D
		);
	
		return new CalculateShellObjMask(
			ccDilation,
			ccErosion,
			iterationsErosionSecond,
			do3D,
			inverse
		);	
	}

	@Override
	protected ObjMask execute( FeatureInputSingleObj params ) throws ExecuteException {
		
		ImageDim sd = params.getNrgStack().getNrgStack().getDimensions();
		try {
			ObjMask om = params.getObjMask();
			ObjMask omShell = createShellObjMask( params, om, ccDilation, ccErosion, iterationsErosionSecond, do3D );
			
			if (inverse) {
				ObjMask omDup = om.duplicate();
				
				Optional<ObjMask> omShellIntersected = omShell.intersect( omDup, sd );
				omShellIntersected.ifPresent( shellIntersected ->
					omDup.binaryVoxelBox().setPixelsCheckMaskOff(
						shellIntersected.relMaskTo(omDup.getBoundingBox())
					)
				);
				return omDup;
				
			} else {
				return omShell;
			}
		
		} catch (CreateException e) {
			throw new ExecuteException(e);
		}	
	}
	
	private static ObjMask createShellObjMask(
		FeatureInputSingleObj params,
		ObjMask om,
		RslvdCachedCalculation<ObjMask,FeatureInputSingleObj> ccDilation,
		RslvdCachedCalculation<ObjMask,FeatureInputSingleObj> ccErosion,
		int iterationsErosionSecond,
		boolean do3D
	) throws CreateException, ExecuteException {

		ObjMask omDilated = ccDilation.getOrCalculate(params).duplicate();
		
		ObjMask omEroded = ccErosion.getOrCalculate(params);
		
		assert( omDilated.getBoundingBox().contains(omEroded.getBoundingBox()) );
		
		assert( omDilated!=null);
		assert( omEroded!=null);
		
		// Maybe apply a second erosion
		omDilated = iterationsErosionSecond>0 ? MorphologicalErosion.createErodedObjMask(
			omDilated,
			null,
			do3D,
			iterationsErosionSecond,
			true,
			null
		) : omDilated;
		
		ObjMask relMask = omEroded.relMaskTo( omDilated.getBoundingBox() );
		
		omDilated.binaryVoxelBox().setPixelsCheckMaskOff( relMask );
	
		return omDilated;
	}
}