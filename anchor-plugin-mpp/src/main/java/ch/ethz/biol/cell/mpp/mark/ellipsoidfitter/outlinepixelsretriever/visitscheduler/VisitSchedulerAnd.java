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
import java.util.Optional;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.geometry.Point3i;
import org.anchoranalysis.core.geometry.Tuple3i;
import org.anchoranalysis.core.random.RandomNumberGenerator;
import org.anchoranalysis.image.extent.ImageResolution;
import org.anchoranalysis.image.object.ObjectMask;

public class VisitSchedulerAnd extends VisitScheduler {

	// START BEAN PROPERTIES
	@BeanField
	private List<VisitScheduler> list = new ArrayList<>();
	// END BEAN PROPERTIES
	
	@Override
	public Optional<Tuple3i> maxDistanceFromRootPoint(ImageResolution res) throws OperationFailedException {
		
		Optional<Tuple3i> maxDistance = Optional.empty();
		
		for( VisitScheduler vs : list ) {
			
			Optional<Tuple3i> distance = vs.maxDistanceFromRootPoint(res);
			
			// Skip if it doesn't return a max-distance
			if (!distance.isPresent()) {
				continue;
			}
			
			if (!maxDistance.isPresent()) {
				maxDistance = Optional.of(
					new Point3i(distance.get())
				);
			} else {
				maxDistance = Optional.of(
					maxDistance.get().min(distance.get())
				);
			}
		}
		
		return maxDistance;
	}

	@Override
	public void beforeCreateObject(RandomNumberGenerator re, ImageResolution res)
			throws InitException {
		
		for( VisitScheduler vs : list ) {
			vs.beforeCreateObject(re, res);
		}
		
	}

	@Override
	public void afterCreateObject(Point3i root, ImageResolution res, RandomNumberGenerator re) throws InitException {
		
		for( VisitScheduler vs : list ) {
			vs.afterCreateObject(root, res, re);
		}
		
	}

	@Override
	public boolean considerVisit(Point3i point, int distanceAlongContour, ObjectMask object) {
		for( VisitScheduler vs : list ) {
			if (!vs.considerVisit(point, distanceAlongContour, object)) {
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
