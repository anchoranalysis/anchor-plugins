package org.anchoranalysisplugin.io.test.image;

import org.anchoranalysis.image.objectmask.ObjectCollection;

class ObjMaskCollectionWithSize {
	private ObjectCollection objs;
	private long size;
	
	public ObjMaskCollectionWithSize(ObjectCollection objs, long size) {
		super();
		this.objs = objs;
		this.size = size;
	}
		
	public double relativeSize( ObjMaskCollectionWithSize other ) {
		return ((double) size) / other.size;
	}

	public ObjectCollection getObjs() {
		return objs;
	}
}
