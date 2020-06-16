package org.anchoranalysis.plugin.annotation.bean.comparison;

import java.util.Optional;

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
import org.anchoranalysis.core.functional.OptionalUtilities;
import org.anchoranalysis.core.log.LogErrorReporter;
import org.anchoranalysis.experiment.JobExecutionException;
import org.anchoranalysis.image.extent.ImageDim;
import org.anchoranalysis.image.io.input.ProvidesStackInput;
import org.anchoranalysis.image.objectmask.ObjectMaskCollection;
import org.anchoranalysis.io.error.AnchorIOException;
import org.anchoranalysis.io.output.bound.BoundIOContext;
import org.anchoranalysis.plugin.annotation.comparison.AnnotationComparisonInput;
import org.anchoranalysis.plugin.annotation.comparison.IAddAnnotation;
import org.anchoranalysis.plugin.annotation.comparison.ObjsToCompare;

class ObjsToCompareFactory {

	private ObjsToCompareFactory() {
	}
	
	public static Optional<ObjsToCompare> create(
		AnnotationComparisonInput<ProvidesStackInput> input,
		IAddAnnotation<?> addAnnotation,
		ImageDim dim,
		BoundIOContext context
	) throws JobExecutionException {

		// Both objects need to be found
		return OptionalUtilities.mapBoth(
			createObjs(true, "leftObj", addAnnotation, input, dim, context),
			createObjs(false,"rightObj", addAnnotation, input, dim, context),
			(left, right) -> new ObjsToCompare(left, right)
		);
	}
	
	private static Optional<ObjectMaskCollection> createObjs(
		boolean left,
		String objName,
		IAddAnnotation<?> addAnnotation,
		AnnotationComparisonInput<ProvidesStackInput> input,
		ImageDim dim,
		BoundIOContext context
	) throws JobExecutionException {
		Findable<ObjectMaskCollection> findable = createFindable(left, input, dim, context.isDebugEnabled() );
		return foundOrLogAddUnnannotated(findable, objName, addAnnotation, context.getLogger());
	}
	
	private static Optional<ObjectMaskCollection> foundOrLogAddUnnannotated(
		Findable<ObjectMaskCollection> objs,
		String objName,
		IAddAnnotation<?> addAnnotation,
		LogErrorReporter logErrorReporter
	) {
		Optional<ObjectMaskCollection> found = objs.getFoundOrLog(objName, logErrorReporter );
		if (!found.isPresent()) {
			addAnnotation.addUnannotatedImage();
		}
		return found;
	}

	private static Findable<ObjectMaskCollection> createFindable(
		boolean left,
		AnnotationComparisonInput<ProvidesStackInput> input,
		ImageDim dim,
		boolean debugMode
	) throws JobExecutionException {
		try {
			return input.getComparerMultiplex(left).createObjs(
				input.pathForBindingRequired(),
				dim,
				debugMode
			);
		} catch (CreateException | AnchorIOException e) {
			throw new JobExecutionException(e);
		}
	}
}
