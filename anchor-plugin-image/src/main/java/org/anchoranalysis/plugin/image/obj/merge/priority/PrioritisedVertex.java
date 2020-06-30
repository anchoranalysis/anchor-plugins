package org.anchoranalysis.plugin.image.obj.merge.priority;

import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.plugin.image.obj.merge.ObjVertex;


/**
 * A vertex with a priority attached, and a boolean flag as to whether it can be merged or not
 * 
 * <p>The higher the priority value, the greater the priority.</p>
 * 
 * @author Owen Feehan
 *
 */
public class PrioritisedVertex {
	
	private ObjVertex vertex;
	private double priority;
	private boolean considerForMerge;
	
	/**
	 * Constructor
	 * 
	 * @param obj object to form the vertex
	 * @param payload associated payload with the object in the vertex
	 * @param priority a priority to determine the order of merges (higher value implies greater priority)
	 * @param considerForMerge iff FALSE, these two objects object may not be merged, and priority is irrelevant.
	 */
	public PrioritisedVertex( ObjectMask obj, double payload, double priority, boolean considerForMerge ) {
		this.vertex = new ObjVertex(obj, payload);
		this.priority = priority;
		this.considerForMerge = considerForMerge;
	}
			
	public ObjVertex getOmWithFeature() {
		return vertex;
	}

	public double getPriority() {
		return priority;
	}

	public boolean isConsiderForMerge() {
		return considerForMerge;
	}
}
