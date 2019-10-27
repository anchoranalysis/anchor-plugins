package objmaskwithfeature;

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

public class ObjMaskWithFeatureAndPriority {
	
	private ObjMaskWithFeature omWithFeature;
	private double priority;
	private boolean considerForMerge;
	
	public ObjMaskWithFeatureAndPriority( ObjMask om, double featureVal, double priority, boolean considerForMerge ) {
		this.omWithFeature = new ObjMaskWithFeature(om, featureVal);
		this.priority = priority;
		this.considerForMerge = considerForMerge;
	}
			
	public ObjMaskWithFeature getOmWithFeature() {
		return omWithFeature;
	}

	public double getPriority() {
		return priority;
	}

	public boolean isConsiderForMerge() {
		return considerForMerge;
	}
}
