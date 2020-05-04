package org.anchoranalysis.plugin.image.merge.condition;

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

import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.image.extent.ImageRes;
import org.anchoranalysis.image.objmask.ObjMask;


/**
 * Rather than checking if obj1 vs obj2 is accepted instead there's a two stage process
 * 
 * 1. Set Obj1
 * 2. Check Obj2 vs Step 1
 * 
 * This allows additional (costly) operations to occur after Step 1, that can be repeatedly used
 * in Step 2 e.g. growing object-masks to check for neighbouring objects
 * 
 * @author FEEHANO
 *
 */
public interface UpdatableBeforeCondition {
	
	void updateSrcObj(ObjMask omSrc, ImageRes res) throws OperationFailedException;
	
	boolean accept(ObjMask omDest);
}
