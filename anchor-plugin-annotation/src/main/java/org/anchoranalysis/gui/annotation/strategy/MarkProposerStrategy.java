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

import java.util.ArrayList;
import java.util.List;

import org.anchoranalysis.annotation.io.bean.comparer.MultipleComparer;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.Optional;
import org.anchoranalysis.gui.bean.mpp.MarkEvaluator;
import org.anchoranalysis.image.io.input.ProvidesStackInput;
import org.anchoranalysis.io.bean.filepath.generator.FilePathGenerator;

/** Annotates each image with a mark */
public class MarkProposerStrategy extends SingleFilePathGeneratorStrategy {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	// START BEAN PROPERTIES
	@BeanField
	private String markProposerName;
	
	@BeanField
	private String pointsFitterName;
	
	@BeanField
	private String markEvaluatorName;
	
	@BeanField @Optional
	private FilePathGenerator defaultCfgFilePathGenerator;
	
	@BeanField @Optional
	private FilePathGenerator keyValueParamsFilePathGenerator;
	
	@BeanField @Optional
	private List<FilePathGenerator> listDisplayRasters = new ArrayList<>();
	
	@BeanField @Optional
	private MultipleComparer multipleComparer;
	
	/** If-defined, mark-evaluator that is added to the GUI to support this annotation */
	@BeanField @Optional
	private MarkEvaluator markEvaluator;
	// END BEAN PROPERTIES

	@Override
	public int weightWidthDescription() {
		return 1;
	}
	
	@Override
	public String annotationLabelFor(ProvidesStackInput item) {
		return null;
	}
	
	public MarkEvaluator getMarkEvaluator() {
		return markEvaluator;
	}
	public void setMarkEvaluator(MarkEvaluator markEvaluator) {
		this.markEvaluator = markEvaluator;
	}

	public String getMarkProposerName() {
		return markProposerName;
	}
	public void setMarkProposerName(String markProposerName) {
		this.markProposerName = markProposerName;
	}
	public String getPointsFitterName() {
		return pointsFitterName;
	}
	public void setPointsFitterName(String pointsFitterName) {
		this.pointsFitterName = pointsFitterName;
	}
	
	public String getMarkEvaluatorName() {
		return markEvaluatorName;
	}
	public void setMarkEvaluatorName(String markEvaluatorName) {
		this.markEvaluatorName = markEvaluatorName;
	}

	public FilePathGenerator getDefaultCfgFilePathGenerator() {
		return defaultCfgFilePathGenerator;
	}

	public void setDefaultCfgFilePathGenerator(
			FilePathGenerator defaultCfgFilePathGenerator) {
		this.defaultCfgFilePathGenerator = defaultCfgFilePathGenerator;
	}

	public FilePathGenerator getKeyValueParamsFilePathGenerator() {
		return keyValueParamsFilePathGenerator;
	}

	public void setKeyValueParamsFilePathGenerator(
			FilePathGenerator keyValueParamsFilePathGenerator) {
		this.keyValueParamsFilePathGenerator = keyValueParamsFilePathGenerator;
	}

	public List<FilePathGenerator> getListDisplayRasters() {
		return listDisplayRasters;
	}

	public void setListDisplayRasters(List<FilePathGenerator> listDisplayRasters) {
		this.listDisplayRasters = listDisplayRasters;
	}

	public MultipleComparer getMultipleComparer() {
		return multipleComparer;
	}

	public void setMultipleComparer(MultipleComparer multipleComparer) {
		this.multipleComparer = multipleComparer;
	}
}
