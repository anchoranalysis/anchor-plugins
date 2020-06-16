package org.anchoranalysis.plugin.mpp.experiment.objs.csv;

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
import org.anchoranalysis.image.index.rtree.ObjMaskCollectionRTree;
import org.anchoranalysis.image.objectmask.properties.ObjMaskWithPropertiesCollection;
import org.anchoranalysis.plugin.mpp.experiment.bean.objs.columndefinition.ColumnDefinition;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * A row of the CSV File
 * 
 * We keep a pointer to the columnDefinition, so we can parse the CSV lines later as needed
 * 
 * @author Owen Feehan
 *
 */
public class CSVRow {
	
	private String id;
	private String group;
	private String[] line;
	
	private ColumnDefinition columnDefinition;
		
	public CSVRow(ColumnDefinition columnDefinition) {
		super();
		this.columnDefinition = columnDefinition;
	}

	public ObjMaskWithPropertiesCollection findObjsMatchingRow( ObjMaskCollectionRTree allObjs ) throws OperationFailedException {
		return columnDefinition.findObjsMatchingRow(this, allObjs);
	}
	
	public void writeToXML( Element xmlElement, Document doc ) {
		columnDefinition.writeToXML(this, xmlElement, doc);
	}

	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public String getGroup() {
		return group;
	}
	
	public void setGroup(String group) {
		this.group = group;
	}
	
	public String[] getLine() {
		return line;
	}
	public void setLine(String[] line) {
		this.line = line;
	}
	
}