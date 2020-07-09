package org.anchoranalysis.plugin.image.feature.object.calculation.single;

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


import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.feature.cache.calculation.CalculationResolver;
import org.anchoranalysis.feature.cache.calculation.FeatureCalculation;
import org.anchoranalysis.feature.cache.calculation.ResolvedCalculation;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.extent.ImageDimensions;
import org.anchoranalysis.image.feature.object.input.FeatureInputSingleObject;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.image.object.morph.MorphologicalErosion;
import org.anchoranalysis.plugin.image.feature.object.calculation.single.morphological.CalculateDilation;
import org.anchoranalysis.plugin.image.feature.object.calculation.single.morphological.CalculateErosion;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor @EqualsAndHashCode(callSuper=false)
public class CalculateShellObjectMask extends FeatureCalculation<ObjectMask,FeatureInputSingleObject> {

	private final ResolvedCalculation<ObjectMask,FeatureInputSingleObject> ccDilation;
	private final ResolvedCalculation<ObjectMask,FeatureInputSingleObject> ccErosion;
	private final int iterationsErosionSecond;
	private final boolean do3D;
	private final boolean inverse;
	
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
	
	
	public static FeatureCalculation<ObjectMask,FeatureInputSingleObject> createFromCache(
		CalculationResolver<FeatureInputSingleObject> params,
		int iterationsDilation,
		int iterationsErosion,
		int iterationsErosionSecond,
		boolean do3D,
		boolean inverse
	) {
		ResolvedCalculation<ObjectMask,FeatureInputSingleObject> ccDilation = CalculateDilation.createFromCache(
			params, iterationsDilation, do3D	
		);
		ResolvedCalculation<ObjectMask,FeatureInputSingleObject> ccErosion = CalculateErosion.createFromCacheRslvd(
			params, iterationsErosion, do3D
		);
	
		return new CalculateShellObjectMask(
			ccDilation,
			ccErosion,
			iterationsErosionSecond,
			do3D,
			inverse
		);	
	}

	@Override
	protected ObjectMask execute( FeatureInputSingleObject input ) throws FeatureCalcException {
	
		ImageDimensions sd = input.getDimensionsRequired();
		
		ObjectMask om = input.getObjectMask();
		ObjectMask omShell = createShellObjMask( input, om, ccDilation, ccErosion, iterationsErosionSecond, do3D );
		
		if (inverse) {
			ObjectMask omDup = om.duplicate();
			
			Optional<ObjectMask> omShellIntersected = omShell.intersect( omDup, sd );
			omShellIntersected.ifPresent( shellIntersected ->
				omDup.binaryVoxelBox().setPixelsCheckMaskOff(
					shellIntersected.relMaskTo(omDup.getBoundingBox())
				)
			);
			return omDup;
			
		} else {
			return omShell;
		}
	}
	
	private static ObjectMask createShellObjMask(
		FeatureInputSingleObject input,
		ObjectMask om,
		ResolvedCalculation<ObjectMask,FeatureInputSingleObject> ccDilation,
		ResolvedCalculation<ObjectMask,FeatureInputSingleObject> ccErosion,
		int iterationsErosionSecond,
		boolean do3D
	) throws FeatureCalcException {

		ObjectMask omDilated = ccDilation.getOrCalculate(input).duplicate();
		ObjectMask omEroded = ccErosion.getOrCalculate(input);
		
		// Maybe apply a second erosion
		try {
			omDilated = iterationsErosionSecond>0 ? MorphologicalErosion.createErodedObjMask(
				omDilated,
				null,
				do3D,
				iterationsErosionSecond,
				true,
				null
			) : omDilated;
		} catch (CreateException e) {
			throw new FeatureCalcException(e);
		}
		
		ObjectMask relMask = omEroded.relMaskTo( omDilated.getBoundingBox() );
		
		omDilated.binaryVoxelBox().setPixelsCheckMaskOff( relMask );
	
		return omDilated;
	}
}