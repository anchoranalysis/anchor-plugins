package org.anchoranalysis.test.mpp;

/*-
 * #%L
 * anchor-test-mpp
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

import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.binary.BinaryChnl;
import org.anchoranalysis.image.binary.values.BinaryValues;
import org.anchoranalysis.image.objmask.ObjMask;
import org.anchoranalysis.image.objmask.ObjMaskCollection;
import org.anchoranalysis.image.objmask.factory.CreateFromConnectedComponentsFactory;
import org.anchoranalysis.image.stack.Stack;
import org.anchoranalysis.test.image.io.TestLoaderImageIO;

public class LoadUtilities {

	public static ObjMask openLargestObjectBinaryFrom( String suffix, TestLoaderImageIO testLoader ) throws CreateException {
		Stack stack = testLoader.openStackFromTestPath(
			path(suffix)
		);
		return largestObjFromStack(stack);
	}
	
	/** Gets largest connected component from a binary-image 
	 * @throws CreateException */
	private static ObjMask largestObjFromStack( Stack stack ) throws CreateException {
		
		CreateFromConnectedComponentsFactory cc = new CreateFromConnectedComponentsFactory();
		ObjMaskCollection objs = cc.createConnectedComponents(
			new BinaryChnl( stack.getChnl(0), BinaryValues.getDefault())
		);
		
		return findLargestObj(objs);
	}
	
	private static ObjMask findLargestObj( ObjMaskCollection objs ) {
		int maxSize = -1;
		ObjMask maxObj = null;
		for( ObjMask om : objs ) {
			int size = om.numPixels();
			if (size>maxSize) {
				maxSize = size;
				maxObj = om;
			}
		}
		return maxObj;
	}
	
	private static String path( String suffix ) {
		return String.format("binaryImageObj/obj%s.tif", suffix);
	}
}
