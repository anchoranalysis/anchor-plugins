package org.anchoranalysis.plugin.annotation.bean.comparison;

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


import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.anchoranalysis.annotation.io.assignment.Assignment;
import org.anchoranalysis.core.text.TypedValue;
import org.anchoranalysis.io.error.AnchorIOException;
import org.anchoranalysis.io.input.InputFromManager;
import org.anchoranalysis.io.output.bound.BoundOutputManagerRouteErrors;
import org.anchoranalysis.io.output.csv.CSVWriter;

class CSVAssignment {

	private boolean includeDescriptiveSplit;
	private int maxSplitGroups;
		
	private Optional<CSVWriter> writer;
	
	private boolean firstRow = true;
	
	public CSVAssignment(BoundOutputManagerRouteErrors outputManager, String outputName, boolean includeDescriptiveSplit, int maxSplitGroups) throws AnchorIOException {
		super();
		this.includeDescriptiveSplit = includeDescriptiveSplit;
		this.maxSplitGroups = maxSplitGroups;

		writer = CSVWriter.createFromOutputManager(outputName, outputManager.getDelegate());
	}
	
	public synchronized void writeStatisticsForImage(
		Assignment assignment,
		SplitString descriptiveSplit,
		InputFromManager inputObject
	) {

		if (!writer.isPresent() || !writer.get().isOutputEnabled()) {
			return;
		}
		
		if (firstRow) {
			firstRow = false;
			
			writer.get().writeHeaders(
				createHeaders(assignment)
			);
		}
			
		writer.get().writeRow(
			createValues(
				assignment,
				inputObject,
				descriptiveSplit
			)
		);
	}
	
	private List<String> createHeaders( Assignment assignment ) {
		List<String> base = createBaseHeaders();
		base.addAll( assignment.createStatisticsHeaderNames() );
		return base;
	}
	
	private List<String> createBaseHeaders() {
		List<String> headerNames = new ArrayList<>();
		headerNames.add("descriptiveName");
		headerNames.add("filePath");
		
		if(includeDescriptiveSplit) {
			for( int i=0; i<maxSplitGroups; i++) {
				headerNames.add( String.format("dscr%d",i) );
			}
		}
		
		return headerNames;
	}
	
	private List<TypedValue> createValues( Assignment assignment, InputFromManager inputObject, SplitString descriptiveSplit ) {
		List<TypedValue> base = createBaseValues( inputObject, descriptiveSplit );
		base.addAll( assignment.createStatistics() );
		return base;
	}
	
	private List<TypedValue> createBaseValues( InputFromManager inputObject, SplitString descriptiveSplit ) {
		
		Elements rowElements = new Elements();
		
		rowElements.add( inputObject.descriptiveName() );
		rowElements.add( inputObject.pathForBinding().toString() );
				
		if (includeDescriptiveSplit) {
			for( int i=0; i<maxSplitGroups; i++) {
				rowElements.add( descriptiveSplit.getSplitPartOrEmptyString(i) );
			}
		}
		
		return rowElements.getList();
	}
		
	public void end() {
		writer.ifPresent( CSVWriter::close );
	}

	private static class Elements {
		private List<TypedValue> delegate = new ArrayList<>();
		
		public void add( String text ) {
			delegate.add( new TypedValue( text, false) );
		}

		public List<TypedValue> getList() {
			return delegate;
		}
	}
}
