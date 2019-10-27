package ch.ethz.biol.cell.mpp.mark.ellipsoidfitter.outlinepixelsretriever.visitscheduler;

/*-
 * #%L
 * anchor-plugin-mpp
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
import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.core.geometry.Point3d;
import org.anchoranalysis.core.geometry.Point3i;
import org.anchoranalysis.core.geometry.PointConverter;
import org.anchoranalysis.core.geometry.Tuple3i;
import org.anchoranalysis.core.random.RandomNumberGenerator;
import org.anchoranalysis.image.extent.BoundingBox;
import org.anchoranalysis.image.extent.ImageRes;
import org.anchoranalysis.image.objmask.ObjMask;

// Breadth-first iteration of pixels
public class VisitSchedulerMaxDist extends VisitScheduler {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// START BEAN PROPERTIES
	@BeanField
	private double maxDist;
	// END BEAN PROPERTIES
	
	private Point3i root;
	
	private ImageRes res;
	
	private double maxDistSq;
	
	public VisitSchedulerMaxDist() {
		super();
	}
	

	@Override
	public void beforeCreateObjMask(RandomNumberGenerator re, ImageRes res)
			throws InitException {
				
	}
	
	@Override
	public Tuple3i maxDistFromRootPoint(ImageRes res) {
		int maxDist = (int) Math.ceil(this.maxDist);
		return new Point3i(maxDist,maxDist,maxDist);
	}
	
	@Override
	public void afterCreateObjMask(Point3i root, ImageRes res, RandomNumberGenerator re) {
		this.root = root;
		this.res = res;
		this.maxDistSq = Math.pow(maxDist,2);
	}

	@Override
	public boolean considerVisit( Point3i pnt, int distAlongContour, ObjMask objMask ) {
		
		if (distToRoot(pnt)>=maxDistSq) {
			return false;
		}

		return true;
	}
	
	private double distToRoot( Point3i pnt ) {
		 return res.distSqZRel(root, pnt);
	}
	
	public static BoundingBox createBoxAroundPoint( Point3i pnt, Tuple3i width ) {
		
		Point3d widthD = new Point3d( width );
		
		// We create a raster around the point, maxDist*2 in both directions, so long as it doesn't escape the region
		Point3d min = PointConverter.doubleFromInt(pnt);
		min.sub(widthD);
		
		Point3d max = PointConverter.doubleFromInt(pnt);
		max.add(widthD);
		
		return new BoundingBox(min, max);
	}
	
	public double getMaxDist() {
		return maxDist;
	}

	public void setMaxDist(double maxDist) {
		this.maxDist = maxDist;
	}
}
