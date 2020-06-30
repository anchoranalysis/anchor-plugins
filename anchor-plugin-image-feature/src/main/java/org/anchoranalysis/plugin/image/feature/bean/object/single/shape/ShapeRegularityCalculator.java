package org.anchoranalysis.plugin.image.feature.bean.object.single.shape;

import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.plugin.image.feature.bean.object.single.border.NumberVoxelsAtBorder;

class ShapeRegularityCalculator {

	private ShapeRegularityCalculator() {}
	
	public static double calcShapeRegularity( ObjectMask om ) {
		double area = om.numVoxelsOn();
		int perimeter = NumberVoxelsAtBorder.numBorderPixels(
			om,
			false,
			false,
			false
		);
		return calcValues(area, perimeter);
	}
	
	private static double calcValues(double area, int perimeter) {

		if (perimeter==0) {
			return 0.0;
		}
		
		double val = ((2 * Math.PI) * Math.sqrt(area/Math.PI)) / perimeter;
		assert( !Double.isNaN(val) );
		return val;
	}
}
