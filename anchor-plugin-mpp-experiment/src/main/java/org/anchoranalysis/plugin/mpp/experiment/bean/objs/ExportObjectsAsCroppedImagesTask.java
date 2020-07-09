package org.anchoranalysis.plugin.mpp.experiment.bean.objs;

/*
 * #%L
 * anchor-plugin-mpp-experiment
 * %%
 * Copyright (C) 2016 ETH Zurich, University of Zurich, Owen Feehan
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


import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import org.anchoranalysis.bean.NamedBean;
import org.anchoranalysis.bean.StringSet;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.core.color.ColorIndex;
import org.anchoranalysis.core.color.ColorList;
import org.anchoranalysis.core.color.RGBColor;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.functional.IdentityOperation;
import org.anchoranalysis.core.log.Logger;
import org.anchoranalysis.core.name.provider.NamedProviderGetException;
import org.anchoranalysis.core.name.value.SimpleNameValue;
import org.anchoranalysis.experiment.ExperimentExecutionException;
import org.anchoranalysis.experiment.JobExecutionException;
import org.anchoranalysis.experiment.task.InputTypesExpected;
import org.anchoranalysis.experiment.task.NoSharedState;
import org.anchoranalysis.experiment.task.InputBound;
import org.anchoranalysis.experiment.task.ParametersExperiment;
import org.anchoranalysis.image.bean.nonbean.init.ImageInitParams;
import org.anchoranalysis.image.bean.provider.stack.StackProvider;
import org.anchoranalysis.image.extent.BoundingBox;
import org.anchoranalysis.image.extent.ImageDimensions;
import org.anchoranalysis.image.io.generator.raster.bbox.ExtractedBBoxGenerator;
import org.anchoranalysis.image.io.generator.raster.bbox.ExtractedBBoxOnRGBObjMaskGenerator;
import org.anchoranalysis.image.io.generator.raster.obj.ObjWithBoundingBoxGenerator;
import org.anchoranalysis.image.io.generator.raster.obj.rgb.RGBObjMaskGenerator;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.image.stack.NamedImgStackCollection;
import org.anchoranalysis.image.stack.Stack;
import org.anchoranalysis.io.bean.objmask.writer.RGBOutlineWriter;
import org.anchoranalysis.io.generator.IterableGenerator;
import org.anchoranalysis.io.generator.IterableGeneratorBridge;
import org.anchoranalysis.io.generator.combined.IterableCombinedListGenerator;
import org.anchoranalysis.io.generator.sequence.GeneratorSequenceIncrementalRerouteErrors;
import org.anchoranalysis.io.generator.sequence.GeneratorSequenceIncrementalWriter;
import org.anchoranalysis.io.namestyle.IndexableOutputNameStyle;
import org.anchoranalysis.io.namestyle.IntegerPrefixOutputNameStyle;
import org.anchoranalysis.io.output.bound.BoundIOContext;
import org.anchoranalysis.io.output.bound.BoundOutputManagerRouteErrors;
import org.anchoranalysis.io.output.error.OutputWriteFailedException;
import org.anchoranalysis.mpp.io.input.MultiInput;
import org.anchoranalysis.mpp.sgmn.bean.define.DefineOutputterMPP;

import lombok.Getter;
import lombok.Setter;


/**
 * Exports a cropped image for each object-mask showing its context iwthin an image
 * 
 * <p>Specifically, a bounding-box is placed around an object-mask, maybe padded and extended, and this is shown</p>
 * 
 * @author Owen Feehan
 *
 */
public class ExportObjectsAsCroppedImagesTask extends ExportObjectsBase<MultiInput,NoSharedState> {

	// START BEAN PROPERTIES
	@BeanField @Getter @Setter
	private DefineOutputterMPP define;
	
	@BeanField @OptionalBean @Getter @Setter
	private List<NamedBean<StackProvider>> listStackProvider = new ArrayList<>();	// The channels we apply the masks to - all assumed to be of same dimension
	
	@BeanField @OptionalBean @Getter @Setter
	private List<NamedBean<StackProvider>> listStackProviderMIP = new ArrayList<>();	// The channels we apply the masks to - all assumed to be of same dimension
	
	@BeanField @Getter @Setter
	private StringSet outputRGBOutline = new StringSet();
	
	@BeanField @Getter @Setter
	private StringSet outputRGBOutlineMIP = new StringSet();
	
	@BeanField @Getter @Setter
	private int outlineWidth = 1;
	
	@BeanField @Getter @Setter
	private boolean extendInZ = false;	// Extends the objects in z-dimension (uses maximum intensity for the segmentation, but in all slices)
	
	/**
	 * If true, rather than writing out a bounding-box around the object mask, the entire image is written
	 */
	@BeanField @Getter @Setter
	private boolean keepEntireImage = false;
	// END BEAN PROPERTIES

	@Override
	public void doJobOnInputObject(	InputBound<MultiInput,NoSharedState> params)	throws JobExecutionException {
		try {
			define.processInputImage(
				params.getInputObject(),
				params.context(),
				paramsInit -> outputObjs(paramsInit, params.context())
			);
			
		} catch (OperationFailedException e) {
			throw new JobExecutionException(e);
		}
	}
	
	
	@Override
	public InputTypesExpected inputTypesExpected() {
		return new InputTypesExpected(MultiInput.class);
	}
	
	
	@Override
	public boolean hasVeryQuickPerInputExecution() {
		return false;
	}
	
	private void outputObjs( ImageInitParams paramsInit, BoundIOContext context ) throws OperationFailedException {
		
		try {
			Logger logger = context.getLogger();
			
			NamedImgStackCollection stackCollection = createStackCollection(paramsInit, logger);
			NamedImgStackCollection stackCollectionMIP = createStackCollectionMIP(paramsInit, logger);
			
			if (stackCollection.keys().isEmpty()) {
				// Nothing to do
				return;
			}
			
			ImageDimensions dim = stackCollection.getException(
				stackCollection.keys().iterator().next()
			).getDimensions();
			
			ObjectCollection objsZ = maybeExtendZObjs(
				inputObjs(paramsInit, logger),
				dim.getZ()
			);
			
			outputGeneratorSeq(
				createGenerator(dim, stackCollection, stackCollectionMIP),
				objsZ,
				context
			);
		} catch (CreateException | InitException e) {
			throw new OperationFailedException(e);
		} catch (NamedProviderGetException e) {
			throw new OperationFailedException(e.summarize());
		}
	}
	
	@Override
	public NoSharedState beforeAnyJobIsExecuted(BoundOutputManagerRouteErrors outputManager, ParametersExperiment params)
			throws ExperimentExecutionException {
		return NoSharedState.INSTANCE;
	}


	@Override
	public void afterAllJobsAreExecuted(NoSharedState sharedState, BoundIOContext context) throws ExperimentExecutionException {
		// NOTHING TO DO
	}	
	
	private void outputGeneratorSeq(
		IterableGenerator<ObjectMask> generator,
		ObjectCollection objs,
		BoundIOContext context
	) {
		GeneratorSequenceIncrementalRerouteErrors<ObjectMask> generatorSeq = createGeneratorSequence(
			generator,
			context
		);
		
		generatorSeq.start();
		
		for(ObjectMask om : objs) {
			generatorSeq.add(om);
		}
		
		generatorSeq.end();
	}
	
	private ObjectCollection maybeExtendZObjs(ObjectCollection objsCollection, int sizeZ) {
		
		if (extendInZ) {
			objsCollection = extendObjsInZ(objsCollection, sizeZ);
		}
		
		return objsCollection;
	}
	

	private NamedImgStackCollection createStackCollection( ImageInitParams so, Logger logger ) throws CreateException {
		// Get named image stack collection
		ImageDimensions dim = null;
		NamedImgStackCollection stackCollection = new NamedImgStackCollection();
			
			
		for( NamedBean<StackProvider> ni : listStackProvider ) {
			
			try {
				ni.getValue().initRecursive(so, logger );
			} catch (InitException e) {
				// NB if we cannot create a particular channel provider, we simply skip.  We use this as a means to provide for channels
				//  that might not always be present
				logger.errorReporter().recordError(ExportObjectsAsCroppedImagesTask.class,e);
				continue;
			}
			
			
			Stack stack = ni.getValue().create();
			
			if (dim==null) {
				dim = stack.getDimensions();
			} else {
				if (!stack.getDimensions().equals(dim)) {
					throw new CreateException(
						String.format("Channel dimensions are not uniform across the channels (%s vs %s)", stack.getDimensions(), dim )
					);
				}
			}
			
			try {
				stackCollection.add(ni.getName(), new IdentityOperation<>(stack) );
			} catch (OperationFailedException e) {
				throw new CreateException(e);
			}
		}
			
		
		return stackCollection;			
	}
	
	
	
	
	private NamedImgStackCollection createStackCollectionMIP( ImageInitParams so, Logger logger ) throws CreateException {
		// Get named image stack collection
		ImageDimensions dim = null;
		NamedImgStackCollection stackCollection = new NamedImgStackCollection();
			
			
		for( NamedBean<StackProvider> ni : listStackProviderMIP ) {
			
			try {
				ni.getValue().initRecursive(so, logger);
			} catch (InitException e) {
				// NB if we cannot create a particular channel provider, we simply skip.  We use this as a means to provide for channels
				//  that might not always be present
				continue;
			}
			
			
			Stack stack = ni.getValue().create();
			
			if (dim==null) {
				dim = stack.getDimensions();
			} else {
				if (!stack.getDimensions().equals(dim)) {
					throw new CreateException("Stack dimensions do not match");
				}
			}
			
			try {
				stackCollection.add(ni.getName(), new IdentityOperation<>(stack) );
			} catch (OperationFailedException e) {
				throw new CreateException(e);
			}
		}
			
		
		return stackCollection;			
	}
	
	private static IterableGenerator<ObjectMask> wrapBBoxGenerator( IterableGenerator<BoundingBox> generator, final boolean mip ) {
		return new IterableGeneratorBridge<>(
			generator,
			sourceObject -> boundingBoxFromObject(sourceObject, mip)
		);
	}
	
	private static BoundingBox boundingBoxFromObject(ObjectMask object, boolean mip) {
		if (mip) {
			return object.getBoundingBox().flattenZ();
		} else {
			return object.getBoundingBox();
		}
	}
	
	private IterableGenerator<ObjectMask> createRGBObjMaskGenerator(
		ExtractedBBoxGenerator generator,
		ColorIndex colorIndex,
		boolean mip
	) {
		RGBObjMaskGenerator rgbObjMaskGenerator = new RGBObjMaskGenerator( new RGBOutlineWriter(outlineWidth), colorIndex);
		return new ExtractedBBoxOnRGBObjMaskGenerator(rgbObjMaskGenerator, generator, "rgbOutline", mip);
	}
	
	private IterableGenerator<ObjectMask> createGenerator(
		final ImageDimensions dim,
		NamedImgStackCollection stackCollection,
		NamedImgStackCollection stackCollectionMIP
	) throws CreateException {
		
		String manifestFunction = "rasterExtract";
		
		IterableCombinedListGenerator<ObjectMask> out = new IterableCombinedListGenerator<>(
			new SimpleNameValue<>(
				"mask",
				new ObjWithBoundingBoxGenerator(dim.getRes())
			)
		);
		
		try {
			for( String key : stackCollection.keys() ) {
				
				Stack stack = stackCollection.getException(key);
				
				ExtractedBBoxGenerator generatorBBox = createBBoxGeneratorForStack(stack, manifestFunction ); 
							
				out.add(
					key,
					wrapBBoxGenerator(generatorBBox,false)
				);
				
				if (outputRGBOutline.contains(key)) {
					out.add( key + "_RGBOutline", createRGBObjMaskGenerator(generatorBBox, new ColorList( new RGBColor(Color.GREEN) ), false) );
				}
				
				if (outputRGBOutlineMIP.contains(key)) {
					out.add( key + "_RGBOutlineMIP", createRGBObjMaskGenerator(generatorBBox, new ColorList( new RGBColor(Color.GREEN) ), true) );
				}			
			}
			
			
			for( String key : stackCollectionMIP.keys() ) {
				
				Stack stack = stackCollectionMIP.getException(key);
				
				ExtractedBBoxGenerator generatorBBox = createBBoxGeneratorForStack(stack, manifestFunction); 
							
				out.add( key, wrapBBoxGenerator(generatorBBox, true) );
				
				if (outputRGBOutlineMIP.contains(key)) {
					out.add( key + "_RGBOutlineMIP", createRGBObjMaskGenerator(generatorBBox, new ColorList( new RGBColor(Color.GREEN) ), true) );
				}			
			}
			
		} catch (NamedProviderGetException e) {
			throw new CreateException(e);
		}

		
		// Maybe we need to change the objectMask to a padded version
		return new IterableGeneratorBridge<>(
			out,
			om -> {
				if (keepEntireImage) {
					return extractObjMaskKeepEntireImage(om, dim);
				} else {
					return maybePadObjMask(om, dim);
				}
			}
		);
	}
	
	
	private static ObjectMask extractObjMaskKeepEntireImage( ObjectMask om, ImageDimensions dim ) throws OutputWriteFailedException {
		return BBoxUtilities.createObjMaskForBBox(
			om,
			new BoundingBox(dim.getExtent())
		);
	}
		
	private GeneratorSequenceIncrementalRerouteErrors<ObjectMask> createGeneratorSequence(
		IterableGenerator<ObjectMask> generator,
		BoundIOContext context
	) {
		IndexableOutputNameStyle outputNameStyle = new IntegerPrefixOutputNameStyle("extractedObjs", 6);
		
		return new GeneratorSequenceIncrementalRerouteErrors<>(
			new GeneratorSequenceIncrementalWriter<>(
				context.getOutputManager().getDelegate(),
				outputNameStyle.getOutputName(),
				outputNameStyle,
				generator,
				0,
				true
			),
			context.getErrorReporter()
		);
	}
	
	private static ObjectCollection extendObjsInZ( ObjectCollection objs, int sz ) {
		return objs.stream().map( om->
			om.flattenZ().growToZ(sz)
		);
	}
}
