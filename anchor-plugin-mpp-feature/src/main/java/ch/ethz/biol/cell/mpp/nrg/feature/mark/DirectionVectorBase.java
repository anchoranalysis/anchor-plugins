package ch.ethz.biol.cell.mpp.nrg.feature.mark;

/*-
 * #%L
 * anchor-plugin-mpp-feature
 * %%
 * Copyright (C) 2010 - 2020 Owen Feehan
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

import org.anchoranalysis.anchor.mpp.feature.bean.mark.FeatureMark;
import org.anchoranalysis.anchor.mpp.feature.bean.mark.FeatureMarkParams;
import org.anchoranalysis.anchor.mpp.mark.conic.MarkEllipsoid;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.core.geometry.Vector3d;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.feature.init.FeatureInitParams;
import org.anchoranalysis.image.bean.orientation.DirectionVectorBean;
import org.anchoranalysis.image.orientation.DirectionVector;
import org.anchoranalysis.image.orientation.Orientation;
import org.anchoranalysis.math.rotation.RotationMatrix;

public abstract class DirectionVectorBase extends FeatureMark {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// START BEAN PROPERTIES
	@BeanField
	private DirectionVectorBean directionVector;
	// END BEAN PROPERTIES
	
	private DirectionVector dv;
	
	@Override
	public void beforeCalc(FeatureInitParams params) throws InitException {
		super.beforeCalc(params);
		dv = directionVector.createVector();
	}
	
	@Override
	public double calc(FeatureMarkParams params) throws FeatureCalcException {

		if (!(params.getMark() instanceof MarkEllipsoid)) {
			throw new FeatureCalcException("Only supports MarkEllipsoids");
		}
		
		MarkEllipsoid mark = (MarkEllipsoid) params.getMark();
		
		Orientation orientation = mark.getOrientation();
		RotationMatrix rotMatrix = orientation.createRotationMatrix();
		return calcForEllipsoid(
			mark,
			orientation,
			rotMatrix, dv.createVector3d()
		);
		
	}
	
	protected abstract double calcForEllipsoid(MarkEllipsoid mark, Orientation orientation, RotationMatrix rotMatrix, Vector3d directionVector) throws FeatureCalcException;
	

	public DirectionVectorBean getDirectionVector() {
		return directionVector;
	}


	public void setDirectionVector(DirectionVectorBean directionVector) {
		this.directionVector = directionVector;
	}
}
