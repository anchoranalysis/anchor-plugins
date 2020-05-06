package org.anchoranalysis.plugin.image.obj.merge;

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


/**
 * A vertex in a merge graph representing an object (and and an associated payload)
 * 
 * @author Owen Feehan
 *
 */
public class ObjVertex {
	
	private ObjMask om;
	private double payload;
	private int numPixels = -1;	// We calculate it when we need to
	
	public ObjVertex(ObjMask om, double featureVal) {
		super();
		this.om = om;
		this.payload = featureVal;
	}
	
	public ObjMask getObjMask() {
		return om;
	}
	public double getPayload() {
		return payload;
	}

	public int numPixels() {
		if (numPixels==-1) {
			numPixels = om.numPixels();
		}
		return numPixels;
	}

	@Override
	public String toString() {
		return om.toString();
	}


}
