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


import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.anchoranalysis.annotation.io.bean.comparer.Comparer;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.DefaultInstance;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.progress.ProgressReporter;
import org.anchoranalysis.core.progress.ProgressReporterMultiple;
import org.anchoranalysis.core.progress.ProgressReporterOneOfMany;
import org.anchoranalysis.image.io.bean.rasterreader.RasterReader;
import org.anchoranalysis.io.bean.input.InputManager;
import org.anchoranalysis.io.deserializer.DeserializationFailedException;
import org.anchoranalysis.io.input.InputFromManager;
import org.anchoranalysis.io.params.InputContextParams;
import org.anchoranalysis.plugin.annotation.comparison.AnnotationComparisonInput;

public class AnnotationComparisonInputManager<T extends InputFromManager> extends InputManager<AnnotationComparisonInput<T>> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// START BEAN PROPERTIES
	@BeanField
	private InputManager<T> input;
	
	@BeanField
	private String nameLeft;
	
	@BeanField
	private String nameRight;
	
	@BeanField
	private Comparer comparerLeft;
	
	@BeanField
	private Comparer comparerRight;
	
	@BeanField @DefaultInstance
	private RasterReader rasterReader;
	// END BEAN PROPERTIES
	
	@Override
	public List<AnnotationComparisonInput<T>> inputObjects(InputContextParams inputContext, ProgressReporter progressReporter)
			throws FileNotFoundException, IOException,
			DeserializationFailedException {
		
		try( ProgressReporterMultiple prm = new ProgressReporterMultiple(progressReporter, 2)) {
			
			Iterator<T> itr = input.inputObjects(inputContext, new ProgressReporterOneOfMany(prm)).iterator();
		
			prm.incrWorker();
			
			
			List<T> tempList = new ArrayList<>();
			while (itr.hasNext()) {
				tempList.add(itr.next());
			}
			
		
			List<AnnotationComparisonInput<T>> outList;
			try {
				outList = createListInputWithAnnotationPath( tempList, new ProgressReporterOneOfMany(prm) );
			} catch (CreateException e) {
				throw new IOException(e);
			}
			prm.incrWorker();
			
			return outList;
		}
	}
	
	public InputManager<T> getInput() {
		return input;
	}

	public void setInput(InputManager<T> input) {
		this.input = input;
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
	
	private List<AnnotationComparisonInput<T>> createListInputWithAnnotationPath( List<T> listInputObjects, ProgressReporter progressReporter ) throws CreateException {
		List<AnnotationComparisonInput<T>> outList = new ArrayList<>();

		progressReporter.setMin( 0 );
		progressReporter.setMax( listInputObjects.size() );
		progressReporter.open();
		
		try {
			for(int i=0; i<listInputObjects.size(); i++) {
				
				T item = listInputObjects.get(i);
				
				AnnotationComparisonInput<T> aci =
					new AnnotationComparisonInput<>(
						item,
						comparerLeft,
						comparerRight,
						nameLeft,
						nameRight,
						rasterReader
					);
				
				outList.add( aci );
				
				progressReporter.update(i);
			}
		} finally {
			progressReporter.close();
		}
		
		return outList;
	}

	public String getNameLeft() {
		return nameLeft;
	}

	public void setNameLeft(String nameLeft) {
		this.nameLeft = nameLeft;
	}

	public String getNameRight() {
		return nameRight;
	}

	public void setNameRight(String nameRight) {
		this.nameRight = nameRight;
	}
}
