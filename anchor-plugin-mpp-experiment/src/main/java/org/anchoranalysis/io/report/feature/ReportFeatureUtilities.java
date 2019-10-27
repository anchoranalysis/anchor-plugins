package org.anchoranalysis.io.report.feature;

/*
 * #%L
 * anchor-io
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

import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.log.LogErrorReporter;
import org.anchoranalysis.core.text.TypedValue;
import org.anchoranalysis.io.bean.report.feature.ReportFeature;

public class ReportFeatureUtilities {

	public static <T> List<String> genHeaderNames( List<ReportFeature<T>> list, LogErrorReporter logger )	{
		
		// Create a list of headers
		List<String> headerNames = new ArrayList<>();
		for( ReportFeature<T> feat : list) {
			String name;
			try {
				name = feat.genTitleStr();
			} catch (OperationFailedException e) {
				name = "error";
				logger.getErrorReporter().recordError(ReportFeatureUtilities.class, e);
			}
			headerNames.add( name );
		}
		return headerNames;
	}
	
	public static <T> List<TypedValue> genElementList( List<ReportFeature<T>> list, T obj, LogErrorReporter logger ) {
		
		List<TypedValue> rowElements = new ArrayList<>();
		
		for( ReportFeature<T> feat : list) {
			String value;
			try {
				value = feat.genFeatureStrFor( obj, logger );
			} catch (OperationFailedException e) {
				value = "error";
				logger.getErrorReporter().recordError(ReportFeatureUtilities.class, e);
			}

			rowElements.add( new TypedValue(value, feat.isNumeric()) );
		}
		
		return rowElements;
	}
}
