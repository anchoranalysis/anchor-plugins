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
import org.anchoranalysis.core.geometry.Point3i;
import org.anchoranalysis.core.geometry.Tuple3i;
import org.anchoranalysis.core.random.RandomNumberGenerator;
import org.anchoranalysis.image.bean.unitvalue.distance.UnitValueDistance;
import org.anchoranalysis.image.extent.ImageRes;
import org.anchoranalysis.image.objectmask.ObjectMask;

// Breadth-first iteration of pixels
public class VisitSchedulerMaxDistSep extends VisitScheduler {

	// START BEAN PROPERTIES
	@BeanField
	private UnitValueDistance maxDistX;
	
	@BeanField
	private UnitValueDistance maxDistY;
	
	@BeanField
	private UnitValueDistance maxDistZ;
	// END BEAN PROPERTIES
	
	private Point3i root;
	private double maxXRslv;
	private double maxYRslv;
	private double maxZRslv;
	
	public VisitSchedulerMaxDistSep() {
		super();
	}
		
	@Override
	public void beforeCreateObjMask(RandomNumberGenerator re, ImageRes res)
			throws InitException {
				
	}
	
	@Override
	public Tuple3i maxDistFromRootPoint(ImageRes res) {
		int distX = (int) Math.ceil(maxXRslv);
		int distY = (int) Math.ceil(maxYRslv);
		int distZ = (int) Math.ceil(maxZRslv);
		return new Point3i(distX,distY,distZ);
	}
	
	@Override
	public void afterCreateObjMask(Point3i root, ImageRes res, RandomNumberGenerator re) throws InitException {
		
		try {
			Optional<ImageRes> resOpt = Optional.of(res);
			
			maxXRslv = maxDistX.rslvForAxis(resOpt, AxisType.X);
			maxYRslv = maxDistY.rslvForAxis(resOpt, AxisType.Y);
			maxZRslv = maxDistZ.rslvForAxis(resOpt, AxisType.Z);
		
			this.root = root;
			
		} catch (OperationFailedException e) {
			throw new InitException(e);
		}
	}

	@Override
	public boolean considerVisit( Point3i pnt, int distAlongContour, ObjectMask objMask ) {

		if (Math.abs(root.getX()-pnt.getX())>maxXRslv) {
			return false;
		}
		if (Math.abs(root.getY()-pnt.getY())>maxYRslv) {
			return false;
		}
		if (Math.abs(root.getZ()-pnt.getZ())>maxZRslv) {
			return false;
		}
		
		return true;
	
	}

	public UnitValueDistance getMaxDistX() {
		return maxDistX;
	}

	public void setMaxDistX(UnitValueDistance maxDistX) {
		this.maxDistX = maxDistX;
	}

	public UnitValueDistance getMaxDistY() {
		return maxDistY;
	}

	public void setMaxDistY(UnitValueDistance maxDistY) {
		this.maxDistY = maxDistY;
	}

	public UnitValueDistance getMaxDistZ() {
		return maxDistZ;
	}

	public void setMaxDistZ(UnitValueDistance maxDistZ) {
		this.maxDistZ = maxDistZ;
	}



}
