package org.anchoranalysis.plugin.points.calculate.ellipse;

import org.anchoranalysis.anchor.mpp.mark.conic.MarkEllipse;
import org.anchoranalysis.image.objectmask.ObjectMask;

public class ObjMaskAndEllipse {

	private ObjectMask objMask;
	private MarkEllipse mark;
		
	public ObjMaskAndEllipse(ObjectMask objMask, MarkEllipse mark) {
		super();
		this.objMask = objMask;
		this.mark = mark;
	}
	
	public ObjectMask getObjMask() {
		return objMask;
	}
	public void setObjMask(ObjectMask objMask) {
		this.objMask = objMask;
	}
	public MarkEllipse getMark() {
		return mark;
	}
	public void setMark(MarkEllipse mark) {
		this.mark = mark;
	}
}
