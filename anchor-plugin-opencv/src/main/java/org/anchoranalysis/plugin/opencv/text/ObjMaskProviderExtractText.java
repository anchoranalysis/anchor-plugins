package org.anchoranalysis.plugin.opencv.text;

import java.nio.file.Path;
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
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.image.bean.provider.ObjMaskProvider;
import org.anchoranalysis.image.bean.provider.stack.StackProvider;
import org.anchoranalysis.image.extent.BoundingBox;
import org.anchoranalysis.image.extent.Extent;
import org.anchoranalysis.image.objmask.ObjMask;
import org.anchoranalysis.image.objmask.ObjMaskCollection;
import org.anchoranalysis.image.scale.ScaleFactor;
import org.anchoranalysis.image.scale.ScaleFactorUtilities;
import org.anchoranalysis.image.stack.Stack;
import org.anchoranalysis.plugin.opencv.CVInit;
import org.anchoranalysis.plugin.opencv.MatConverter;
import org.apache.commons.math3.util.Pair;
import org.opencv.core.Mat;
import org.opencv.core.Size;
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

	static {
		CVInit.alwaysExecuteBeforeCallingLibrary();
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
	
	/** Iff TRUE, non-maxima-suppression is applied to filter the proposed bounding boxes */
	@BeanField
	private boolean suppressNonMaxima = true;
	// END BEAN PROPERTIES
	
	/** Only exact integral multiples of this size in each dimension can be accepted as input */
	private static final Extent EAST_EXTENT = new Extent(32,32,0);
	
	@Override
	public ObjMaskCollection create() throws CreateException {

		Stack stack = createInput();
		
		Pair<Extent, ScaleFactorInt> pairFirstScale;
		try {
			pairFirstScale = FindLargestMultipleWithin.apply(EAST_EXTENT, stack.getDimensions().getExtnt());
		} catch (OperationFailedException e) {
			throw new CreateException("Cannot scale input to size needed for EAST", e);
		}
		
		Pair<Mat,ScaleFactor> pair = createScaledInput(stack, pairFirstScale.getFirst());
		
		List<BoundingBoxWithConfidence> listBoxes = EastBoundingBoxExtractor.extractBoundingBoxes(
			pair.getFirst(),
			minConfidence,
			pathToEastModel(),
			pairFirstScale.getSecond()
		);
		
		// Scale each bounding box back to the original-size, and convert into an object-mask
		return scaledMasksForEachBox(
			maybeFilterList(listBoxes),
			pair.getSecond()
		);
	}
	
	private Stack createInput() throws CreateException {
		
		Stack stack = stackProvider.create();

		if (stack.getNumChnl()!=3) {
			throw new CreateException("Non-RGB stacks are not supported by this algorithm");
		}
		
		if (stack.getDimensions().getZ()>1) {
			throw new CreateException("z-stacks are not supported by this algorithm");
		}
		
		return stack;
	}

	/** Maybe apply non-maxima suppression */
	private List<BoundingBoxWithConfidence> maybeFilterList( List<BoundingBoxWithConfidence> listBoxes ) {
		if (suppressNonMaxima) {
			return NonMaximaSuppression.apply(
				listBoxes,
				suppressIntersectionOverUnionAbove
			);
		} else {
			return listBoxes;
		}
	}
	
	/** Returns a scaled-down version of the stack, and a scale-factor that would return it to original size */
	private Pair<Mat,ScaleFactor> createScaledInput( Stack stack, Extent targetExtent ) throws CreateException {
		
		Mat original = MatConverter.makeRGBStack(stack);
		
		Mat input = resizeMatToTarget(original, targetExtent);
		
		ScaleFactor sf = calcRelativeScale(original, input);
		
		return new Pair<>( input, sf );
	}
	
	private static ScaleFactor calcRelativeScale(Mat original, Mat resized) {
		return ScaleFactorUtilities.calcRelativeScale(
			extentFromMat(resized),
			extentFromMat(original)
		);
	}
	
	private static Extent extentFromMat( Mat mat ) {
		return new Extent(
			mat.cols(),
			mat.rows(),
			0
		);
	}
	
	private Path pathToEastModel() {
		return getSharedObjects().getModelDir().resolve("frozen_east_text_detection.pb");
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
		
	private Mat resizeMatToTarget( Mat src, Extent targetExtent ) {
		Mat dst = new Mat();
		Size sz = new Size(targetExtent.getX(),targetExtent.getY());
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

	public boolean isSuppressNonMaxima() {
		return suppressNonMaxima;
	}

	public void setSuppressNonMaxima(boolean suppressNonMaxima) {
		this.suppressNonMaxima = suppressNonMaxima;
	}

}
