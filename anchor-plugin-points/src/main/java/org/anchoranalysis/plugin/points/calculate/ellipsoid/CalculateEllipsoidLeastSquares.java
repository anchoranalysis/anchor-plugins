package org.anchoranalysis.plugin.points.calculate.ellipsoid;

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


import java.util.List;

import org.anchoranalysis.anchor.mpp.mark.conic.MarkEllipsoid;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.geometry.Point3i;
import org.anchoranalysis.feature.cache.SessionInput;
import org.anchoranalysis.feature.cache.calculation.FeatureCalculation;
import org.anchoranalysis.feature.cache.calculation.ResolvedCalculation;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.feature.object.input.FeatureInputSingleObject;
import org.anchoranalysis.plugin.points.calculate.CalculatePntsFromOutline;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;

@AllArgsConstructor(access = AccessLevel.PRIVATE) @EqualsAndHashCode(callSuper=false)
public class CalculateEllipsoidLeastSquares extends FeatureCalculation<MarkEllipsoid,FeatureInputSingleObject> {

	private final boolean suppressZCovariance;
	private final ResolvedCalculation<List<Point3i>,FeatureInputSingleObject> ccPnts;

	public static MarkEllipsoid of(SessionInput<FeatureInputSingleObject> input, boolean suppressZCovariance ) throws FeatureCalcException {
		
		ResolvedCalculation<List<Point3i>,FeatureInputSingleObject> ccPnts = input.resolver().search(
			new CalculatePntsFromOutline()
		);
		
		ResolvedCalculation<MarkEllipsoid,FeatureInputSingleObject> ccEllipsoid = input
			.resolver()
			.search(
				new CalculateEllipsoidLeastSquares(suppressZCovariance, ccPnts )
			);
		return input.calc(ccEllipsoid);
	}
	
	@Override
	protected MarkEllipsoid execute( FeatureInputSingleObject input ) throws FeatureCalcException {
		
		try {
			// Shell Rad is arbitrary here for now
			return EllipsoidFactory.createMarkEllipsoidLeastSquares(
				new CachedCalculationOperation<>(ccPnts,input),
				input.getDimensionsRequired(),
				suppressZCovariance,
				0.2
			);
		} catch (CreateException e) {
			throw new FeatureCalcException(e);
		}
	}
}
