package org.anchoranalysis.plugin.annotation.comparison;

import org.anchoranalysis.image.objectmask.ObjectMaskCollection;

public class ObjsToCompare {

	private ObjectMaskCollection left;
	private ObjectMaskCollection right;
	
	public ObjsToCompare(ObjectMaskCollection left, ObjectMaskCollection right) {
		super();
		this.left = left;
		this.right = right;
	}

	public ObjectMaskCollection getLeft() {
		return left;
	}

	public ObjectMaskCollection getRight() {
		return right;
	}		
}
