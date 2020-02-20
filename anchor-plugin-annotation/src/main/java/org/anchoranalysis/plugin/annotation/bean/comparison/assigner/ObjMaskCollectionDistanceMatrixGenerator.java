package org.anchoranalysis.plugin.annotation.bean.comparison.assigner;

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


import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.anchoranalysis.annotation.io.assignment.ObjMaskCollectionDistanceMatrix;
import org.anchoranalysis.core.text.TypedValue;
import org.anchoranalysis.image.objmask.ObjMask;
import org.anchoranalysis.image.objmask.ObjMaskCollection;
import org.anchoranalysis.io.generator.csv.CSVGenerator;
import org.anchoranalysis.io.output.bean.OutputWriteSettings;
import org.anchoranalysis.io.output.csv.CSVWriter;
import org.anchoranalysis.io.output.error.OutputWriteFailedException;

class ObjMaskCollectionDistanceMatrixGenerator extends CSVGenerator {

	private ObjMaskCollectionDistanceMatrix distanceMatrix;
	private int numDecimalPlaces;
	
	public ObjMaskCollectionDistanceMatrixGenerator(
			ObjMaskCollectionDistanceMatrix distanceMatrix, int numDecimalPlaces ) {
		super("objMaskCollectionDistanceMatrix");
		this.distanceMatrix = distanceMatrix;
		this.numDecimalPlaces = numDecimalPlaces;
	}

	@Override
	public void writeToFile(OutputWriteSettings outputWriteSettings,
			Path filePath) throws OutputWriteFailedException {
		
		try (CSVWriter csvWriter = CSVWriter.create(filePath)) {
			
			// A sensible header string, bearing in the mind the first column has object descriptions
			List<String> headers = dscrFromObjMaskCollection( distanceMatrix.getObjs2() );
			headers.add(0, "Objects");
			
			csvWriter.writeHeaders(headers);
			
			// The descriptions of objs1 go in the first column
			List<String> column0 = dscrFromObjMaskCollection( distanceMatrix.getObjs1() );
			
			for( int i=0; i<distanceMatrix.sizeObjs1(); i++ ) {
				List<TypedValue> row = rowFromDistanceMatrix(i);
				
				// Insert the description
				row.add(0, new TypedValue(column0.get(i)) );
				
				csvWriter.writeRow(row);
			}
		} catch (IOException e) {
			throw new OutputWriteFailedException(e);
		}
	}
	
	private List<TypedValue> rowFromDistanceMatrix( int indx1 ) {
		List<TypedValue> out = new ArrayList<TypedValue>();
		
		for( int indx2=0; indx2<distanceMatrix.sizeObjs2(); indx2++) {
			out.add( new TypedValue( distanceMatrix.getDistance(indx1, indx2), numDecimalPlaces) );
		}
		
		return out;
	}
	
	
	// A description of each object in a collection
	private static List<String> dscrFromObjMaskCollection( ObjMaskCollection objs ) {
		List<String> out = new ArrayList<String>();
		
		for( ObjMask om : objs ) {
			String dscr = om.centerOfGravity().toString();
			out.add( dscr );
		}
		
		return out;
	}
}
