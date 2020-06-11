package org.anchoranalysis.plugin.points.bean.fitter;

/*-
 * #%L
 * anchor-plugin-points
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

import java.util.List;

import org.anchoranalysis.anchor.mpp.bean.points.PointsBean;
import org.anchoranalysis.anchor.mpp.bean.points.fitter.InsufficientPointsException;
import org.anchoranalysis.anchor.mpp.bean.points.fitter.PointsFitter;
import org.anchoranalysis.anchor.mpp.bean.points.fitter.PointsFitterException;
import org.anchoranalysis.anchor.mpp.mark.Mark;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.geometry.Point3f;
import org.anchoranalysis.image.bean.provider.ImageDimProvider;
import org.anchoranalysis.image.bean.provider.ObjMaskProvider;
import org.anchoranalysis.image.extent.ImageDim;
import org.anchoranalysis.image.objmask.ObjMaskCollection;

public class PointsFitterToMark extends PointsBean<PointsFitterToMark> {

	// START BEAN PROPERTIES
	@BeanField
	private PointsFitter pointsFitter;
	
	@BeanField
	private ImageDimProvider dim;
	
	/** If an object has fewer points than before being fitted, we ignore */
	@BeanField
	private int minNumPnts = 0;
	
	@BeanField
	private ObjMaskProvider objs;
	// END BEAN PROPERTIES
	
	public void fitPointsToMark( List<Point3f> pntsForFitter, Mark mark, ImageDim dim) throws OperationFailedException {
		try {
			pointsFitter.fit( pntsForFitter, mark, dim );
		} catch (PointsFitterException | InsufficientPointsException e) {
			throw new OperationFailedException(e);
		}
	}
	
	public ObjMaskCollection createObjs() throws CreateException {
		return objs.create();
	}
	
	public ImageDim createDim() throws CreateException {
		return dim.create();
	}
		
	public ObjMaskProvider getObjs() {
		return objs;
	}

	public void setObjs(ObjMaskProvider objs) {
		this.objs = objs;
	}

	public int getMinNumPnts() {
		return minNumPnts;
	}

	public void setMinNumPnts(int minNumPnts) {
		this.minNumPnts = minNumPnts;
	}
	
	public PointsFitter getPointsFitter() {
		return pointsFitter;
	}

	public void setPointsFitter(PointsFitter pointsFitter) {
		this.pointsFitter = pointsFitter;
	}

	public ImageDimProvider getDim() {
		return dim;
	}

	public void setDim(ImageDimProvider dim) {
		this.dim = dim;
	}
}
