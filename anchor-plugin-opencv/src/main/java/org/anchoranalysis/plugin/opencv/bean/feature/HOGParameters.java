package org.anchoranalysis.plugin.opencv.bean.feature;

import java.util.Optional;

import org.anchoranalysis.bean.AnchorBean;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.bean.size.SizeXY;
import org.anchoranalysis.image.extent.Extent;
import org.opencv.core.Size;
import org.opencv.objdetect.HOGDescriptor;

public class HOGParameters extends AnchorBean<HOGParameters> {

	// START BEAN PROPERTIES
	/** The window-size as per OpenCV implementation. If not specified, the window is set to be the same size as the (possibly resized) image.*/
	@BeanField @OptionalBean
	private SizeXY windowSize = new SizeXY(32,32);

	/** The block-size as per OpenCV implementation (identical default-size)*/
	@BeanField
	private SizeXY blockSize = new SizeXY(16,16);
	
	/** The block-stride as per OpenCV implementation (identical default-size)*/
	@BeanField
	private SizeXY blockStride = new SizeXY(8,8);
	
	/** The cell-size as per OpenCV implementation (identical default-size)*/
	@BeanField
	private SizeXY cellSize = new SizeXY(8,8);
	
	/** The number of bins in each histogram for a cell */
	@BeanField
	private int numBins = 9;
	// END BEAN PROPERTIES
	
	/** Calculates the size of the descriptor will be for a given image, assuming the window is equal to the image-size */
	public int sizeDescriptor(Extent imageSize) {
		
		Size windowSize = determineWindowSize(imageSize);
		// windowStride defaults to cellSize if Size() is passed (see OpenCV source file hog.cpp)
		SizeXY windowStride = cellSize;
		
		int cellsPerBlock = DivideUtilities.divide(blockSize,cellSize,SizeXY::getWidth)
				* DivideUtilities.divide(blockSize,cellSize,SizeXY::getHeight);

		int blocksPerWindow = numberSlidingWindowsFor(
			windowSize,
			sizeFor(blockSize),
			cellSize,
			blockStride
		);
		
		int windowsPerImage = numberSlidingWindowsFor(
			sizeFor(imageSize),
			windowSize,
			cellSize,
			windowStride	
		);
		
		// We assume we are always using the default winStride, which is equal to cellSize
		// windows per image
		return blocksPerWindow * cellsPerBlock * numBins * windowsPerImage;
	}
		
	public HOGDescriptor createDescriptor(Extent imageSize) {
		return new HOGDescriptor(
			determineWindowSize(imageSize),
			sizeFor(blockSize),
			sizeFor(blockStride),
			sizeFor(cellSize),
			numBins
		);
	}
	
	private Size determineWindowSize(Extent imageSize) {
		return convertOr(
			Optional.ofNullable(windowSize),
			imageSize
		);
	}
	
	public void checkSize(Extent extent) throws FeatureCalcException {
				
		SizeXY extentAsSize = new SizeXY(extent);
		
		DivideUtilities.checkDivisibleBy(extentAsSize, blockSize, "image-size", "block-size");
		DivideUtilities.checkDivisibleBy(blockSize, cellSize, "block-size", "cell-size");
		DivideUtilities.checkDivisibleBy(blockSize, blockStride, "block-size", "block-stride");
		DivideUtilities.checkDivisibleBy(blockStride, cellSize, "block-stride", "cell-size");
		
		if (windowSize!=null) {
			if (extent.getX() < windowSize.getWidth()) {
				throw new FeatureCalcException("Image width is smaller than HOG window width. This is not permitted.");
			}
			if (extent.getY() < windowSize.getHeight()) {
				throw new FeatureCalcException("Image height is smaller than HOG window height. This is not permitted.");
			}
		}
	}
	
	public SizeXY getWindowSize() {
		return windowSize;
	}

	public void setWindowSize(SizeXY windowSize) {
		this.windowSize = windowSize;
	}
	
	public int getNumBins() {
		return numBins;
	}

	public void setNumBins(int numBins) {
		this.numBins = numBins;
	}

	public SizeXY getBlockSize() {
		return blockSize;
	}

	public void setBlockSize(SizeXY blockSize) {
		this.blockSize = blockSize;
	}

	public SizeXY getBlockStride() {
		return blockStride;
	}

	public void setBlockStride(SizeXY blockStride) {
		this.blockStride = blockStride;
	}

	public SizeXY getCellSize() {
		return cellSize;
	}

	public void setCellSize(SizeXY cellSize) {
		this.cellSize = cellSize;
	}

	private static int numberSlidingWindowsFor(Size size, Size subtractFromSize, SizeXY addToSize, SizeXY stride) {
		
		int timesX = DivideUtilities.ceilDiv(
			size.width - subtractFromSize.width + addToSize.getWidth(),
			stride.getWidth()
		);
		
		int timesY = DivideUtilities.ceilDiv(
			size.height - subtractFromSize.height + addToSize.getHeight(),
			stride.getHeight()
		);
		
		return timesX * timesY;
	}

	
	/** Convert to OpenCV Size class */
	private static Size convertOr(Optional<SizeXY> in, Extent imageSize) {
		return in.map(
			HOGParameters::sizeFor
		).orElseGet( ()->
			new Size(imageSize.getX(), imageSize.getY())
		);
	}
	
	private static Size sizeFor(Extent extent) {
		return new Size(extent.getX(), extent.getY());
	}
	
	private static Size sizeFor(SizeXY size) {
		return new Size(size.getWidth(), size.getHeight());
	}
}
