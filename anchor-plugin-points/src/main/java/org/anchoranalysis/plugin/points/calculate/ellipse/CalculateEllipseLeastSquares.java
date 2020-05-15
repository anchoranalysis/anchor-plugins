package org.anchoranalysis.plugin.points.calculate.ellipse;

import org.anchoranalysis.anchor.mpp.bean.points.fitter.InsufficientPointsException;
import org.anchoranalysis.anchor.mpp.mark.conic.MarkEllipse;

/*
 * #%L
 * anchor-plugin-points
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


import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.feature.cache.calculation.FeatureCalculation;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.feature.nrg.NRGStack;
import org.anchoranalysis.image.feature.objmask.FeatureInputSingleObj;
import org.anchoranalysis.image.objmask.ObjMask;
import org.apache.commons.lang.builder.HashCodeBuilder;

import ch.ethz.biol.cell.mpp.mark.pointsfitter.LinearLeastSquaresEllipseFitter;

public class CalculateEllipseLeastSquares extends FeatureCalculation<ObjMaskAndEllipse, FeatureInputSingleObj> {

	private EllipseFactory factory;
	
	public CalculateEllipseLeastSquares() {
		super();
		
		factory = new EllipseFactory(
			new LinearLeastSquaresEllipseFitter()
		);
	}
		
	private static ObjMask extractEllipseSlice( ObjMask om ) throws CreateException {
		try {
			int zSliceCenter = (int) om.centerOfGravity().getZ();
			return om.extractSlice(zSliceCenter - om.getBoundingBox().getCrnrMin().getZ(), false);
		} catch (OperationFailedException e1) {
			throw new CreateException(e1);
		}
	}
	

	@Override
	protected ObjMaskAndEllipse execute( FeatureInputSingleObj input ) throws FeatureCalcException {
		
		try {
			NRGStack nrgStack = input.getNrgStackRequired().getNrgStack();
			
			ObjMask om = extractEllipseSlice( input.getObjMask() );
			
			// Shell Rad is arbitrary here for now
			MarkEllipse mark = factory.create(om,nrgStack.getDimensions(), 0.2, nrgStack.getChnl(0) );

			return new ObjMaskAndEllipse(om,mark);
		} catch (CreateException | InsufficientPointsException e) {
			throw new FeatureCalcException(e);
		}
	}
	
	@Override
	public boolean equals(final Object obj){
	    if(obj instanceof CalculateEllipseLeastSquares){
	        return true;
	    } else{
	        return false;
	    }
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().toHashCode();
	}
}
