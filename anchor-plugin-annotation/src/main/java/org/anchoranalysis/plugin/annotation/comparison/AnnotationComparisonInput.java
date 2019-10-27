package org.anchoranalysis.plugin.annotation.comparison;

import java.nio.file.Path;

import org.anchoranalysis.annotation.io.bean.comparer.Comparer;

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

import org.anchoranalysis.core.error.reporter.ErrorReporter;
import org.anchoranalysis.image.io.bean.rasterreader.RasterReader;
import org.anchoranalysis.io.input.InputFromManager;

public class AnnotationComparisonInput<T extends InputFromManager> implements InputFromManager {

	private T inputObject;
	
	private Comparer comparerLeft;
	private Comparer comparerRight;
	
	private String nameLeft;
	private String nameRight;

	private RasterReader rasterReader;
	
	// Names are allowed be null, to indicate they are not defined
	public AnnotationComparisonInput(T inputObject, Comparer comparerLeft, Comparer comparerRight, String nameLeft, String nameRight, RasterReader rasterReader) {
		super();
		this.inputObject = inputObject;
		this.comparerLeft = comparerLeft;
		this.comparerRight = comparerRight;
		this.nameLeft = nameLeft;
		this.nameRight = nameRight;
		this.rasterReader = rasterReader;
	}

	@Override
	public String descriptiveName() {
		return inputObject.descriptiveName();
	}

	@Override
	public Path pathForBinding() {
		return inputObject.pathForBinding();
	}
	
	// Uses a boolean flag to multiplex between comparerLeft and comparerRight
	public Comparer getComparerMultiplex( boolean left ) {
		if (left) {
			return comparerLeft;
		} else {
			return comparerRight;
		}
	}

	@Override
	public void close(ErrorReporter errorReporter) {
		inputObject.close(errorReporter);
	}

	public T getInputObject() {
		return inputObject;
	}

	public void setInputObject(T inputObject) {
		this.inputObject = inputObject;
	}

	public String getNameLeft() {
		return nameLeft;
	}
	
	public String getNameRight() {
		return nameRight;
	}

	public Comparer getComparerLeft() {
		return comparerLeft;
	}

	public void setComparerLeft(Comparer comparerLeft) {
		this.comparerLeft = comparerLeft;
	}

	public Comparer getComparerRight() {
		return comparerRight;
	}

	public void setComparerRight(Comparer comparerRight) {
		this.comparerRight = comparerRight;
	}

	public RasterReader getRasterReader() {
		return rasterReader;
	}

	public void setRasterReader(RasterReader rasterReader) {
		this.rasterReader = rasterReader;
	}
}
