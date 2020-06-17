package org.anchoranalysis.plugin.image.obj.merge;

import org.anchoranalysis.image.objectmask.ObjectMask;


/**
 * A vertex in a merge graph representing an object (and and an associated payload)
 * 
 * @author Owen Feehan
 *
 */
public class ObjVertex {
	
	private ObjectMask om;
	private double payload;
	private int numPixels = -1;	// We calculate it when we need to
	
	public ObjVertex(ObjectMask om, double featureVal) {
		super();
		this.om = om;
		this.payload = featureVal;
	}
	
	public ObjectMask getObjMask() {
		return om;
	}
	public double getPayload() {
		return payload;
	}

	public int numPixels() {
		if (numPixels==-1) {
			numPixels = om.numVoxelsOn();
		}
		return numPixels;
	}

	@Override
	public String toString() {
		return om.toString();
	}


}
