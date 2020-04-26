package ch.ethz.biol.cell.mpp.nrg.feature.objmask;

import org.anchoranalysis.image.objmask.ObjMask;

class ShapeRegularityCalculator {

	private ShapeRegularityCalculator() {}
	
	public static double calcShapeRegularity( ObjMask om ) {
		double area = om.numPixels();
		int perimeter = NumBorderVoxels.numBorderPixels(
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
