package org.anchoranalysis.plugin.annotation.comparison;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.anchoranalysis.annotation.io.assignment.Assignment;
import org.anchoranalysis.core.text.TypedValue;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

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

@RequiredArgsConstructor @Accessors(fluent=true) @EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class AnnotationGroup<T extends Assignment> implements IAddAnnotation<T> {

	// START REQUIRED ARGUMENTS
	@Getter @EqualsAndHashCode.Include
	private final String identifier;
	// END REQUIRED ARGUMENTS
	
	@Getter
	private int numImagesWithAcceptedAnnotations = 0;
	
	@Getter
	private int numImagesWithSkippedAnnotations = 0;
	
	@Getter
	private int numImagesWithoutAnnotations = 0;
	
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
		return new ArrayList<>(
			Arrays.asList(
				"name",
				"percentAnnotated",
				"percentSkipped",
				"cntImages",
				"cntImagesAnnotations",
				"cntImagesAcceptedAnnotations",
				"cntImagesSkippedAnnotations",
				"cntImagesWithoutAnnotations"
			)
		);
	}
	
	public List<TypedValue> createValues() {
		return new ArrayList<>(
			Arrays.asList(
				new TypedValue( identifier, false ),
				new TypedValue( percentAnnotatedImages(), 2 ),
				new TypedValue( percentSkippedAnnotations(), 2 ),
				new TypedValue( numImagesTotal(), 2 ),
				new TypedValue( numImagesAnnotations(), 2 ),
				new TypedValue( numImagesWithAcceptedAnnotations(), 2 ),
				new TypedValue( numImagesWithSkippedAnnotations(), 2 ),
				new TypedValue( numImagesWithoutAnnotations(), 2 )
			)
		);
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
}
