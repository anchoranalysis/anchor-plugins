package org.anchoranalysis.plugin.points.bean.fitter;

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
import java.util.Optional;

import org.anchoranalysis.anchor.mpp.bean.cfg.CfgProvider;
import org.anchoranalysis.anchor.mpp.bean.mark.factory.MarkFactory;
import org.anchoranalysis.anchor.mpp.cfg.Cfg;
import org.anchoranalysis.anchor.mpp.mark.Mark;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.geometry.Point2i;
import org.anchoranalysis.core.geometry.Point3f;
import org.anchoranalysis.core.geometry.PointConverter;
import org.anchoranalysis.image.extent.ImageDim;
import org.anchoranalysis.image.objectmask.ObjectMask;
import org.anchoranalysis.image.objectmask.ObjectCollection;

import ch.ethz.biol.cell.imageprocessing.binaryimgchnl.provider.ConvexHullUtilities;

public class CfgProviderPointsFitterFromObjs extends CfgProvider {

	/// START BEAN PROPERTIES
	@BeanField
	private PointsFitterToMark pointsFitter;
	
	@BeanField
	private MarkFactory markFactory;
	
	/** If true, Reduces the set of points by applying a convex-hull operation */
	@BeanField
	private boolean convexHull = true;
	// END BEAN PROPERTIES
	
	@Override
	public Cfg create() throws CreateException {
		
		ImageDim dim = pointsFitter.createDim();

		ObjectCollection objsCollection = pointsFitter.createObjs();
		
		Cfg cfgOut = new Cfg();
		
		for( ObjectMask om : objsCollection ) {
			Optional<Mark> mark = createMarkFromObj(om,dim);
			mark.ifPresent( m->
				cfgOut.add( m )
			);
		}

		return cfgOut;
	}
	
	private	Optional<Mark> createMarkFromObj( ObjectMask om, ImageDim dim ) throws CreateException {	
		
		Optional<List<Point2i>> pts = ConvexHullUtilities.extractPointsFromOutline(
			om,
			pointsFitter.getMinNumPnts(),
			convexHull
		);
		
		if (pts.isPresent()) {
			List<Point3f> ptsConverted = PointConverter.convert2i_3f(pts.get()); 
			return Optional.of( 
				fitToMark(ptsConverted, dim)
			);
		} else {
			return Optional.empty();
		}
	}
	
	private Mark fitToMark( List<Point3f> pntsToFit, ImageDim dim) throws CreateException {

		Mark markOut = markFactory.create();
		
		try {
			pointsFitter.fitPointsToMark( pntsToFit, markOut, dim );
		} catch (OperationFailedException e) {
			throw new CreateException(e);
		}
		
		return markOut;		
	}

	public MarkFactory getMarkFactory() {
		return markFactory;
	}

	public void setMarkFactory(MarkFactory markFactory) {
		this.markFactory = markFactory;
	}

	public boolean isConvexHull() {
		return convexHull;
	}

	public void setConvexHull(boolean convexHull) {
		this.convexHull = convexHull;
	}

	public PointsFitterToMark getPointsFitter() {
		return pointsFitter;
	}

	public void setPointsFitter(PointsFitterToMark pointsFitter) {
		this.pointsFitter = pointsFitter;
	}
}
