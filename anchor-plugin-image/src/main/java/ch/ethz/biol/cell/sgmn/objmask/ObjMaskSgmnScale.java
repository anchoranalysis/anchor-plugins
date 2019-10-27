package ch.ethz.biol.cell.sgmn.objmask;

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
import org.anchoranalysis.core.error.OptionalOperationUnsupportedException;
import org.anchoranalysis.image.bean.scale.ScaleCalculator;
import org.anchoranalysis.image.bean.sgmn.objmask.ObjMaskSgmn;
import org.anchoranalysis.image.chnl.Chnl;
import org.anchoranalysis.image.interpolator.Interpolator;
import org.anchoranalysis.image.interpolator.InterpolatorFactory;
import org.anchoranalysis.image.objmask.ObjMask;
import org.anchoranalysis.image.objmask.ObjMaskCollection;
import org.anchoranalysis.image.scale.ScaleFactor;
import org.anchoranalysis.image.seed.SeedCollection;
import org.anchoranalysis.image.sgmn.SgmnFailedException;


public class ObjMaskSgmnScale extends ObjMaskSgmn {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
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
	
	private Interpolator createInterpolator() {
		return interpolate ? InterpolatorFactory.getInstance().binaryResizing() : InterpolatorFactory.getInstance().noInterpolation();
	}

	@Override
	public ObjMaskCollection sgmn(Chnl chnl,
			SeedCollection seeds)
			throws SgmnFailedException {
		
		try {
			ScaleFactor sf = scaleCalculator.calc( chnl.getDimensions() );
			
			Chnl chnlScaled = chnl.scaleXY( sf.getX(), sf.getY(), createInterpolator() );
			
			// do watershed		
			ObjMaskCollection objs = sgmn.sgmn( chnlScaled.duplicate(), seeds );
			
			ScaleFactor sfInv = sf.invert();
			
			objs.scale( sfInv.getX(), sfInv.getY(), createInterpolator() );
			
			return objs;
		} catch (OperationFailedException e) {
			throw new SgmnFailedException(e);
		}
	}
	
	
	private SeedCollection scaleSeeds( SeedCollection seedsUnscaled, ScaleFactor sf ) throws OptionalOperationUnsupportedException, OperationFailedException {
		
		if (seedsUnscaled==null) {
			return null;
		}
		
		if (sf.getX()!=sf.getY()) {
			throw new OperationFailedException("scaleFactor in X and Y must be equal to scale seeds");
		}
		
		SeedCollection seedsScaled = seedsUnscaled.duplicate();
		seedsScaled.scaleXY( sf.getX() );
		return seedsScaled;
	}
	
	
	@Override
	public ObjMaskCollection sgmn(Chnl chnl, ObjMask objMask,
			SeedCollection seeds) throws SgmnFailedException {

		ScaleFactor sf;
		try {
			sf = scaleCalculator.calc( chnl.getDimensions() );
		} catch (OperationFailedException e) {
			throw new SgmnFailedException("Cannot calculate scale", e);
		}
		
		Chnl chnlScaled = chnl.scaleXY( sf.getX(), sf.getY(), createInterpolator());

		try {
			seeds = scaleSeeds(seeds, sf);
		} catch (OptionalOperationUnsupportedException | OperationFailedException e) {
			throw new SgmnFailedException("Cannot scale seeds", e);
		}
		
		// do watershed		
		ObjMaskCollection objs = sgmn.sgmn( chnlScaled.duplicate(), objMask, seeds );
		
		ScaleFactor sfInv = sf.invert();
		
		try {
			objs.scale( sfInv.getX(), sfInv.getY(), createInterpolator() );
		} catch (OperationFailedException e) {
			throw new SgmnFailedException("Cannot scale objects", e);
		}
		
		return objs;
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
