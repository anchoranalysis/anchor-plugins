package org.anchoranalysis.plugin.annotation.comparison;

import org.anchoranalysis.image.object.ObjectCollection;

public class ObjsToCompare {

	private ObjectCollection left;
	private ObjectCollection right;
	
	public ObjsToCompare(ObjectCollection left, ObjectCollection right) {
		super();
		this.left = left;
		this.right = right;
	}

	public ObjectCollection getLeft() {
		return left;
	}

	public ObjectCollection getRight() {
		return right;
	}		
}
