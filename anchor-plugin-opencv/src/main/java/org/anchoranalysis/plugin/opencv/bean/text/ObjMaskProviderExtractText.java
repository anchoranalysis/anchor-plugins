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
import org.anchoranalysis.image.bean.provider.ObjectCollectionProvider;
import org.anchoranalysis.image.bean.provider.stack.StackProvider;
import org.anchoranalysis.image.extent.Extent;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.image.scale.ScaleFactor;
import org.anchoranalysis.image.stack.Stack;
import org.anchoranalysis.plugin.opencv.CVInit;
import org.anchoranalysis.plugin.opencv.nonmaxima.NonMaximaSuppressionObjs;
import org.anchoranalysis.plugin.opencv.nonmaxima.WithConfidence;
import org.apache.commons.math3.util.Pair;
import org.opencv.core.Mat;


/**
 * Extracts text from a RGB image by using the EAST deep neural network model
 * 
 * <p>Particular thanks to <a href="https://www.pyimagesearch.com/2018/08/20/opencv-text-detection-east-text-detector/">Adrian Rosebrock</a>
 * whose tutorial was useful in applying this model</p>
 * 
 * @author Owen Feehan
 *
 */
public class ObjMaskProviderExtractText extends ObjectCollectionProvider {

	static {
		CVInit.alwaysExecuteBeforeCallingLibrary();
	}
	
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
	private static final Extent EAST_EXTENT = new Extent(32,32,1);
	
	/** As the EAST detector was designed to work with originally 1280x720 pixel images approximately
	 *   we don't allow dramatically higher resolutions that this, so text objects remain roughly
	 *   in size proportionate to what EAST was trained on.
	 */
	private static final int MAX_SCALE_FACTOR = (720/EAST_EXTENT.getY());
	
	@Override
	public ObjectCollection create() throws CreateException {
		
		Stack stack = createInput();
				
		// Scales the input to the largest acceptable-extent
		Pair<Mat,ScaleFactor> pair = CreateScaledInput.apply(
			stack,
			findLargestExtent( stack.getDimensions().getExtent() )
		);
		
		// Convert marks to object-masks
		List<WithConfidence<ObjectMask>> objsWithConfidence = EastObjsExtractor.apply(
			pair.getFirst(),
			stack.getDimensions().getRes(),
			minConfidence,
			pathToEastModel()
		);
		
		// Scale each object-mask and extract as an ObjMaskCollection
		return ScaleExtractObjs.apply(
			maybeFilterList(objsWithConfidence),
			pair.getSecond()
		);
	}
	
	/** 
	 * Finds largest allowed extent to scale the input image down to
	 * 
	 * @param stayWithin an upper bound on what's allowed
	 * @return the largest extent allowed that is a scale multiple of EAST_EXTENT
	 * @throws CreateException
	 */
	private static Extent findLargestExtent( Extent stayWithin ) throws CreateException {
		try {
			return FindLargestMultipleWithin.apply(
				EAST_EXTENT,
				stayWithin,
				MAX_SCALE_FACTOR
			);
		} catch (OperationFailedException e) {
			throw new CreateException("Cannot scale input to size needed for EAST", e);
		}
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
	
	private List<WithConfidence<ObjectMask>> maybeFilterList( List<WithConfidence<ObjectMask>> list ) {
		if (suppressNonMaxima) {
			return new NonMaximaSuppressionObjs().apply(
				list,
				suppressIntersectionOverUnionAbove
			);
		} else {
			return list;
		}
	}
		
	private Path pathToEastModel() {
		return getInitializationParameters().getModelDir().resolve("frozen_east_text_detection.pb");
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
