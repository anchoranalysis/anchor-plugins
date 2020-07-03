package org.anchoranalysis.plugin.mpp.bean.proposer.points.fromorientation;

/*-
 * #%L
 * anchor-plugin-mpp
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

import java.awt.Color;
import java.util.List;
import java.util.Optional;

import org.anchoranalysis.anchor.mpp.cfg.ColoredCfg;
import org.anchoranalysis.anchor.mpp.mark.MarkLineSegment;
import org.anchoranalysis.anchor.mpp.mark.conic.MarkConicFactory;
import org.anchoranalysis.anchor.mpp.mark.points.MarkPointListFactory;
import org.anchoranalysis.core.geometry.Point3i;

class VisualizationUtilities {

	private VisualizationUtilities() {}
	
	public static void maybeAddLineSegment(ColoredCfg cfg, Optional<Point3i> point1, Optional<Point3i> point2, Color color) {
		if (point1.isPresent() && point2.isPresent()) {
			cfg.addChangeID(
				new MarkLineSegment(point1.get(), point2.get()),
				color
			);
		}
	}
	
	public static void maybeAddPoints(ColoredCfg cfg, List<Point3i> pointsList, Color color) {
		if (pointsList.size()>0) {
			cfg.addChangeID(
				MarkPointListFactory.createMarkFromPoints3i(pointsList),
				color
			);
		}	
	}
	
	public static void maybeAddConic(ColoredCfg cfg, Optional<Point3i> centerPoint, Color color, boolean do3D) {
		if (centerPoint.isPresent()) {
			cfg.addChangeID(
				MarkConicFactory.createMarkFromPoint(centerPoint.get(),1,do3D),
				color
			);
		}
	}
}
