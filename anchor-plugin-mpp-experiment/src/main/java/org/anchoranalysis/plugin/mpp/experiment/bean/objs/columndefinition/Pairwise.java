package org.anchoranalysis.plugin.mpp.experiment.bean.objs.columndefinition;

import java.util.Arrays;
import java.util.Collection;

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


import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.image.index.ObjectCollectionRTree;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.image.object.properties.ObjectCollectionWithProperties;
import org.anchoranalysis.plugin.mpp.experiment.objs.csv.CSVRow;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import lombok.Getter;
import lombok.Setter;

public class Pairwise extends ColumnDefinition {

	// START BEAN PROPERTIES
	/**
	 * Name of CSV column with X coordinate of point for the first Object
	 */
	@BeanField @Getter @Setter
	private String columnFirstPointX = "first.insidePnt.x";

	/**
	 * Name of CSV column with Y coordinate of point for the first Object
	 */
	@BeanField @Getter @Setter
	private String columnFirstPointY = "first.insidePnt.y";
	
	/**
	 * Name of CSV column with Z coordinate of point for the first Object
	 */
	@BeanField @Getter @Setter
	private String columnFirstPointZ = "first.insidePnt.z";
	
	/**
	 * Name of CSV column with X coordinate of point for the first Object
	 */
	@BeanField @Getter @Setter
	private String columnSecondPointX = "second.insidePnt.x";

	/**
	 * Name of CSV column with Y coordinate of point for the first Object
	 */
	@BeanField @Getter @Setter
	private String columnSecondPointY = "second.insidePnt.y";
	
	/**
	 * Name of CSV column with Z coordinate of point for the first Object
	 */
	@BeanField @Getter @Setter
	private String columnSecondPointZ = "second.insidePnt.z";
	
	/**
	 * Name of CSV column with the number of pixels for the first Object
	 */
	@BeanField @Getter @Setter
	private String columnFirstNumberPixels = "first.numPixels";

	/**
	 * Name of CSV column with the number of pixels for the second Object
	 */
	@BeanField @Getter @Setter
	private String columnSecondNumberPixels = "second.numPixels";
	// END BEAN PROPERTIES
	
	private ObjectInCsvRowIndices indicesFirst;
	private ObjectInCsvRowIndices indicesSecond;
		
	@Override
	public void initHeaders( String[] headers ) throws InitException {
		super.initHeaders(headers);
		indicesFirst = HeaderFinder.findHeadersToDescribeObject(
			headers,
			columnFirstNumberPixels,
			pointHeadersFirst()
		);
		indicesSecond = HeaderFinder.findHeadersToDescribeObject(
			headers,
			columnSecondNumberPixels,
			pointHeadersSecond()
		);
	}
	
	@Override
	public ObjectCollectionWithProperties findObjectsMatchingRow( CSVRow csvRow, ObjectCollectionRTree allObjs ) throws OperationFailedException {
		
		ObjectCollectionWithProperties objects = new ObjectCollectionWithProperties();
		
		ObjectMask object1 = indicesFirst.findObjectFromCSVRow(allObjs, csvRow);
		ObjectMask object2 = indicesSecond.findObjectFromCSVRow(allObjs, csvRow);
		
		if (object1.equals(object2)) {
			throw new OperationFailedException(
				String.format("Objects are identical at point %s and %s",
					indicesFirst.describeObject(csvRow),
					indicesSecond.describeObject(csvRow)
				)
			);
		}
		
		objects.add(object1);
		objects.add(object2);
		return objects;
	}
	
	@Override
	public void writeToXML(CSVRow csvRow, Element xmlElement, Document doc) {
        
		AddToXMLDocument xmlDocument = new AddToXMLDocument(xmlElement, doc);
		
		String[] line = csvRow.getLine();
		
		xmlDocument.addObjectMask("First", line, indicesFirst);
		xmlDocument.addObjectMask("Sirst", line, indicesSecond);
	}
		
	private Collection<String> pointHeadersFirst() {
		return Arrays.asList(columnFirstPointX, columnFirstPointY, columnFirstPointZ);
	}
	
	private Collection<String> pointHeadersSecond() {
		return Arrays.asList(columnSecondPointX, columnSecondPointY, columnSecondPointZ);
	}
}
