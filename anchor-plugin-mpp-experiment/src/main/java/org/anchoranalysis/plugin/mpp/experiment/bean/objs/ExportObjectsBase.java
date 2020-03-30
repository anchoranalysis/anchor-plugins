package org.anchoranalysis.plugin.mpp.experiment.bean.objs;

/*-
 * #%L
 * anchor-plugin-mpp-experiment
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

import org.anchoranalysis.anchor.overlay.bean.objmask.writer.ObjMaskWriter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.color.ColorList;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.core.log.LogErrorReporter;
import org.anchoranalysis.experiment.task.Task;
import org.anchoranalysis.image.bean.provider.ObjMaskProvider;
import org.anchoranalysis.image.extent.BoundingBox;
import org.anchoranalysis.image.extent.Extent;
import org.anchoranalysis.image.extent.ImageDim;
import org.anchoranalysis.image.init.ImageInitParams;
import org.anchoranalysis.image.io.generator.raster.bbox.ExtractedBBoxGenerator;
import org.anchoranalysis.image.io.generator.raster.obj.rgb.RGBObjMaskGeneratorCropped;
import org.anchoranalysis.image.objmask.ObjMask;
import org.anchoranalysis.image.objmask.ObjMaskCollection;
import org.anchoranalysis.image.stack.DisplayStack;
import org.anchoranalysis.image.stack.Stack;
import org.anchoranalysis.io.input.InputFromManager;
import org.anchoranalysis.io.output.error.OutputWriteFailedException;

public abstract class ExportObjectsBase<T extends InputFromManager, S> extends Task<T,S> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// START BEAN PROPERTIES
	/**
	 * The objects that are matched against the points
	 */
	@BeanField
	private ObjMaskProvider objs;
	
	/**
	 * Padding placed on each side of the outputted image (if it's within the image) in XY directions
	 */
	@BeanField
	private int paddingXY = 0;
	
	/**
	 * Padding placed on each side of the outputted image (if it's within the image) in Z direction
	 */
	@BeanField
	private int paddingZ = 0;
	// END BEAN PROPERTIES
	
	protected ObjMaskCollection inputObjs( ImageInitParams so, LogErrorReporter logger ) throws CreateException, InitException {
		ObjMaskProvider objsDup = objs.duplicateBean();
		objsDup.initRecursive(so,logger);
		return objsDup.create();
	}
	
	
	/**
	 * Adds padding (if set) to an object-mask
	 * 
	 * @param om object-mask to be padded
	 * @param dim size of image
	 * @return either the exist object-mask (if no padding is to be added) or a padded object-mask
	 * @throws OutputWriteFailedException
	 */
	protected ObjMask maybePadObjMask( ObjMask om, ImageDim dim ) throws OutputWriteFailedException {
		
		if (paddingXY==0 && paddingZ==0) {
			return om;
		}
		
		BoundingBox bboxToExtract = new BoundingBox(om.getBoundingBox());
		
		bboxToExtract.getCrnrMin().setX( bboxToExtract.getCrnrMin().getX() - paddingXY );
		bboxToExtract.getCrnrMin().setY( bboxToExtract.getCrnrMin().getY() - paddingXY );
		bboxToExtract.getCrnrMin().setZ( bboxToExtract.getCrnrMin().getZ() - paddingZ );
		
		Extent e = bboxToExtract.extnt();
		e.setX( e.getX() + (paddingXY*2) );
		e.setY( e.getY() + (paddingXY*2) );
		e.setZ( e.getZ() + (paddingZ*2) );
		
		bboxToExtract.clipTo(dim.getExtnt());	
		return BBoxUtilities.createObjMaskForBBox( om, bboxToExtract );
	}
		
	protected ExtractedBBoxGenerator createBBoxGeneratorForStack( Stack stack, String manifestFunction ) throws CreateException {
		ExtractedBBoxGenerator generator = new ExtractedBBoxGenerator(stack, manifestFunction);
		generator.setPaddingXY(paddingXY);
		generator.setPaddingZ(paddingZ);
		return generator;
	}
		
	protected RGBObjMaskGeneratorCropped createRGBMaskGenerator(
		ObjMaskWriter objMaskWriter,
		DisplayStack background,
		ColorList colorList
	) {
		RGBObjMaskGeneratorCropped delegate = new RGBObjMaskGeneratorCropped(
			objMaskWriter,
			background,
			colorList
		);
		delegate.setPaddingXY(paddingXY);
		delegate.setPaddingZ(paddingZ);
		return delegate;
	}
	
	public ObjMaskProvider getObjs() {
		return objs;
	}

	public void setObjs(ObjMaskProvider objs) {
		this.objs = objs;
	}

	public int getPaddingXY() {
		return paddingXY;
	}

	public void setPaddingXY(int paddingXY) {
		this.paddingXY = paddingXY;
	}

	public int getPaddingZ() {
		return paddingZ;
	}

	public void setPaddingZ(int paddingZ) {
		this.paddingZ = paddingZ;
	}
}