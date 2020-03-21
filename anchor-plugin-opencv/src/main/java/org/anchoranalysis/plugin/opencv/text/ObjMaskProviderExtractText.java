package org.anchoranalysis.plugin.opencv.text;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;



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

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.geometry.Point3i;
import org.anchoranalysis.image.bean.provider.ObjMaskProvider;
import org.anchoranalysis.image.bean.provider.stack.StackProvider;
import org.anchoranalysis.image.extent.BoundingBox;
import org.anchoranalysis.image.extent.ImageDim;
import org.anchoranalysis.image.objmask.ObjMask;
import org.anchoranalysis.image.objmask.ObjMaskCollection;
import org.anchoranalysis.image.scale.ScaleFactor;
import org.anchoranalysis.image.scale.ScaleFactorUtilities;
import org.anchoranalysis.image.stack.Stack;
import org.anchoranalysis.plugin.opencv.MatConverter;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.dnn.Dnn;
import org.opencv.dnn.Net;
import org.opencv.imgproc.Imgproc;


/**
 * Extracts text from a RGB image by using the EAST deep neural network model
 * 
 * <p>Particular thanks to <a href="https://www.pyimagesearch.com/2018/08/20/opencv-text-detection-east-text-detector/">Adrian Rosebrock</a>
 * whose tutorial was useful in applying this model</p>
 * 
 * @author owen
 *
 */
public class ObjMaskProviderExtractText extends ObjMaskProvider {

	// Needs to be executed at least once before the OpenCV library is used
	static {
		nu.pattern.OpenCV.loadShared();
	}
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	// START BEAN PROPERTIES
	/** An RGB stack to look for text in */
	@BeanField
	private StackProvider stackProvider;
	
	/** Proposed bounding boxes below this confidence interval are removed */
	@BeanField
	private double minConfidence = 0.5;
	
	/** Bounding boxes with IoU scores above this threshold are removed */
	@BeanField
	private double suppressIntersectionOverUnionAbove = 0.3;
	// END BEAN PROPERTIES
	
	/** The image is first scaled to this size, which must be a multiple of 80, 80 for EAST */
	private static final int RESIZE_WIDTH = 320;
	private static final int RESIZE_HEIGHT = 320;
	
	@Override
	public ObjMaskCollection create() throws CreateException {

		// TODO check that it's a RGB stack
		Stack stack = stackProvider.create();
		
		Mat input = resizeMatToTarget(
			MatConverter.fromStack( stack )
		);
		
		ScaleFactor sf = ScaleFactorUtilities.calcRelativeScale(
			new ImageDim(input.cols(), input.rows(), 0),
			stack.getDimensions()
		);
		
				
		//double rX = ((double) stack.getDimensions().getX()) / input.cols();
		//double rY = ((double) stack.getDimensions().getY()) / input.rows();
		
		List<BoundingBoxWithConfidence> listBoxes = extractBoundingBoxes(input);
		
		// Apply non-maxima suppression
		List<BoundingBoxWithConfidence> listAfterSupression = NonMaximaSuppression.apply(
			listBoxes,
			suppressIntersectionOverUnionAbove
		);
		
		// Scale each bounding box back to the original-size, and convert into an object-mask
		return scaledMasksForEachBox(listAfterSupression, sf);
	}
	
	private List<BoundingBoxWithConfidence> extractBoundingBoxes( Mat input ) {
		
		List<BoundingBoxWithConfidence> list = new ArrayList<>();
		
		Path pathModel = getSharedObjects().getModelDir().resolve("frozen_east_text_detection.pb");
		
		Net net = Dnn.readNetFromTensorflow(pathModel.toAbsolutePath().toString());
		
		Scalar meanSubtractionConstants = new Scalar(123.68, 116.78, 103.94);
		
		net.setInput(
			Dnn.blobFromImage(input, 1.0, input.size(), meanSubtractionConstants, true, false )
		);

		
		// Calculated seperately due to a bug in the OpenCV java library which returns an exception (know further details)
		//  when they are calculated together. A similar bug looks to be reported here:
		// https://answers.opencv.org/question/214676/android-java-dnnforward-multiple-output-layers-segmentation-fault/
		Mat scores = net.forward("feature_fusion/Conv_7/Sigmoid");
		Mat geometry = net.forward("feature_fusion/concat_3");
			
		
		Mat scores2 = scores.reshape(1, 1);
		Mat geometry2 = geometry.reshape(1, 5);
		
		int rowsByCols = (int) scores2.size().width;
		
		float[] scoresData = arrayFromMat(scores2, 0, rowsByCols);
		
		float[] xData0 = arrayFromMat(geometry2, 0, rowsByCols);
		float[] xData1 = arrayFromMat(geometry2, 1, rowsByCols);
		float[] xData2 = arrayFromMat(geometry2, 2, rowsByCols);
		float[] xData3 = arrayFromMat(geometry2, 3, rowsByCols);
		float[] anglesData = arrayFromMat(geometry2, 4, rowsByCols);
		
		int numCols = (int) Math.floor( Math.sqrt(rowsByCols) );
		int numRows = rowsByCols / numCols;
		
		int index = 0;
		for (int y=0; y<numRows; y++) {
			for (int x=0; x<numCols; x++) {
				
				float confidence = scoresData[index]; 
				if (confidence>=minConfidence) {
					int offsetX = x * 4;
					int offsetY = y * 4;
					
					// Rotation angle
					float angle = anglesData[index];
					double cos = Math.cos(angle);
					double sin = Math.sin(angle);
					
					// Width and height of bounding box
					float h = xData0[index] + xData2[index];
					float w = xData1[index] + xData3[index];
					
					// Starting and ending coordinates
					double endX_d = offsetX + (cos * xData1[index]) + (sin * xData2[index]);
					int endX = (int) endX_d;
										
					double endY_d = offsetY - (sin * xData1[index]) + (cos * xData2[index]);
					int endY = (int) endY_d;
										
					int startX = endX - ((int) w) - 1;
					int startY = endY - ((int) h) - 1;
					
					BoundingBox bbox = new BoundingBox(
						new Point3i(startX, startY, 0),
						new Point3i(endX, endY, 0)
					);
					
					list.add(
						new BoundingBoxWithConfidence(bbox, confidence)
					);
					
					// Add the bounding box coordinates and the probability scores to a list
					
				}
				index++;
			}
			
		}
		
		return list;
	}
	
	private ObjMaskCollection scaledMasksForEachBox( List<BoundingBoxWithConfidence> list, ScaleFactor sf ) {
		
		ObjMaskCollection objs = new ObjMaskCollection();
		for (BoundingBoxWithConfidence bbox : list) {
			bbox.scaleXYPosAndExtnt(sf);
			
			getLogger().getLogReporter().logFormatted(
				"Bounding box %s with confidence %f",
				bbox.toString(),
				bbox.getConfidence()
			);
		
			objs.add(
				objWithAllPixelsOn(bbox.getBBox())
			);
		}
		
		return objs;
	}
	
	private static ObjMask objWithAllPixelsOn( BoundingBox bbox ) {
		ObjMask om = new ObjMask(bbox);
		om.binaryVoxelBox().setAllPixelsToOn();
		return om;
	}
	
	private static float[] arrayFromMat(Mat mat, int rowIndex, int arrSize) {
		float arr[] = new float[arrSize];
		mat.get(rowIndex, 0, arr);
		return arr;
	}
		
	private Mat resizeMatToTarget( Mat src ) {
		Mat dst = new Mat();
		Size sz = new Size(RESIZE_WIDTH,RESIZE_HEIGHT);
		Imgproc.resize(src, dst, sz);
		return dst;
	}

	public StackProvider getStackProvider() {
		return stackProvider;
	}

	public void setStackProvider(StackProvider stackProvider) {
		this.stackProvider = stackProvider;
	}

	public double getSuppressIntersectionOverUnionAbove() {
		return suppressIntersectionOverUnionAbove;
	}

	public void setSuppressIntersectionOverUnionAbove(double suppressIntersectionOverUnionAbove) {
		this.suppressIntersectionOverUnionAbove = suppressIntersectionOverUnionAbove;
	}

	public double getMinConfidence() {
		return minConfidence;
	}

	public void setMinConfidence(double minConfidence) {
		this.minConfidence = minConfidence;
	}

}
