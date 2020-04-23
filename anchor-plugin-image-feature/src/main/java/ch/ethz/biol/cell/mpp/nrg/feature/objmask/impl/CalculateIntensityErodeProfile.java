package ch.ethz.biol.cell.mpp.nrg.feature.objmask.impl;

/*-
 * #%L
 * anchor-plugin-image-feature
 * %%
 * Copyright (C) 2010 - 2019 Owen Feehan, ETH Zurich, University of Zurich, Hoffmann la Roche
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

import java.util.ArrayList;
import java.util.List;

import org.anchoranalysis.core.cache.ExecuteException;
import org.anchoranalysis.feature.cache.calculation.CachedCalculation;
import org.anchoranalysis.feature.cache.calculation.RslvdCachedCalculation;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.feature.session.cache.ICachedCalculationSearch;
import org.anchoranalysis.image.chnl.Chnl;
import org.anchoranalysis.image.feature.objmask.FeatureObjMaskParams;
import org.anchoranalysis.image.objmask.ObjMask;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import ch.ethz.biol.cell.mpp.nrg.feature.objmask.IntensityMean;
import ch.ethz.biol.cell.mpp.nrg.feature.objmask.cachedcalculation.CalculateShellTwoStage;

public class CalculateIntensityErodeProfile extends CachedCalculation<ErodeProfile,FeatureObjMaskParams> {

	private int nrgIndex = 0;
	
	private int iterationsErosionStart = 0;
	
	private int iterationsErosionEnd = 0;

	private boolean do3D = true;
	
	// List of cached calculations for creating the object for a given iteration
	private List<RslvdCachedCalculation<ObjMask,FeatureObjMaskParams>> calcShell;

	private CalculateIntensityErodeProfile(int nrgIndex, int iterationsErosionStart, int iterationsErosionEnd,
			boolean do3D, List<RslvdCachedCalculation<ObjMask,FeatureObjMaskParams>> calcShell) {
		super();
		this.nrgIndex = nrgIndex;
		this.iterationsErosionStart = iterationsErosionStart;
		this.iterationsErosionEnd = iterationsErosionEnd;
		this.do3D = do3D;
		this.calcShell = calcShell;
	}
	
	private static RslvdCachedCalculation<ObjMask,FeatureObjMaskParams> createShellCalculator( ICachedCalculationSearch<FeatureObjMaskParams> cache, int iterations, boolean do3D ) throws FeatureCalcException {
		return CalculateShellTwoStage.createFromCache(
			cache,
			iterations,
			1,
			do3D
		);
	}
	
	
	public static RslvdCachedCalculation<ErodeProfile,FeatureObjMaskParams> createFromCache(
			ICachedCalculationSearch<FeatureObjMaskParams> cache,
			int nrgIndex,
			int iterationsErosionStart,
			int iterationsErosionEnd,
			boolean do3D
		) throws FeatureCalcException {
			List<RslvdCachedCalculation<ObjMask,FeatureObjMaskParams>> listCalcShell = new ArrayList<>();
			for( int i=iterationsErosionStart; i<=iterationsErosionEnd; i++) {
				listCalcShell.add( createShellCalculator(cache,i, do3D) );
			}
		
			return cache.search(
				new CalculateIntensityErodeProfile(nrgIndex, iterationsErosionStart, iterationsErosionEnd, do3D, listCalcShell)
			);
		}
	
	@Override
	protected ErodeProfile execute(FeatureObjMaskParams params) throws ExecuteException {
		
		ErodeProfile erodeProfile = new ErodeProfile();
		
		Chnl chnl = params.getNrgStack().getNrgStack().getChnl(nrgIndex);
		
		// We calculate the mean-intensity on each part of it
		for( int i=iterationsErosionStart; i<=iterationsErosionEnd; i++) {
			
			ObjMask om;
			try {
				om = calcShell.get(i-iterationsErosionStart).getOrCalculate(params);
			} catch (ExecuteException e) {
				// We ignore, as we assume it's because the erosions have come to an end
				continue;
			}
			
			double featVal;
			try {
				featVal = IntensityMean.calcMeanIntensityObjMask(chnl, om );
			} catch (FeatureCalcException e) {
				throw new ExecuteException(e);
			}
			erodeProfile.add(i, featVal);
			
		}
		
		return erodeProfile;
	}
	
	@Override
	public boolean equals(final Object obj){
	    if(obj instanceof CalculateIntensityErodeProfile){
	        final CalculateIntensityErodeProfile other = (CalculateIntensityErodeProfile) obj;
	        return new EqualsBuilder()
	            .append(nrgIndex, other.nrgIndex)
	            .append(iterationsErosionStart, other.iterationsErosionEnd)
	            .append(iterationsErosionEnd, other.iterationsErosionEnd)
	            .append(do3D, other.do3D)
	            .isEquals();
	    } else{
	        return false;
	    }
	}
	
	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(nrgIndex).append(iterationsErosionStart).append(iterationsErosionEnd).append(do3D).toHashCode();
	}
}
