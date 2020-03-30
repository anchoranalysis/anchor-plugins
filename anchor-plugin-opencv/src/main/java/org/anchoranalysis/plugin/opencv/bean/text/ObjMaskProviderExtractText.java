package org.anchoranalysis.plugin.opencv.bean.text;

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
import org.anchoranalysis.image.extent.Extent;
import org.anchoranalysis.image.objmask.ObjMaskCollection;
import org.anchoranalysis.image.scale.ScaleFactor;
import org.anchoranalysis.image.stack.Stack;
import org.anchoranalysis.plugin.opencv.CVInit;
import org.apache.commons.math3.util.Pair;
import org.opencv.core.Mat;


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
		
		Extent extent;
		try {
			extent = FindLargestMultipleWithin.apply(EAST_EXTENT, stack.getDimensions().getExtnt());
		} catch (OperationFailedException e) {
			throw new CreateException("Cannot scale input to size needed for EAST", e);
		}
		
		Pair<Mat,ScaleFactor> pair = CreateScaledInput.apply(stack, extent);
		
		List<BoundingBoxWithConfidence> listBoxes = EastBoundingBoxExtractor.extractBoundingBoxes(
			pair.getFirst(),
			minConfidence,
			pathToEastModel()
		);
		
		// Scale each bounding box back to the original-size, and convert into an object-mask
		return ScaleConvertToMask.scaledMasksForEachBox(
			maybeFilterList(listBoxes),
			pair.getSecond(),
			stack.getDimensions()
		);
	}
	
	private Stack createInput() throws CreateException {
		
		Stack stack = stackProvider.create();

		if (stack.getNumChnl()!=3) {
			throw new CreateException(
				String.format(
					"Non-RGB stacks are not supported by this algorithm. This stack has %d channels.",
					stack.getNumChnl()
				)
			);
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
	
	private Path pathToEastModel() {
		return getSharedObjects().getModelDir().resolve("frozen_east_text_detection.pb");
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