package ch.ethz.biol.cell.mpp.mark.ellipsoidfitter.outlinepixelsretriever.visitscheduler;

import java.util.Optional;

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
import org.anchoranalysis.core.axis.AxisType;
import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.error.friendly.AnchorFriendlyRuntimeException;
import org.anchoranalysis.core.geometry.Point3i;
import org.anchoranalysis.core.geometry.Tuple3i;
import org.anchoranalysis.core.random.RandomNumberGenerator;
import org.anchoranalysis.image.bean.unitvalue.distance.UnitValueDistance;
import org.anchoranalysis.image.extent.ImageResolution;
import org.anchoranalysis.image.object.ObjectMask;

// Breadth-first iteration of pixels
public class VisitSchedulerMaxDistUnitValue extends VisitScheduler {

	// START BEAN PROPERTIES
	@BeanField
	private UnitValueDistance maxDist;
	// END BEAN PROPERTIES
	
	private Point3i root;
	
	private ImageResolution res;
	
	
	
	public VisitSchedulerMaxDistUnitValue() {
		super();
	}

	
	@Override
	public void beforeCreateObjMask(RandomNumberGenerator re, ImageResolution res)
			throws InitException {
			
	}
	
	@Override
	public Tuple3i maxDistFromRootPoint(ImageResolution res) throws OperationFailedException {
		return new Point3i(
			distForAxis(AxisType.X, res),
			distForAxis(AxisType.Y, res),
			distForAxis(AxisType.Z, res)
		);
	}
	
	private int distForAxis(AxisType axis, ImageResolution res) throws OperationFailedException {
		return (int) Math.ceil(
			maxDist.rslvForAxis(Optional.of(res), axis)
		);
	}
	
	@Override
	public void afterCreateObjMask(Point3i root, ImageResolution res, RandomNumberGenerator re) {
		this.res = res;
		this.root = root;
	}

	@Override
	public boolean considerVisit( Point3i pnt, int distAlongContour, ObjectMask objMask ) {
		
		try {
			if (distToRoot(pnt)>=maxDist.rslv(
				Optional.of(res),
				root,
				pnt
			)) {
				return false;
			}
			
			return true;
		} catch (OperationFailedException e) {
			throw new AnchorFriendlyRuntimeException(e);
		}
	}
	
	private double distToRoot( Point3i pnt ) {
		 return res.distance(root, pnt);
	}

	public UnitValueDistance getMaxDist() {
		return maxDist;
	}

	public void setMaxDist(UnitValueDistance maxDist) {
		this.maxDist = maxDist;
	}

}
