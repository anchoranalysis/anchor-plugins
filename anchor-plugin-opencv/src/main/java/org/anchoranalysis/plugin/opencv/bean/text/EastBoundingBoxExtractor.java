package org.anchoranalysis.plugin.opencv.bean.text;

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
	
	private static final ScaleFactorInt SCALE_BY_4 = new ScaleFactorInt(4,4);
	
	
	/**
	 * Extracts bounding boxes from an image using the EAST model
	 * 
	 * @param image an RGB image to extract boxes from
	 * @param minConfidence filters boxes to have confidence >= minConfidence
	 * @param pathToModel path to the model-weights
	 * 
	 * @return a list of bounding-boxes, each with a confidence value
	 */
	public static List<BoundingBoxWithConfidence> extractBoundingBoxes(
		Mat image,
		double minConfidence,
		Path pathToModel
	) {
		
		Net net = Dnn.readNetFromTensorflow(pathToModel.toAbsolutePath().toString());
				
		net.setInput(
			Dnn.blobFromImage(image, 1.0, image.size(), MEAN_SUBTRACTION_CONSTANTS, true, false )
		);
		
		// Calculated separately due to a bug in the OpenCV java library which returns an exception (know further details)
		//  when they are calculated together. A similar bug looks to be reported here:
		// https://answers.opencv.org/question/214676/android-java-dnnforward-multiple-output-layers-segmentation-fault/
		Mat scores = net.forward(OUTPUT_SCORES);
		Mat geometry = net.forward(OUTPUT_GEOMETRY);
		
		return extractFromMatrices(
			scores,
			geometry,
			SCALE_BY_4,
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
					
					BoundingBox bbox = BoundingBoxFromArrays.boxFor(geometryArrs, index, offsetX, offsetY);
					
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
		
	/** Extracts an array of floats from a matrix */
	private static float[] arrayFromMat(Mat mat, int rowIndex, int arrSize) {
		float arr[] = new float[arrSize];
		mat.get(rowIndex, 0, arr);
		return arr;
	}
}