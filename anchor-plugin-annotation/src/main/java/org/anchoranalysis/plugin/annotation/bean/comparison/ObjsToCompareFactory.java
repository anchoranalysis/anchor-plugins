package org.anchoranalysis.plugin.annotation.bean.comparison;

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
import org.anchoranalysis.image.io.input.StackInput;
import org.anchoranalysis.image.objmask.ObjMaskCollection;
import org.anchoranalysis.plugin.annotation.comparison.AnnotationComparisonInput;
import org.anchoranalysis.plugin.annotation.comparison.IAddAnnotation;
import org.anchoranalysis.plugin.annotation.comparison.ObjsToCompare;

class ObjsToCompareFactory {

	private ObjsToCompareFactory() {
	}
	
	// Returns null if an error occurs
	public static ObjsToCompare create(
		AnnotationComparisonInput<StackInput> input,
		IAddAnnotation<?> addAnnotation,
		LogErrorReporter logErrorReporter,
		ImageDim dim,
		boolean debugMode
	) throws JobExecutionException {
		 
		ObjMaskCollection leftObjs = createObjs( true, input, dim, debugMode );
		
		if (checkNull(leftObjs,"leftObj", input, addAnnotation, logErrorReporter)) {
			return null;
		}
		
		// Create our object groups, and add an assignment
		ObjMaskCollection rightObjs = createObjs( false, input, dim, debugMode );
		
		if (checkNull(rightObjs,"rightObj", input, addAnnotation, logErrorReporter)) {
			return null;
		}
		
		return new ObjsToCompare(leftObjs, rightObjs);
	}
	
	private static boolean checkNull(
		ObjMaskCollection objs,
		String objName,
		AnnotationComparisonInput<StackInput> input,
		IAddAnnotation<?> addAnnotation,
		LogErrorReporter logErrorReporter
	) {
		if (objs==null) {
			// Is this for certain?
			logErrorReporter.getLogReporter().logFormatted(
				"Cannot find %s for %s",
				objName,
				input.descriptiveName()
			);
			addAnnotation.addUnannotatedImage();
			return true;
		}
		return false;
	}

	private static ObjMaskCollection createObjs(
		boolean left,
		AnnotationComparisonInput<StackInput> input,
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
