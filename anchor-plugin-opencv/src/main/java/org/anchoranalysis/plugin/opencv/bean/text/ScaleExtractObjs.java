package org.anchoranalysis.plugin.opencv.bean.text;

/*-
 * #%L
 * anchor-plugin-opencv
 * %%
 * Copyright (C) 2010 - 2020 Owen Feehan, ETH Zurich, University of Zurich, Hoffmann la Roche
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

import java.util.List;
import java.util.stream.Collectors;

import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.image.interpolator.InterpolatorFactory;
import org.anchoranalysis.image.objectmask.ObjectMask;
import org.anchoranalysis.image.objectmask.ObjectCollection;
import org.anchoranalysis.image.scale.ScaleFactor;
import org.anchoranalysis.plugin.opencv.nonmaxima.WithConfidence;

/**
 * Extracts and object-mask from the list and scales
 * 
 * @author owen
 *
 */
class ScaleExtractObjs {

	public static ObjectCollection apply( List<WithConfidence<ObjectMask>> list, ScaleFactor sf ) {
		ObjectCollection objs = extractObjs(list);
		
		// Scale back to the needed original resolution
		scaleObjs(objs, sf);
		
		return objs;
	}
	
	private static ObjectCollection extractObjs( List<WithConfidence<ObjectMask>> list ) {
		return new ObjectCollection(
			list.stream()
				.map( wc->wc.getObj() )
				.collect( Collectors.toList() )
		);
	}
	
	private static void scaleObjs( ObjectCollection objs, ScaleFactor sf ) {
		try {
			objs.scale(sf, InterpolatorFactory.getInstance().binaryResizing() );
		} catch (OperationFailedException e) {
			assert(false);
		}
	}
}
