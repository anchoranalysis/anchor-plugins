package ch.ethz.biol.cell.mpp.nrg.feature.objmask;

import java.util.Optional;

import org.anchoranalysis.bean.BeanInstanceMap;

/*
 * #%L
 * anchor-plugin-image-feature
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


import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.error.BeanMisconfiguredException;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.feature.cache.CacheableParams;
import org.anchoranalysis.feature.cache.calculation.CachedCalculation;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.feature.nrg.NRGStack;
import org.anchoranalysis.image.binary.values.BinaryValues;
import org.anchoranalysis.image.chnl.Chnl;
import org.anchoranalysis.image.extent.BoundingBox;
import org.anchoranalysis.image.feature.bean.objmask.FeatureObjMask;
import org.anchoranalysis.image.feature.objmask.FeatureInputSingleObj;
import org.anchoranalysis.image.objmask.ObjMask;

import ch.ethz.biol.cell.mpp.nrg.feature.objmask.IntensityMeanMaxSlice.ValueAndIndex;
import ch.ethz.biol.cell.mpp.nrg.feature.objmask.cachedcalculation.CalculateShellObjMask;

//
// Constructs a 'shell' around an object by a number of dilation/erosion operations (not including the original object mask)
//  and measures the mean intensity of this shell
//
public class IntensityMeanShellMaxSlice extends FeatureObjMask {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// START BEAN PROPERTIES
	@BeanField
	private int nrgIndex = 0;
	
	@BeanField
	private int iterationsDilation = 0;
	
	@BeanField
	private int iterationsErosion = 0;
	
	@BeanField
	private int iterationsErosionSecond = 0;
	
	@BeanField
	private boolean do3D = true;
	
	@BeanField
	private boolean inverse = false;		// Calculates instead on the inverse of the mask (what's left when the shell is removed)
	
	@BeanField
	private int nrgIndexMask = -1;
	
	@BeanField
	private boolean inverseMask = false;	// Uses the inverse of the passed mask
	
	@BeanField
	private double emptyValue = 255;
	// END BEAN PROPERTIES

	@Override
	public void checkMisconfigured( BeanInstanceMap defaultInstances ) throws BeanMisconfiguredException {
		super.checkMisconfigured( defaultInstances );
		if( iterationsDilation==0 && iterationsErosion==0 ) {
			throw new BeanMisconfiguredException("At least one of iterationsDilation and iterationsErosion must be positive");
		}
	}
	
	@Override
	public double calc(CacheableParams<FeatureInputSingleObj> paramsCacheable) throws FeatureCalcException {
		
		FeatureInputSingleObj params = paramsCacheable.getParams();
		
		NRGStack nrgStack = params.getNrgStack().getNrgStack();
		
		CachedCalculation<ObjMask,FeatureInputSingleObj> ccShellObjMask = CalculateShellObjMask.createFromCache(
			paramsCacheable,
			iterationsDilation,
			iterationsErosion,
			iterationsErosionSecond,
			do3D,
			inverse	
		);
		ObjMask om = paramsCacheable.calc(ccShellObjMask);

		
		if (nrgIndexMask!=-1) {
			Optional<ObjMask> omIntersected = om.intersect(
				createNrgMask(nrgStack),
				nrgStack.getDimensions()
			);
			
			if (omIntersected.isPresent()) {
				om = omIntersected.get();
			} else {
				return emptyValue;
			}
		}
				
		Chnl chnl = nrgStack.getChnl(nrgIndex);
		ValueAndIndex vai = IntensityMeanMaxSlice.calcMeanIntensityObjMask(chnl, om, false );
		
		if (vai.getIndex()==-1) {
			return emptyValue;
		}
	
		return vai.getValue();
	}
	
	private ObjMask createNrgMask( NRGStack nrgStack ) throws FeatureCalcException {
		try {
			return new ObjMask(
				new BoundingBox( nrgStack.getDimensions().getExtnt() ),
				nrgStack.getChnl(nrgIndexMask).getVoxelBox().asByte(),
				inverseMask ? BinaryValues.getDefault().createInverted() : BinaryValues.getDefault()
			);
		} catch (CreateException e) {
			throw new FeatureCalcException(e);
		}
	}

	public int getNrgIndex() {
		return nrgIndex;
	}

	public void setNrgIndex(int nrgIndex) {
		this.nrgIndex = nrgIndex;
	}

	public boolean isDo3D() {
		return do3D;
	}

	public void setDo3D(boolean do3d) {
		do3D = do3d;
	}

	public int getIterationsDilation() {
		return iterationsDilation;
	}

	public void setIterationsDilation(int iterationsDilation) {
		this.iterationsDilation = iterationsDilation;
	}

	public int getIterationsErosion() {
		return iterationsErosion;
	}

	public void setIterationsErosion(int iterationsErosion) {
		this.iterationsErosion = iterationsErosion;
	}

	public boolean isInverse() {
		return inverse;
	}

	public void setInverse(boolean inverse) {
		this.inverse = inverse;
	}

	public int getNrgIndexMask() {
		return nrgIndexMask;
	}

	public void setNrgIndexMask(int nrgIndexMask) {
		this.nrgIndexMask = nrgIndexMask;
	}

	public boolean isInverseMask() {
		return inverseMask;
	}

	public void setInverseMask(boolean inverseMask) {
		this.inverseMask = inverseMask;
	}

	public double getEmptyValue() {
		return emptyValue;
	}

	public void setEmptyValue(double emptyValue) {
		this.emptyValue = emptyValue;
	}

	public int getIterationsErosionSecond() {
		return iterationsErosionSecond;
	}

	public void setIterationsErosionSecond(int iterationsErosionSecond) {
		this.iterationsErosionSecond = iterationsErosionSecond;
	}

	@Override
	public String getParamDscr() {
		return String.format(
			"iterationsDilation=%d,iterationsErosion=%d,iterationsErosionSecond=%d,do3D=%s,inverse=%s",
			iterationsDilation,
			iterationsErosion,
			iterationsErosionSecond,
			do3D ? "true" : "false",
			inverse ? "true" : "false"
		);
	}

}
