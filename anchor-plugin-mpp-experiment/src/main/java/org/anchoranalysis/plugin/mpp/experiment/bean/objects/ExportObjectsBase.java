package org.anchoranalysis.plugin.mpp.experiment.bean.objects;

import org.anchoranalysis.anchor.overlay.bean.DrawObject;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.color.ColorList;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.core.geometry.Point3i;
import org.anchoranalysis.core.log.Logger;
import org.anchoranalysis.experiment.task.Task;
import org.anchoranalysis.image.bean.nonbean.init.ImageInitParams;
import org.anchoranalysis.image.bean.provider.ObjectCollectionProvider;
import org.anchoranalysis.image.extent.BoundingBox;
import org.anchoranalysis.image.extent.ImageDimensions;
import org.anchoranalysis.image.io.generator.raster.bbox.ExtractedBoundingBoxGenerator;
import org.anchoranalysis.image.io.generator.raster.obj.rgb.DrawCroppedObjectsGenerator;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.image.stack.DisplayStack;
import org.anchoranalysis.image.stack.Stack;
import org.anchoranalysis.io.input.InputFromManager;
import org.anchoranalysis.io.output.error.OutputWriteFailedException;

import lombok.Getter;
import lombok.Setter;

public abstract class ExportObjectsBase<T extends InputFromManager, S> extends Task<T,S> {

	// START BEAN PROPERTIES
	/**
	 * The objects that are matched against the points
	 */
	@BeanField @Getter @Setter
	private ObjectCollectionProvider objects;
	
	/**
	 * Padding placed on each side of the outputted image (if it's within the image) in XY directions
	 */
	@BeanField @Getter @Setter
	private int paddingXY = 0;
	
	/**
	 * Padding placed on each side of the outputted image (if it's within the image) in Z direction
	 */
	@BeanField @Getter @Setter
	private int paddingZ = 0;
	// END BEAN PROPERTIES
	
	protected ObjectCollection inputObjects( ImageInitParams so, Logger logger ) throws CreateException, InitException {
		ObjectCollectionProvider objectsDuplicated = objects.duplicateBean();
		objectsDuplicated.initRecursive(so,logger);
		return objectsDuplicated.create();
	}
		
	/**
	 * Adds padding (if set) to an object-mask
	 * 
	 * @param object object-mask to be padded
	 * @param dimensions size of image
	 * @return either the exist object-mask (if no padding is to be added) or a padded object-mask
	 * @throws OutputWriteFailedException
	 */
	protected ObjectMask maybePadObject( ObjectMask object, ImageDimensions dimensions ) {
		
		if (paddingXY==0 && paddingZ==0) {
			return object;
		}
		
		BoundingBox bboxToExtract = object.getBoundingBox().growBy(
			new Point3i(paddingXY, paddingXY, paddingZ),
			dimensions.getExtent()
		);
		
		return BoundingBoxUtilities.createObjectForBoundingBox(object, bboxToExtract);
	}
		
	protected ExtractedBoundingBoxGenerator createBoundingBoxGeneratorForStack(Stack stack, String manifestFunction) {
		ExtractedBoundingBoxGenerator generator = new ExtractedBoundingBoxGenerator(
			stack,
			manifestFunction
		);
		generator.setPaddingXY(paddingXY);
		generator.setPaddingZ(paddingZ);
		return generator;
	}
		
	protected DrawCroppedObjectsGenerator createRGBMaskGenerator(
		DrawObject drawObject,
		DisplayStack background,
		ColorList colorList
	) {
		DrawCroppedObjectsGenerator delegate = new DrawCroppedObjectsGenerator(
			drawObject,
			background,
			colorList
		);
		delegate.setPaddingXY(paddingXY);
		delegate.setPaddingZ(paddingZ);
		return delegate;
	}
}
