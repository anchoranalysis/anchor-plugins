package org.anchoranalysis.plugin.image.feature.bean.obj.pair.touching;

import org.anchoranalysis.image.extent.BoundingBox;
import org.anchoranalysis.image.objmask.ObjMask;

/**
 * Calculates objects/bounding boxes relative to others
 * 
 * @author owen
 *
 */
class RelativeUtilities {

	public static BoundingBox createRelBBox( BoundingBox bboxIntersect, ObjMask om1 ) {
		BoundingBox bboxIntersectRel = new BoundingBox( bboxIntersect.relPosTo(om1.getBoundingBox()), bboxIntersect.extnt() );
		bboxIntersectRel.clipTo( om1.getBoundingBox().extnt() );
		return bboxIntersectRel;
	}
	
	public static ObjMask createRelMask( ObjMask om1, ObjMask om2 ) {
		ObjMask om2Rel = om2.relMaskTo(om1.getBoundingBox());
		om2Rel.getBoundingBox().getCrnrMin().scale(-1);
		return om2Rel;
	}
}
