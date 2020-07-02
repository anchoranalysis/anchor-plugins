package org.anchoranalysis.plugin.mpp.bean.proposer.points.fromorientation;

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
