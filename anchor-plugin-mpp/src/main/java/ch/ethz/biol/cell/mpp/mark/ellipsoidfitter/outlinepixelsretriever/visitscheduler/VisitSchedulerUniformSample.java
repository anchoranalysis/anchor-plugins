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

import lombok.Getter;
import lombok.Setter;


public class VisitSchedulerUniformSample extends VisitScheduler {

	// START BEAN PROPERTIES
	@BeanField @Getter @Setter
	private List<VisitScheduler> list = new ArrayList<>();
	// END BEAN PROPERTIES

	private VisitScheduler selected;
	
	@Override
	public Optional<Tuple3i> maxDistanceFromRootPoint(ImageResolution res) throws OperationFailedException {
		return selected.maxDistanceFromRootPoint(res);
	}
	
	@Override
	public void beforeCreateObject(RandomNumberGenerator re, ImageResolution res)
			throws InitException {
		int rand = (int) (re.nextDouble() * list.size());
		selected = list.get(rand);
		selected.beforeCreateObject(re, res);
	}
	
	@Override
	public void afterCreateObject(Point3i root, ImageResolution res, RandomNumberGenerator re) throws InitException {
		selected.afterCreateObject(root, res, re);
	}

	@Override
	public boolean considerVisit(Point3i point, int distanceAlongContour, ObjectMask object) {
		return selected.considerVisit(point, distanceAlongContour, object);
	}
}
