package ch.ethz.biol.cell.mpp.mark.ellipsoidfitter.outlinepixelsretriever.visitscheduler;

/*
 * #%L
 * anchor-plugin-mpp
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


import java.util.ArrayList;
import java.util.List;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.core.geometry.Point3i;
import org.anchoranalysis.core.geometry.Tuple3i;
import org.anchoranalysis.core.random.RandomNumberGenerator;
import org.anchoranalysis.image.extent.ImageRes;
import org.anchoranalysis.image.objmask.ObjMask;

public class VisitSchedulerAnd extends VisitScheduler {

	// START BEAN PROPERTIES
	@BeanField
	private List<VisitScheduler> list = new ArrayList<>();
	// END BEAN PROPERTIES
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public Tuple3i maxDistFromRootPoint(ImageRes res) {
		
		Point3i maxDist = null;
		
		for( VisitScheduler vs : list ) {
			
			Tuple3i d = vs.maxDistFromRootPoint(res);
			
			// Skip if it doesn't return a maxDist
			if (d==null) {
				continue;
			}
			
			if (maxDist==null) {
				maxDist = new Point3i(d);
			} else {
				maxDist.setX( Math.min( maxDist.getX(), d.getX() ));
				maxDist.setY( Math.min( maxDist.getY(), d.getY() ));
				maxDist.setZ( Math.min( maxDist.getZ(), d.getZ() ));
			}
		}
		
		return maxDist;
	}

	@Override
	public void beforeCreateObjMask(RandomNumberGenerator re, ImageRes res)
			throws InitException {
		
		for( VisitScheduler vs : list ) {
			vs.beforeCreateObjMask(re, res);
		}
		
	}

	@Override
	public void afterCreateObjMask(Point3i root, ImageRes res, RandomNumberGenerator re) throws InitException {
		
		for( VisitScheduler vs : list ) {
			vs.afterCreateObjMask(root, res, re);
		}
		
	}

	@Override
	public boolean considerVisit(Point3i pnt, int distAlongContour, ObjMask objMask) {
		for( VisitScheduler vs : list ) {
			if (!vs.considerVisit(pnt, distAlongContour, objMask)) {
				return false;
			}
		}
		return true;
	}
	
	public List<VisitScheduler> getList() {
		return list;
	}

	public void setList(List<VisitScheduler> list) {
		this.list = list;
	}



}
