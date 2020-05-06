package org.anchoranalysis.plugin.image.obj.merge.priority;

/*-
 * #%L
 * anchor-plugin-image
 * %%
 * Copyright (C) 2010 - 2019 Owen Feehan, ETH Zurich, University of Zurich, Hoffmann la Roche
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */

import org.anchoranalysis.image.objmask.ObjMask;
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
	public PrioritisedVertex( ObjMask obj, double payload, double priority, boolean considerForMerge ) {
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