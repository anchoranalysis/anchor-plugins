package org.anchoranalysis.test.feature.plugins;

import org.anchoranalysis.anchor.mpp.mark.GlobalRegionIdentifiers;
import org.anchoranalysis.anchor.mpp.mark.conic.MarkCircle;
import org.anchoranalysis.anchor.mpp.regionmap.RegionMapSingleton;
import org.anchoranalysis.core.geometry.Point2i;
import org.anchoranalysis.core.geometry.PointConverter;
import org.anchoranalysis.feature.nrg.NRGStackWithParams;
import org.anchoranalysis.image.binary.values.BinaryValuesByte;
import org.anchoranalysis.image.extent.ImageDim;
import org.anchoranalysis.image.objmask.ObjMask;

public class CircleObjMaskFixture {

	private static ImageDim DIMS = new ImageDim(800, 600, 1);
	
	public static ObjMask circleAt( Point2i center, double radius ) {
		MarkCircle mark = new MarkCircle();
		mark.setPos( PointConverter.doubleFromInt(center) );
		mark.setRadius(radius);
		return mark.calcMask(
			DIMS,
			RegionMapSingleton.instance().membershipWithFlagsForIndex(GlobalRegionIdentifiers.SUBMARK_INSIDE),
			BinaryValuesByte.getDefault()
		).getMask();
	}
	
	public static NRGStackWithParams nrgStack() {
		return new NRGStackWithParams(DIMS);
	}

	public static boolean sceneContains(Point2i pnt) {
		return DIMS.contains(
			PointConverter.convertTo3d(pnt)
		);
	}
}
