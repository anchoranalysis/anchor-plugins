package org.anchoranalysis.plugin.mpp.experiment.bean.objs.columndefinition;

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
import org.anchoranalysis.core.geometry.Point3i;
import org.anchoranalysis.image.index.rtree.ObjMaskCollectionRTree;
import org.anchoranalysis.image.objectmask.ObjectMask;
import org.anchoranalysis.image.objectmask.properties.ObjectWithProperties;
import org.anchoranalysis.image.objectmask.properties.ObjectCollectionWithProperties;
import org.anchoranalysis.plugin.mpp.experiment.objs.csv.CSVRow;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class Single extends ColumnDefinition {

	// START BEAN PROPERTIES
	/**
	 * Name of CSV column with X coordinate of point for the first Object
	 */
	@BeanField
	private String columnPntX = "insidePnt.x";

	/**
	 * Name of CSV column with Y coordinate of point for the first Object
	 */
	@BeanField
	private String columnPntY = "insidePnt.y";
	
	/**
	 * Name of CSV column with Z coordinate of point for the first Object
	 */
	@BeanField
	private String columnPntZ = "insidePnt.z";
	
	/**
	 * Name of CSV column with the number of pixels for the first Object
	 */
	@BeanField
	private String columnNumPixels = "numPixels";
	// END BEAN PROPERTIES
	
	private int indexNumPixels = -1;
	private int[] indexPnt = new int[3];
	
	@Override
	public void initHeaders(String[] headers) throws InitException {
		super.initHeaders(headers);
		
		// We resolve each of our columnNames to an index
		indexPnt[0] = Utilities.findHeaderIndex( headers, columnPntX );
		indexPnt[1] = Utilities.findHeaderIndex( headers, columnPntY );
		indexPnt[2] = Utilities.findHeaderIndex( headers, columnPntZ );
		
		indexNumPixels = Utilities.findHeaderIndex( headers, columnNumPixels );
		
		assert( indexNumPixels != indexPnt[0] );
		assert( indexNumPixels != indexPnt[1] );
		assert( indexNumPixels != indexPnt[2] );
		assert( indexPnt[1] != indexPnt[2] );
		assert( indexPnt[0] != indexPnt[2] );
		assert( indexPnt[1] != indexPnt[0] );
	}

	@Override
	public ObjectCollectionWithProperties findObjsMatchingRow(CSVRow csvRow, ObjMaskCollectionRTree allObjs)
			throws OperationFailedException {
		
		assert( indexNumPixels>= 0 );

		ObjectCollectionWithProperties objs = new ObjectCollectionWithProperties();
		
		ObjectMask obj = Utilities.findObjForCSVRow( allObjs, csvRow, indexPnt, indexNumPixels );
		
		objs.add( new ObjectWithProperties(obj) );
		return objs;
	}

	@Override
	public void writeToXML(CSVRow csvRow, Element xmlElement, Document doc) {
		
		String[] line = csvRow.getLine();
		
		int numPixels = Utilities.intFromLine(line,indexNumPixels);
		
		Point3i pnt = Utilities.pntFromLine(line,indexPnt);			
		
        Utilities.addPoint( "point", pnt, xmlElement, doc );
        Utilities.addInteger( "numPixels", numPixels, xmlElement, doc );

		
	}

	public String getColumnNumPixels() {
		return columnNumPixels;
	}

	public void setColumnNumPixels(String columnNumPixels) {
		this.columnNumPixels = columnNumPixels;
	}

	public String getColumnPntX() {
		return columnPntX;
	}

	public void setColumnPntX(String columnPntX) {
		this.columnPntX = columnPntX;
	}

	public String getColumnPntY() {
		return columnPntY;
	}

	public void setColumnPntY(String columnPntY) {
		this.columnPntY = columnPntY;
	}

	public String getColumnPntZ() {
		return columnPntZ;
	}

	public void setColumnPntZ(String columnPntZ) {
		this.columnPntZ = columnPntZ;
	}
}
