package org.anchoranalysis.plugin.opencv.bean.feature;

import org.anchoranalysis.bean.AnchorBean;
import org.anchoranalysis.bean.BeanInstanceMap;
import org.anchoranalysis.bean.error.BeanMisconfiguredException;
import org.anchoranalysis.image.bean.size.SizeXY;
import org.opencv.core.Size;
import org.opencv.objdetect.HOGDescriptor;

public class HOGParameters extends AnchorBean<HOGParameters> {

	// START BEAN PROPERTIES
	/** The window-size as per OpenCV implementation (but with a different square default-size)*/
	private SizeXY windowSize = new SizeXY(64,64);
	
	/** The block-size as per OpenCV implementation (identical default-size)*/
	private SizeXY blockSize = new SizeXY(16,16);
	
	/** The block-stride as per OpenCV implementation (identical default-size)*/
	private SizeXY blockStride = new SizeXY(8,8);
	
	/** The cell-size as per OpenCV implementation (identical default-size)*/
	private SizeXY cellSize = new SizeXY(8,8);
	
	/** The number oflevels */
	private int numLevels = HOGDescriptor.DEFAULT_NLEVELS;
	// END BEAN PROPERTIES
		
	public HOGDescriptor createDescriptor() {
		return new HOGDescriptor(
			convert(windowSize),
			convert(blockSize),
			convert(blockStride),
			convert(cellSize),
			numLevels
		);
	}
	
	/** Convert to OpenCV Size class */
	private static Size convert(SizeXY in) {
		return new Size(in.getWidth(), in.getHeight());
	}

	public SizeXY getWindowSize() {
		return windowSize;
	}

	public void setWindowSize(SizeXY windowSize) {
		this.windowSize = windowSize;
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

	public int getNumLevels() {
		return numLevels;
	}

	public void setNumLevels(int numLevels) {
		this.numLevels = numLevels;
	}
}
