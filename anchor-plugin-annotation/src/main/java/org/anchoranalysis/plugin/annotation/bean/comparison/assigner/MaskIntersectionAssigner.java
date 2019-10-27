package org.anchoranalysis.plugin.annotation.bean.comparison.assigner;

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

import org.anchoranalysis.annotation.io.assignment.AssignmentMaskIntersection;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.log.LogErrorReporter;
import org.anchoranalysis.image.extent.ImageDim;
import org.anchoranalysis.image.objmask.ObjMask;
import org.anchoranalysis.image.objmask.ObjMaskCollection;
import org.anchoranalysis.io.output.bound.BoundOutputManagerRouteErrors;
import org.anchoranalysis.plugin.annotation.comparison.AnnotationGroup;
import org.anchoranalysis.plugin.annotation.comparison.ObjsToCompare;

public class MaskIntersectionAssigner extends AnnotationComparisonAssigner<AssignmentMaskIntersection> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public AssignmentMaskIntersection createAssignment(ObjsToCompare objsToCompare, ImageDim dim, boolean useMIP,
			BoundOutputManagerRouteErrors outputManager, LogErrorReporter logErrorReporter)
			throws CreateException {
		
		
		return new AssignmentMaskIntersection(
			extractSingleObj("left", objsToCompare.getLeft()),
			extractSingleObj("right", objsToCompare.getRight())
		);
	}
	
	private static ObjMask extractSingleObj( String dscr, ObjMaskCollection objs ) throws CreateException {
		if (objs.size()==0) {
			throw new CreateException(
				String.format("%s obj contains no objects. Exactly one must exist.", dscr )	
			); 
		} else if (objs.size()>1) {
			throw new CreateException(
				String.format("%s obj contains %d objects. Only one is allowed.", dscr, objs.size() )	
			); 
		} else {
			return objs.get(0);
		}
	}

	@Override
	public AnnotationGroup<AssignmentMaskIntersection> groupForKey(String key) {
		return new AnnotationGroup<AssignmentMaskIntersection>(key);
	}

	@Override
	public boolean moreThanOneObj() {
		return false;
	}

}
