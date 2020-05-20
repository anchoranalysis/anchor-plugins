package ch.ethz.biol.cell.sgmn.objmask;

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


import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.functional.UnaryOperatorWithException;
import org.anchoranalysis.image.bean.scale.ScaleCalculator;
import org.anchoranalysis.image.bean.sgmn.objmask.ObjMaskSgmn;
import org.anchoranalysis.image.chnl.Chnl;
import org.anchoranalysis.image.extent.ImageDim;
import org.anchoranalysis.image.interpolator.Interpolator;
import org.anchoranalysis.image.interpolator.InterpolatorFactory;
import org.anchoranalysis.image.objmask.ObjMask;
import org.anchoranalysis.image.objmask.ObjMaskCollection;
import org.anchoranalysis.image.scale.ScaleFactor;
import org.anchoranalysis.image.seed.SeedCollection;
import org.anchoranalysis.image.sgmn.SgmnFailedException;


public class ObjMaskSgmnScale extends ObjMaskSgmn {

	// START BEAN PROPERTIES
	@BeanField
	private ObjMaskSgmn sgmn;
	
	@BeanField
	private ScaleCalculator scaleCalculator;
	
	@BeanField
	private int outlineWidth = 1;
	
	@BeanField
	private boolean interpolate = true;
	// END BEAN PROPERTIES
	
	@Override
	public ObjMaskCollection sgmn(Chnl chnl, Optional<ObjMask> mask,	Optional<SeedCollection> seeds) throws SgmnFailedException {

		Interpolator interpolator = createInterpolator();
		
		ScaleFactor sf = determineScaleFactor(chnl.getDimensions());
		
		// Scale input channel
		Chnl chnlScaled = chnl.scaleXY( sf.getX(), sf.getY(), interpolator);
		
		// Scale seeds
		seeds = mapScale(
			seeds,
			s -> scaleSeeds(s, sf),
			"seeds"
		);
		
		// Scale mask
		mask = mapScale(
			mask,
			om -> om.scaleNew(sf, interpolator),
			"mask"
		);

		// Segment and scale results back up to original-scale
		return scaleResultToOriginalScale(
			sgmn.sgmn( chnlScaled.duplicate(), mask, seeds ),
			sf
		);
	}
	
	private ScaleFactor determineScaleFactor( ImageDim dim ) throws SgmnFailedException {
		try {
			return scaleCalculator.calc(dim);
		} catch (OperationFailedException e) {
			throw new SgmnFailedException("Cannot calculate scale", e);
		}
	}
	
	private ObjMaskCollection scaleResultToOriginalScale(ObjMaskCollection objs, ScaleFactor sf) throws SgmnFailedException {
		try {
			objs.scale( sf.invert(), createInterpolator() );
		} catch (OperationFailedException e) {
			throw new SgmnFailedException("Cannot scale objects", e);
		}
		return objs;
	}
	
	/**
	 * Scales an {@link Optional} if its present
	 * 
	 * @param <T> optional-type
	 * @param optional the optional to be scaled
	 * @param scaleFunc function to use for scaling
	 * @param textualDscrInError how to describe the optional in an error message
	 * @return an optional with either a scaled value or empty() depending on the input-option
	 * @throws SgmnFailedException if the scaling operation fails
	 */
	private static <T> Optional<T> mapScale(
		Optional<T> optional,
		UnaryOperatorWithException<T,OperationFailedException> scaleFunc,
		String textualDscrInError
	) throws SgmnFailedException {
		try {
			if (optional.isPresent()) {
				return Optional.of(
					scaleFunc.apply(optional.get())
				);
			} else {
				return Optional.empty();
			}
		} catch (OperationFailedException e) {
			throw new SgmnFailedException("Cannot scale " + textualDscrInError);
		}
	}

	private static SeedCollection scaleSeeds( SeedCollection seedsUnscaled, ScaleFactor sf ) throws OperationFailedException {
		
		if (sf.getX()!=sf.getY()) {
			throw new OperationFailedException("scaleFactor in X and Y must be equal to scale seeds");
		}
		
		SeedCollection seedsScaled = seedsUnscaled.duplicate();
		seedsScaled.scaleXY( sf.getX() );
		return seedsScaled;
	}
	
	private Interpolator createInterpolator() {
		return interpolate ? InterpolatorFactory.getInstance().binaryResizing() : InterpolatorFactory.getInstance().noInterpolation();
	}

	public ObjMaskSgmn getSgmn() {
		return sgmn;
	}

	public void setSgmn(ObjMaskSgmn sgmn) {
		this.sgmn = sgmn;
	}

	public ScaleCalculator getScaleCalculator() {
		return scaleCalculator;
	}

	public void setScaleCalculator(ScaleCalculator scaleCalculator) {
		this.scaleCalculator = scaleCalculator;
	}

	public int getOutlineWidth() {
		return outlineWidth;
	}

	public void setOutlineWidth(int outlineWidth) {
		this.outlineWidth = outlineWidth;
	}

	public boolean isInterpolate() {
		return interpolate;
	}

	public void setInterpolate(boolean interpolate) {
		this.interpolate = interpolate;
	}


}
