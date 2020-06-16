package org.anchoranalysisplugin.io.test.image;

import org.anchoranalysis.image.objectmask.ObjectMaskCollection;

class ObjMaskCollectionWithSize {
	private ObjectMaskCollection objs;
	private long size;
	
	public ObjMaskCollectionWithSize(ObjectMaskCollection objs, long size) {
		super();
		this.objs = objs;
		this.size = size;
	}
		
	public double relativeSize( ObjMaskCollectionWithSize other ) {
		return ((double) size) / other.size;
	}

	public ObjectMaskCollection getObjs() {
		return objs;
	}
}
