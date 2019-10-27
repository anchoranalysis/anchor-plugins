package ch.ethz.biol.cell.imageprocessing.objmask.provider;

/*-
 * #%L
 * anchor-plugin-points
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

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.geometry.PointConverter;
import org.anchoranalysis.image.bean.provider.ObjMaskProvider;
import org.anchoranalysis.image.contour.Contour;
import org.anchoranalysis.image.objmask.ObjMask;
import org.anchoranalysis.image.objmask.ObjMaskCollection;
import org.anchoranalysis.image.objmask.factory.CreateFromPointsFactory;

import ch.ethz.biol.cell.imageprocessing.objmask.provider.smoothspline.ContourList;
import ch.ethz.biol.cell.imageprocessing.objmask.provider.smoothspline.SplitContourSmoothingSpline;

/**
 * Splits a 2D contour represented by an obj-mask into several object-masks, by doing
 *  smoothing spline interpolation along the contour, and finding saddle points.
 *  
 * @author feehano
 *
 */
public class ObjMaskProviderSplitContourSmoothingSpline extends ObjMaskProvider {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// START BEAN PROPERTIES
	/** Each contour is represented by an object */
	@BeanField
	private ObjMaskProvider objMaskProvider;
	
	@BeanField
	private double smoothingFactor = 0.001;
	
	@BeanField
	private int numLoopPoints = 0;
	
	/** If a contour has less than this number of points, we don't split it, and return it as-is */
	@BeanField
	private int minNumPoints = 10;
	// END BEAN PROPERTIES
	
	@Override
	public ObjMaskCollection create() throws CreateException {

		ObjMaskCollection out = new ObjMaskCollection();
		
		ObjMaskCollection in = objMaskProvider.create();
		
		for( ObjMask om : in ) {
			splitContoursFromObj(om, out);
		}
		
		return out;
	}
	
	private void splitContoursFromObj( ObjMask om, ObjMaskCollection out ) throws CreateException {
		
		if (om.getBoundingBox().extnt().getZ()>1) {
			throw new CreateException("Only objs with z-slices > 1 are allowed");
		}
		
		try {
			ContourList cl = SplitContourSmoothingSpline.apply(om, smoothingFactor, numLoopPoints, minNumPoints);
			addContoursAsObjs( cl, out );
			
		} catch (OperationFailedException e) {
			throw new CreateException(e);
		}		
	}
	
	private void addContoursAsObjs( ContourList cl, ObjMaskCollection out ) {
		for( Contour c : cl) {
			out.add(
				createObjMaskFromContour(c, true)
			);
		}
	}

	public ObjMaskProvider getObjMaskProvider() {
		return objMaskProvider;
	}

	public void setObjMaskProvider(ObjMaskProvider objMaskProvider) {
		this.objMaskProvider = objMaskProvider;
	}

	public double getSmoothingFactor() {
		return smoothingFactor;
	}

	public void setSmoothingFactor(double smoothingFactor) {
		this.smoothingFactor = smoothingFactor;
	}

	public int getNumLoopPoints() {
		return numLoopPoints;
	}

	public void setNumLoopPoints(int numLoopPoints) {
		this.numLoopPoints = numLoopPoints;
	}

	public int getMinNumPoints() {
		return minNumPoints;
	}

	public void setMinNumPoints(int minNumPoints) {
		this.minNumPoints = minNumPoints;
	}
	
	private static ObjMask createObjMaskFromContour( Contour c, boolean round ) {
		return CreateFromPointsFactory.create(
			PointConverter.convert3i(c.getPoints(), round)
		);
	}

}
