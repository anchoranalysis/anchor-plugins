package org.anchoranalysis.gui.annotation.strategy;

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

import java.util.List;

import org.anchoranalysis.annotation.wholeimage.WholeImageLabelAnnotation;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.gui.annotation.bean.label.AnnotationLabel;
import org.anchoranalysis.gui.annotation.bean.label.GroupedAnnotationLabels;
import org.anchoranalysis.image.io.input.ProvidesStackInput;
import org.anchoranalysis.io.error.AnchorIOException;

public class WholeImageLabelStrategy extends SingleFilePathGeneratorStrategy {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// START BEAN PROPERTIES
	@BeanField
	private List<AnnotationLabel> labels;
	
	@BeanField
	private int weightWidthDescription;
	// END BEAN PROPERTIES

	@Override
	public String annotationLabelFor(ProvidesStackInput item) throws AnchorIOException {
		WholeImageLabelAnnotation annotation = ReadAnnotationFromFile.readCheckExists(
			annotationPathFor(item)
		);
		if (annotation==null) {
			return null;
		}
		return annotation.getLabel();
	}
	
	// This is actually called twice during a typically opening of an annotation
	// But overhead is minor (assuming not very many labels)
	public GroupedAnnotationLabels groupedLabels() {
		return new GroupedAnnotationLabels(labels);
	}
	
	@Override
	public int weightWidthDescription() {
		return weightWidthDescription;
	}
	
	public List<AnnotationLabel> getLabels() {
		return labels;
	}

	public void setLabels(List<AnnotationLabel> labels) {
		this.labels = labels;
	}

	public int getWeightWidthDescription() {
		return weightWidthDescription;
	}

	public void setWeightWidthDescription(int weightWidthDescription) {
		this.weightWidthDescription = weightWidthDescription;
	}
}
