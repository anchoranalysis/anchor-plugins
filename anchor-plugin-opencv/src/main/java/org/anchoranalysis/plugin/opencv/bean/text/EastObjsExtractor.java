package org.anchoranalysis.plugin.opencv.bean.text;

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
