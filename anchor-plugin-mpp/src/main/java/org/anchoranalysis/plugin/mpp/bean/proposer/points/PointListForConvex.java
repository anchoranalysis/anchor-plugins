package org.anchoranalysis.plugin.mpp.bean.proposer.points;

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

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.anchoranalysis.core.geometry.Point3i;
import org.anchoranalysis.image.binary.voxel.BinaryVoxelBox;

import ch.ethz.biol.cell.mpp.mark.ellipsoidfitter.outlinepixelsretriever.visitscheduler.VisitSchedulerConvexAboutRoot;

class PointListForConvex {
	private List<Point3i> list = new ArrayList<Point3i>();

	public boolean add(Point3i e) {
		return list.add(e);
	}
	
	public boolean convexWithAtLeastOnePoint( Point3i root, Point3i pnt, BinaryVoxelBox<ByteBuffer> vb  ) {
		return VisitSchedulerConvexAboutRoot.isPointConvexTo(root, pnt, vb);
	}
	
	public boolean convexWithAtLeastOnePoint( Point3i pnt, BinaryVoxelBox<ByteBuffer> vb  ) {
		
		for( Point3i p : list ) {
			if (convexWithAtLeastOnePoint(p, pnt, vb)) {
				return true;
			}
		}
		return false;
	}
	
}