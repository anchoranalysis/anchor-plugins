package org.anchoranalysis.plugin.opencv.text;

/*-
 * #%L
 * anchor-plugin-opencv
 * %%
 * Copyright (C) 2010 - 2020 Owen Feehan, ETH Zurich, University of Zurich, Hoffmann la Roche
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

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.anchoranalysis.core.geometry.Point3i;
import org.anchoranalysis.image.extent.BoundingBox;
import org.anchoranalysis.image.extent.Extent;
import org.apache.commons.math3.util.Pair;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.dnn.Dnn;
import org.opencv.dnn.Net;

class EastBoundingBoxExtractor {

	private static final Scalar MEAN_SUBTRACTION_CONSTANTS = new Scalar(123.68, 116.78, 103.94); 
	
	private static final String OUTPUT_SCORES = "feature_fusion/Conv_7/Sigmoid";
	private static final String OUTPUT_GEOMETRY = "feature_fusion/concat_3";
	
	public static List<BoundingBoxWithConfidence> extractBoundingBoxes(
		Mat input,
		double minConfidence,
		Path model,
		ScaleFactorInt scaleFactor
	) {
		
		Net net = Dnn.readNetFromTensorflow(model.toAbsolutePath().toString());
				
		net.setInput(
			Dnn.blobFromImage(input, 1.0, input.size(), MEAN_SUBTRACTION_CONSTANTS, true, false )
		);
		
		// Calculated separately due to a bug in the OpenCV java library which returns an exception (know further details)
		//  when they are calculated together. A similar bug looks to be reported here:
		// https://answers.opencv.org/question/214676/android-java-dnnforward-multiple-output-layers-segmentation-fault/
		Mat scores = net.forward(OUTPUT_SCORES);
		Mat geometry = net.forward(OUTPUT_GEOMETRY);
		
		return extractFromMatrices(
			scores,
			geometry,
			new ScaleFactorInt(4,4),
			minConfidence
		);
	}

	private static List<BoundingBoxWithConfidence> extractFromMatrices(
		Mat scores,
		Mat geometry,
		ScaleFactorInt offsetScale,
		double minConfidence
	) {
		Pair<Mat,Extent> pair = reshapeScores(scores);
		
		return extractFromMatricesReshaped(
			pair.getFirst(),
			reshapeGeometry(geometry),
			pair.getSecond(),
			offsetScale,
			minConfidence
		);	
	}
	
	private static List<BoundingBoxWithConfidence> extractFromMatricesReshaped(
		Mat scores,
		Mat geometry,
		Extent extnt,
		ScaleFactorInt offsetScale,
		double minConfidence
	) {
		List<BoundingBoxWithConfidence> list = new ArrayList<>();
		
		int rowsByCols = extnt.getVolumeXY();
		
		float[] scoresData = arrayFromMat(scores, 0, rowsByCols);
		float[][] geometryArrs = splitGeometryIntoFiveArrays(geometry, rowsByCols);
		
		int index = 0;
		for (int y=0; y<extnt.getY(); y++) {
			for (int x=0; x<extnt.getX(); x++) {
				
				float confidence = scoresData[index]; 
				if (confidence>=minConfidence) {
					
					int offsetX = offsetScale.scaledX(x);
					int offsetY = offsetScale.scaledY(y);
					
					BoundingBox bbox = boxFor(geometryArrs, index, offsetX, offsetY);
					
					list.add(
						new BoundingBoxWithConfidence(bbox, confidence)
					);
				}
				index++;
			}
			
		}
		
		return list;		
	}

	
	/**
	 * Reshapes the geometry matrix from 4D to 2D
	 * 
	 * <p>As the Java interface to OpenCV only supports 2 dimensional matrices (rather than n dimensional)
		 it is necessary to reshape the matrix to be 2 dimensions only</p>
	 * 
	 * @param geometry matrix to reshape
	 * @return the reshaped-matrix
	 */
	private static Mat reshapeGeometry( Mat geometry ) {
		return geometry.reshape(1, 5);
	}
	
	
	/** 
	 * Reshapes the scores matrix from 4d into 2d and derives the numCols/numRows 
	 * 
	 * <p>As the Java interface to OpenCV only supports 2 dimensional matrices (rather than n dimensional)
	 *	 it is necessary to reshape the matrix to be 2 dimensions only</p>
	 * 
	 * @param scores matrix to reshape
	 * @return the reshaped-matrix and the numbers of rows and columns
	 **/
	private static Pair<Mat,Extent> reshapeScores(Mat scores) {
		
		Mat scoresReshaped = scores.reshape(1, 1);
		
		int rowsByCols = (int) scoresReshaped.size().width;
		
		// Assumes its a square matrix
		int numCols = (int) Math.floor( Math.sqrt(rowsByCols) );
		int numRows = rowsByCols / numCols;
		
		return new Pair<>(
			scoresReshaped,
			new Extent(numCols, numRows, 0)
		);
	}
	
	private static float[][] splitGeometryIntoFiveArrays( Mat geometry, int rowsByCols ) {
		
		float[][] out = new float[5][];
		for( int i=0; i<5; i++) {
			out[i] = arrayFromMat(geometry, i, rowsByCols);
		}
		return out;
	}
	
	private static BoundingBox boxFor( float[][] geometryArrs, int index, int offsetX, int offsetY) {
		return boxFor(
			geometryArrs[0][index],
			geometryArrs[1][index],
			geometryArrs[2][index],
			geometryArrs[3][index],
			geometryArrs[4][index],
			offsetX,
			offsetY
		);
	}
	
	private static BoundingBox boxFor( float p0, float p1, float p2, float p3, float angle, int offsetX, int offsetY) {
		double cos = Math.cos(angle);
		double sin = Math.sin(angle);
		
		// Width and height of bounding box
		float height = p0 + p2;
		float width = p1 + p3;
		
		// Starting and ending coordinates
		double endX = offsetX + (cos * p1) + (sin * p2);
		double endY = offsetY - (sin * p1) + (cos * p2);
		
		return boxFor( (int) endX, (int) endY, (int) width, (int) height);
	}
	
	
	/** Create bounding box from the end-crnr and a width and height */
	private static BoundingBox boxFor( int endX, int endY, int width, int height) {
		
		int startX = endX - width - 1;
		int startY = endY - height - 1;
		
		return new BoundingBox(
			new Point3i(startX, startY, 0),
			new Point3i(endX, endY, 0)
		);
	}
	
	/** Extracts an array of floats from a matrix */
	private static float[] arrayFromMat(Mat mat, int rowIndex, int arrSize) {
		float arr[] = new float[arrSize];
		mat.get(rowIndex, 0, arr);
		return arr;
	}
}
