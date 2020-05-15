package org.anchoranalysis.plugin.annotation.bean.comparison;

import org.anchoranalysis.annotation.io.wholeimage.findable.Findable;

/*-
 * #%L
 * anchor-plugin-annotation
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
import org.anchoranalysis.core.log.LogErrorReporter;
import org.anchoranalysis.experiment.JobExecutionException;
import org.anchoranalysis.image.extent.ImageDim;
import org.anchoranalysis.image.io.input.ProvidesStackInput;
import org.anchoranalysis.image.objmask.ObjMaskCollection;
import org.anchoranalysis.io.output.bound.BoundIOContext;
import org.anchoranalysis.plugin.annotation.comparison.AnnotationComparisonInput;
import org.anchoranalysis.plugin.annotation.comparison.IAddAnnotation;
import org.anchoranalysis.plugin.annotation.comparison.ObjsToCompare;

class ObjsToCompareFactory {

	private ObjsToCompareFactory() {
	}
	
	// Returns null if an error occurs
	public static ObjsToCompare create(
		AnnotationComparisonInput<ProvidesStackInput> input,
		IAddAnnotation<?> addAnnotation,
		ImageDim dim,
		BoundIOContext context
	) throws JobExecutionException {
		 
		Findable<ObjMaskCollection> leftObjs = createObjs( true, input, dim, context.isDebugEnabled() );
		
		if (!checkNull(leftObjs,"leftObj", addAnnotation, context.getLogger())) {
			return null;
		}
		
		// Create our object groups, and add an assignment
		Findable<ObjMaskCollection> rightObjs = createObjs( false, input, dim, context.isDebugEnabled() );
		
		if (!checkNull(rightObjs,"rightObj", addAnnotation, context.getLogger())) {
			return null;
		}
		
		// We can only get this far if both objects were found
		return new ObjsToCompare(leftObjs.getOrNull(), rightObjs.getOrNull());
	}
	
	private static boolean checkNull(
		Findable<ObjMaskCollection> objs,
		String objName,
		IAddAnnotation<?> addAnnotation,
		LogErrorReporter logErrorReporter
	) {
		boolean success = objs.logIfFailure(objName, logErrorReporter );
		if (!success) {
			addAnnotation.addUnannotatedImage();
		}
		return success;
	}

	private static Findable<ObjMaskCollection> createObjs(
		boolean left,
		AnnotationComparisonInput<ProvidesStackInput> input,
		ImageDim dim,
		boolean debugMode
	) throws JobExecutionException {
		try {
			return input.getComparerMultiplex(left).createObjs(
				input.pathForBinding(),
				dim,
				debugMode
			);
		} catch (CreateException e) {
			throw new JobExecutionException(e);
		}
	}
}
