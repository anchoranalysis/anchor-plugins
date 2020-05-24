package org.anchoranalysis.plugin.annotation.bean.comparison;

import java.util.Optional;

/*
 * #%L
 * anchor-annotation
 * %%
 * Copyright (C) 2016 ETH Zurich, University of Zurich, Owen Feehan
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


import org.anchoranalysis.annotation.io.assignment.Assignment;
import org.anchoranalysis.io.error.AnchorIOException;
import org.anchoranalysis.io.output.bound.BoundOutputManagerRouteErrors;
import org.anchoranalysis.io.output.csv.CSVWriter;
import org.anchoranalysis.plugin.annotation.comparison.AnnotationGroup;
import org.anchoranalysis.plugin.annotation.comparison.AnnotationGroupList;

class CSVComparisonGroup<T extends Assignment> {

	private AnnotationGroupList<T> annotationGroupList;
	
	public CSVComparisonGroup(AnnotationGroupList<T> annotationGroupList) {
		super();
		this.annotationGroupList = annotationGroupList;
	}

	private void writeGroupStatsForGroup( AnnotationGroup<T> annotationGroup, CSVWriter writer ) {
		
		writer.writeRow(
			annotationGroup.createValues()				
		);		
	}
	
	public void writeGroupStats( BoundOutputManagerRouteErrors outputManager ) throws AnchorIOException {
		
		Optional<CSVWriter> writer = CSVWriter.createFromOutputManager("byGroup", outputManager.getDelegate());

		if (!writer.isPresent()) {
			return;
		}
			
		try {
			writer.get().writeHeaders(
				annotationGroupList.first().createHeaders()
			);
			
			for( AnnotationGroup<T> group : annotationGroupList) {
				writeGroupStatsForGroup( group, writer.get() );
			}
			
		} finally {
			writer.get().close();
		}
	}
}
