package ch.ethz.biol.cell.countchrom.experiment.exportobjectsfromcsv.columndefinition;

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
import org.anchoranalysis.image.objmask.ObjMask;
import org.anchoranalysis.image.objmask.properties.ObjMaskWithProperties;
import org.anchoranalysis.image.objmask.properties.ObjMaskWithPropertiesCollection;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import ch.ethz.biol.cell.countchrom.experiment.exportobjectsfromcsv.CSVRow;

public class Pairwise extends ColumnDefinition {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// START BEAN PROPERTIES
	/**
	 * Name of CSV column with X coordinate of point for the first Object
	 */
	@BeanField
	private String columnFirstPntX = "first.insidePnt.x";

	/**
	 * Name of CSV column with Y coordinate of point for the first Object
	 */
	@BeanField
	private String columnFirstPntY = "first.insidePnt.y";
	
	/**
	 * Name of CSV column with Z coordinate of point for the first Object
	 */
	@BeanField
	private String columnFirstPntZ = "first.insidePnt.z";
	
	/**
	 * Name of CSV column with X coordinate of point for the first Object
	 */
	@BeanField
	private String columnSecondPntX = "second.insidePnt.x";

	/**
	 * Name of CSV column with Y coordinate of point for the first Object
	 */
	@BeanField
	private String columnSecondPntY = "second.insidePnt.y";
	
	/**
	 * Name of CSV column with Z coordinate of point for the first Object
	 */
	@BeanField
	private String columnSecondPntZ = "second.insidePnt.z";
	
	/**
	 * Name of CSV column with the number of pixels for the first Object
	 */
	@BeanField
	private String columnFirstNumPixels = "first.numPixels";

	/**
	 * Name of CSV column with the number of pixels for the second Object
	 */
	@BeanField
	private String columnSecondNumPixels = "second.numPixels";
	// END BEAN PROPERTIES
	

	private int indexFirstNumPixels;
	private int indexSecondNumPixels;
	private int[] indexFirstPnt = new int[3];
	private int[] indexSecondPnt = new int[3];
	
	
	
	
	@Override
	public void initHeaders( String[] headers ) throws InitException {
		super.initHeaders(headers);
		indexFirstPnt[0] = Utilities.findHeaderIndex( headers, columnFirstPntX );
		indexFirstPnt[1] = Utilities.findHeaderIndex( headers, columnFirstPntY );
		indexFirstPnt[2] = Utilities.findHeaderIndex( headers, columnFirstPntZ );
		indexSecondPnt[0] = Utilities.findHeaderIndex( headers, columnSecondPntX );
		indexSecondPnt[1] = Utilities.findHeaderIndex( headers, columnSecondPntY );
		indexSecondPnt[2] = Utilities.findHeaderIndex( headers, columnSecondPntZ );
		
		indexFirstNumPixels = Utilities.findHeaderIndex( headers, columnFirstNumPixels );
		indexSecondNumPixels = Utilities.findHeaderIndex( headers, columnSecondNumPixels );
	}
	
	
	

	
	@Override
	public ObjMaskWithPropertiesCollection findObjsMatchingRow( CSVRow csvRow, ObjMaskCollectionRTree allObjs ) throws OperationFailedException {
		
		ObjMaskWithPropertiesCollection objs = new ObjMaskWithPropertiesCollection();
		
		ObjMask obj1 = Utilities.findObjForCSVRow( allObjs, csvRow, indexFirstPnt, indexFirstNumPixels );
		ObjMask obj2 = Utilities.findObjForCSVRow( allObjs, csvRow, indexSecondPnt, indexSecondNumPixels );
		
		if (obj1==obj2) {
			throw new OperationFailedException(
				String.format("Objects are identical at point %s and %s",
					Utilities.describeObject( csvRow, indexFirstPnt, indexFirstNumPixels ),
					Utilities.describeObject( csvRow, indexSecondPnt, indexSecondNumPixels )
				)
			);
		}
		
		objs.add( new ObjMaskWithProperties(obj1) );
		objs.add( new ObjMaskWithProperties(obj2) );
		return objs;
	}
	

	@Override
	public void writeToXML(CSVRow csvRow, Element xmlElement, Document doc) {
        
		String[] line = csvRow.getLine();
		
		int numPixelsFirst = Utilities.intFromLine(line,indexFirstNumPixels);
		int numPixelsSecond = Utilities.intFromLine(line,indexSecondNumPixels);
		
		Point3i pntFirst = Utilities.pntFromLine(line,indexFirstPnt);			
		Point3i pntSecond = Utilities.pntFromLine(line,indexSecondPnt);
		
		Utilities.addPoint( "pointFirst", pntFirst, xmlElement, doc );
		Utilities.addPoint( "pointSecond", pntSecond, xmlElement, doc );
        
        Utilities.addInteger( "numPixelsFirst", numPixelsFirst, xmlElement, doc );
        Utilities.addInteger( "numPixelsSecond", numPixelsSecond, xmlElement, doc );		
	}
	
	

	public String getColumnFirstPntX() {
		return columnFirstPntX;
	}

	public void setColumnFirstPntX(String columnFirstPntX) {
		this.columnFirstPntX = columnFirstPntX;
	}

	public String getColumnFirstPntY() {
		return columnFirstPntY;
	}

	public void setColumnFirstPntY(String columnFirstPntY) {
		this.columnFirstPntY = columnFirstPntY;
	}

	public String getColumnFirstPntZ() {
		return columnFirstPntZ;
	}

	public void setColumnFirstPntZ(String columnFirstPntZ) {
		this.columnFirstPntZ = columnFirstPntZ;
	}

	public String getColumnSecondPntX() {
		return columnSecondPntX;
	}

	public void setColumnSecondPntX(String columnSecondPntX) {
		this.columnSecondPntX = columnSecondPntX;
	}

	public String getColumnSecondPntY() {
		return columnSecondPntY;
	}

	public void setColumnSecondPntY(String columnSecondPntY) {
		this.columnSecondPntY = columnSecondPntY;
	}

	public String getColumnSecondPntZ() {
		return columnSecondPntZ;
	}

	public void setColumnSecondPntZ(String columnSecondPntZ) {
		this.columnSecondPntZ = columnSecondPntZ;
	}

	public String getColumnFirstNumPixels() {
		return columnFirstNumPixels;
	}

	public void setColumnFirstNumPixels(String columnFirstNumPixels) {
		this.columnFirstNumPixels = columnFirstNumPixels;
	}

	public String getColumnSecondNumPixels() {
		return columnSecondNumPixels;
	}

	public void setColumnSecondNumPixels(String columnSecondNumPixels) {
		this.columnSecondNumPixels = columnSecondNumPixels;
	}


}
