package org.anchoranalysis.plugin.annotation.comparison;

import java.util.ArrayList;
import java.util.List;

import org.anchoranalysis.annotation.io.assignment.Assignment;
import org.anchoranalysis.core.text.TypedValue;

/*
 * #%L
 * anchor-mpp
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


public class AnnotationGroup<T extends Assignment> implements IAddAnnotation<T> {

	private int numImagesWithAcceptedAnnotations = 0;
	private int numImagesWithSkippedAnnotations = 0;
	private int numImagesWithoutAnnotations = 0;
	
	private String identifier;
	
	public AnnotationGroup(String identifier) {
		super();
		this.identifier = identifier;
	}
	
	@Override
	public void addSkippedAnnotationImage() {
		numImagesWithSkippedAnnotations++;
	}
	
	@Override
	public void addUnannotatedImage() {
		numImagesWithoutAnnotations++;
	}

	@Override
	public void addAcceptedAnnotation( T assignment ) {
		numImagesWithAcceptedAnnotations++;
	}
	
	public List<String> createHeaders() {
		List<String> headerNames = new ArrayList<>();
		headerNames.add("name");
		
		headerNames.add("percentAnnotated");
		headerNames.add("percentSkipped");
		headerNames.add("cntImages");
		headerNames.add("cntImagesAnnotations");
		headerNames.add("cntImagesAcceptedAnnotations");
		headerNames.add("cntImagesSkippedAnnotations");
		headerNames.add("cntImagesWithoutAnnotations");
		return headerNames;
	}
	
	public List<TypedValue> createValues() {
		
		List<TypedValue> rowElements = new ArrayList<>();
		
		rowElements.add( new TypedValue( getIdentifier(), false ));
		
		rowElements.add( new TypedValue( percentAnnotatedImages(), 2 ));
		rowElements.add( new TypedValue( percentSkippedAnnotations(), 2 ));
		
		rowElements.add( new TypedValue( numImagesTotal(), 2 ));
		rowElements.add( new TypedValue( numImagesAnnotations(), 2 ));
		rowElements.add( new TypedValue( numImagesWithAcceptedAnnotations(), 2 ));
		rowElements.add( new TypedValue( numImagesWithSkippedAnnotations(), 2 ));
		rowElements.add( new TypedValue( numImagesWithoutAnnotations(), 2 ));
		
		return rowElements;
	}
	
	public int numImagesWithAcceptedAnnotations() {
		return numImagesWithAcceptedAnnotations;
	}
	
	public int numImagesWithSkippedAnnotations() {
		return numImagesWithSkippedAnnotations;
	}

	public int numImagesWithoutAnnotations() {
		return numImagesWithoutAnnotations;
	}
	
	public int numImagesTotal() {
		return numImagesWithAcceptedAnnotations + numImagesWithoutAnnotations + numImagesWithSkippedAnnotations;
	}
	
	public int numImagesAnnotations() {
		return numImagesWithAcceptedAnnotations + numImagesWithSkippedAnnotations;
	}
	
	public double percentAnnotatedImages() { 
		 return ((double) numImagesAnnotations()*100 ) / numImagesTotal();
	}
	
	public double percentSkippedAnnotations() { 
		 return ((double) (numImagesWithSkippedAnnotations) )*100 / numImagesAnnotations();
	}
	
	
	public String getIdentifier() {
		return identifier;
	}

	@Override
	public int hashCode() {
		return getIdentifier().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof AnnotationGroup)) {
			return false;
		}
		@SuppressWarnings("unchecked")
		AnnotationGroup<T> objCast = (AnnotationGroup<T>) obj;
		return getIdentifier().equals(objCast.getIdentifier());
	}
}
