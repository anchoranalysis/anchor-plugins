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
import java.util.List;
import java.util.stream.Collectors;

import org.anchoranalysis.anchor.mpp.mark.GlobalRegionIdentifiers;
import org.anchoranalysis.anchor.mpp.mark.Mark;
import org.anchoranalysis.anchor.mpp.regionmap.RegionMapSingleton;
import org.anchoranalysis.image.binary.values.BinaryValuesByte;
import org.anchoranalysis.image.extent.Extent;
import org.anchoranalysis.image.extent.ImageDim;
import org.anchoranalysis.image.extent.ImageRes;
import org.anchoranalysis.image.objmask.ObjMask;
import org.anchoranalysis.image.objmask.properties.ObjMaskWithProperties;
import org.anchoranalysis.plugin.opencv.nonmaxima.WithConfidence;
import org.opencv.core.Mat;


/**
 * Extracts object-masks representing text regions from an image
 * 
 * <p>Each object-mask represented rotated-bounding box and is associated with a confidence score</p>
 * 
 * @author owen
 *
 */
class EastObjsExtractor {

	public static List<WithConfidence<ObjMask>> apply( Mat image, ImageRes res, double minConfidence, Path pathToEastModel ) {
		List<WithConfidence<Mark>> listMarks = EastMarkExtractor.extractBoundingBoxes(
			image,
			minConfidence,
			pathToEastModel
		);
		
		// Convert marks to object-masks
		return convertMarksToObjMask(
			listMarks,
			dimsForMat(image, res )
		);
	}
	
	private static List<WithConfidence<ObjMask>> convertMarksToObjMask(
		List<WithConfidence<Mark>> listMarks,
		ImageDim dim
	) {
		return listMarks.stream()
				.map( wc -> convertToObjMask(wc, dim) )
				.collect( Collectors.toList() );
	}
		
	private static ImageDim dimsForMat( Mat mat, ImageRes res ) {
		
		int width = (int) mat.size().width;
		int height = (int) mat.size().height;
		
		ImageDim dims = new ImageDim(
			new Extent(width, height, 1),
			res
		);
		
		return dims;
	}
	
	private static WithConfidence<ObjMask> convertToObjMask( WithConfidence<Mark> wcMark, ImageDim dim ) {
		
		ObjMaskWithProperties om = wcMark.getObj().calcMask(
			dim,
			RegionMapSingleton.instance().membershipWithFlagsForIndex(GlobalRegionIdentifiers.SUBMARK_INSIDE),
			BinaryValuesByte.getDefault()
		);
		return new WithConfidence<ObjMask>(
			om.getMask(),
			wcMark.getConfidence()
		);
	}
}
