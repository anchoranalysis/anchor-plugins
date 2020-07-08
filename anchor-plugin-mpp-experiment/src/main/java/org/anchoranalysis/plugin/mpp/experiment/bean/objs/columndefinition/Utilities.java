package org.anchoranalysis.plugin.mpp.experiment.bean.objs.columndefinition;

import org.anchoranalysis.core.error.InitException;

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


import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.geometry.Point3i;
import org.anchoranalysis.core.index.SetOperationFailedException;
import org.anchoranalysis.image.index.ObjectCollectionRTree;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.plugin.mpp.experiment.objs.csv.CSVRow;
import org.apache.commons.lang.ArrayUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access=AccessLevel.PRIVATE)
class Utilities {

	static int findHeaderIndex( String[] headers, String columnName ) throws InitException {
		int index = ArrayUtils.indexOf( headers, columnName );
		if (index==ArrayUtils.INDEX_NOT_FOUND) {
			throw new InitException( String.format("Cannot find column '%s' among CSV file headers", columnName) );
		}
		return index;
	}
	
	
	static Point3i pntFromLine( String[] line, int[] indices ) {
		return new Point3i(
			intFromLine(line, indices[0]),
			intFromLine(line, indices[1]),
			intFromLine(line, indices[2])
		);
	}
	
	static int intFromLine( String[] line, int index ) {
		return Integer.parseInt( line[ index] );
	}
	

	/**
	 * Finds an object that contains a particular point, and has the exact number of voxels indicated
	 * 
	 * @param pnt
	 * @param numVoxels
	 * @return the object if found, or NULL otherwise
	 * @throws SetOperationFailedException
	 */
	private static ObjectMask findObjForPoint( ObjectCollectionRTree allObjs, Point3i pnt, int numVoxels ) throws OperationFailedException {

		ObjectCollection objs = allObjs.contains(pnt);
		
		for (ObjectMask om : objs) {
			if (om.numVoxelsOn()==numVoxels) {
				return om;
			}
		}
		
		throw new OperationFailedException(
			String.format("Cannot find matching object for %s (with numVoxels=%d)", pnt.toString(), numVoxels)
		);
	}
	
	static ObjectMask findObjForCSVRow( ObjectCollectionRTree allObjs, CSVRow csvRow, int[] indexPnt, int indexNumPixels ) throws OperationFailedException {
		
		String[] line = csvRow.getLine();
	
		return findObjForPoint(
			allObjs,
			pntFromLine(line,indexPnt),
			intFromLine(line,indexNumPixels)
		);
	}
	
	static String describeObject( CSVRow csvRow, int[] indexPnt, int indexNumPixels ) {
		
		String[] line = csvRow.getLine();
		
		return String.format(
			"%s (numVoxels=%d)",
			pntFromLine(line,indexPnt),
			intFromLine(line,indexNumPixels)
		);
	}
	
	static void addInteger( String elementName, int val, Element parent, Document doc ) {
		Element elmnRes = doc.createElement(elementName);
		parent.appendChild(elmnRes);
		
		elmnRes.appendChild( doc.createTextNode( Integer.toString(val) )); 
	}
	
	
	static void addPoint( String elementName, Point3i point, Element parent, Document doc ) {
		Element elmnRes = doc.createElement(elementName);
		parent.appendChild(elmnRes);
        
        Element x = doc.createElement("x");
        Element y = doc.createElement("y");
        Element z = doc.createElement("z");
        
        elmnRes.appendChild(x);
        elmnRes.appendChild(y);
        elmnRes.appendChild(z);
        
        x.appendChild( doc.createTextNode( Integer.toString(point.getX()) ));
        y.appendChild( doc.createTextNode( Integer.toString(point.getY()) ));
        z.appendChild( doc.createTextNode( Integer.toString(point.getZ()) ));
	}
}
