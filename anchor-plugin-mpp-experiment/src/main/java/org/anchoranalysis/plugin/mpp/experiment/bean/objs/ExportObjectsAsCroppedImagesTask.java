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

import org.anchoranalysis.anchor.mpp.bean.init.GeneralInitParams;
import org.anchoranalysis.anchor.mpp.bean.init.MPPInitParams;
import org.anchoranalysis.bean.NamedBean;
import org.anchoranalysis.bean.StringSet;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.Optional;
import org.anchoranalysis.bean.define.Define;
import org.anchoranalysis.bean.shared.random.RandomNumberGeneratorBean;
import org.anchoranalysis.bean.shared.random.RandomNumberGeneratorMersenneConstantBean;
import org.anchoranalysis.core.bridge.IObjectBridge;
import org.anchoranalysis.core.cache.IdentityOperation;
import org.anchoranalysis.core.color.ColorIndex;
import org.anchoranalysis.core.color.ColorList;
import org.anchoranalysis.core.color.RGBColor;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.error.reporter.ErrorReporter;
import org.anchoranalysis.core.index.GetOperationFailedException;
import org.anchoranalysis.core.log.LogErrorReporter;
import org.anchoranalysis.core.log.LogReporter;
import org.anchoranalysis.core.name.store.SharedObjects;
import org.anchoranalysis.experiment.ExperimentExecutionException;
import org.anchoranalysis.experiment.JobExecutionException;
import org.anchoranalysis.experiment.task.InputTypesExpected;
import org.anchoranalysis.experiment.task.ParametersBound;
import org.anchoranalysis.experiment.task.ParametersExperiment;
import org.anchoranalysis.image.bean.provider.stack.StackProvider;
import org.anchoranalysis.image.extent.BoundingBox;
import org.anchoranalysis.image.extent.ImageDim;
import org.anchoranalysis.image.init.ImageInitParams;
import org.anchoranalysis.image.io.generator.raster.bbox.ExtractedBBoxGenerator;
import org.anchoranalysis.image.io.generator.raster.bbox.ExtractedBBoxOnRGBObjMaskGenerator;
import org.anchoranalysis.image.io.generator.raster.obj.ObjWithBoundingBoxGenerator;
import org.anchoranalysis.image.io.generator.raster.obj.rgb.RGBObjMaskGenerator;
import org.anchoranalysis.image.objmask.ObjMask;
import org.anchoranalysis.image.objmask.ObjMaskCollection;
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
import org.anchoranalysis.io.output.bound.BoundOutputManagerRouteErrors;
import org.anchoranalysis.io.output.error.OutputWriteFailedException;
import org.anchoranalysis.mpp.io.input.MultiInput;
import org.anchoranalysis.plugin.mpp.experiment.outputter.SharedObjectsUtilities;


/**
 * Exports a cropped image for each object-mask showing its context iwthin an image
 * 
 * <p>Specifically, a bounding-box is placed around an object-mask, maybe padded and extended, and this is shown</p>
 * 
 * @author owen
 *
 */
public class ExportObjectsAsCroppedImagesTask extends ExportObjectsBase<MultiInput,Object> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	
	// START BEAN PROPERTIES
	@BeanField
	private Define namedDefinitions;
	
	@BeanField
	private RandomNumberGeneratorBean randomNumberGenerator = new RandomNumberGeneratorMersenneConstantBean();
	
	@BeanField
	private boolean suppressSubfolders = true;
	
	@BeanField
	private boolean suppressOutputExceptions = false;
	
	@BeanField @Optional
	private List<NamedBean<StackProvider>> listStackProvider = new ArrayList<>();	// The channels we apply the masks to - all assumed to be of same dimension
	
	@BeanField @Optional
	private List<NamedBean<StackProvider>> listStackProviderMIP = new ArrayList<>();	// The channels we apply the masks to - all assumed to be of same dimension
	
	@BeanField
	private StringSet outputRGBOutline = new StringSet();
	
	@BeanField
	private StringSet outputRGBOutlineMIP = new StringSet();
	
	@BeanField
	private int outlineWidth = 1;
	
	@BeanField
	private boolean extendInZ = false;	// Extends the objects in z-dimension (uses maximum intensity for the segmentation, but in all slices)
	
	/**
	 * If true, rather than writing out a bounding-box around the object mask, the entire image is written
	 */
	@BeanField
	private boolean keepEntireImage = false;
	// END BEAN PROPERTIES
	
	@Override
	public InputTypesExpected inputTypesExpected() {
		return new InputTypesExpected(MultiInput.class);
	}
	
	
	@Override
	public boolean hasVeryQuickPerInputExecution() {
		return false;
	}
	
	@Override
	public void doJobOnInputObject(	ParametersBound<MultiInput,Object> params)	throws JobExecutionException {
		
		LogErrorReporter logErrorReporter = params.getLogErrorReporter();
		MultiInput inputObject = params.getInputObject();
		BoundOutputManagerRouteErrors outputManager = params.getOutputManager();
		
		try {
			SharedObjects so = new SharedObjects( logErrorReporter );
			MPPInitParams soMPP = MPPInitParams.create(
				so,
				namedDefinitions,
				new GeneralInitParams(
					randomNumberGenerator.create(),
					params.getExperimentArguments().getModelDirectory(),
					logErrorReporter
				)
			);
			ImageInitParams soImage = soMPP.getImage();
			
			inputObject.addToSharedObjects( soMPP, soImage );
			
			NamedImgStackCollection stackCollection = createStackCollection(soImage, logErrorReporter);
			NamedImgStackCollection stackCollectionMIP = createStackCollectionMIP(soImage, logErrorReporter);
			
			if (stackCollection.keys().size()==0) {
				// Nothing to do
				return;
			}
			
			ImageDim dim = stackCollection.getException( stackCollection.keys().iterator().next() ).getDimensions();
			
			ObjMaskCollection objsZ = maybeExtendZObjs(
				inputObjs(soImage, logErrorReporter),
				dim.getZ()
			);
			
			outputGeneratorSeq(
				createGenerator(dim, stackCollection, stackCollectionMIP),
				so,
				objsZ,
				outputManager,
				logErrorReporter
			);
					
			// For-e
			if (suppressOutputExceptions) {
				SharedObjectsUtilities.output(soMPP, outputManager, logErrorReporter, suppressSubfolders);
			} else {
				SharedObjectsUtilities.outputWithException(soMPP, outputManager, suppressSubfolders);
			}
			
		} catch (OperationFailedException | OutputWriteFailedException | CreateException | GetOperationFailedException | InitException e) {
			throw new JobExecutionException(e);
		}
	}

	@Override
	public Object beforeAnyJobIsExecuted(BoundOutputManagerRouteErrors outputManager, ParametersExperiment params)
			throws ExperimentExecutionException {
		return null;
	}


	@Override
	public void afterAllJobsAreExecuted(BoundOutputManagerRouteErrors outputManager, Object sharedState,
			LogReporter logReporter) throws ExperimentExecutionException {
	}	
	
	private void outputGeneratorSeq(
		IterableGenerator<ObjMask> generator,
		SharedObjects so,
		ObjMaskCollection objs,
		BoundOutputManagerRouteErrors outputManager,
		LogErrorReporter logErrorReporter
	) throws CreateException {
		GeneratorSequenceIncrementalRerouteErrors<ObjMask> generatorSeq = createGeneratorSequence(
			generator,
			outputManager,
			so,
			logErrorReporter.getErrorReporter()
		);
		
		generatorSeq.start();
		
		for(ObjMask om : objs) {
			generatorSeq.add(om);
		}
		
		generatorSeq.end();
	}
	
	private ObjMaskCollection maybeExtendZObjs(ObjMaskCollection objsCollection, int sizeZ) throws CreateException {
		
		if (extendInZ) {
			objsCollection = extendObjsInZ(objsCollection, sizeZ);
		}
		
		return objsCollection;
	}
	

	private NamedImgStackCollection createStackCollection( ImageInitParams so, LogErrorReporter logger ) throws JobExecutionException {
		// Get named image stack collection
		ImageDim dim = null;
		NamedImgStackCollection stackCollection = new NamedImgStackCollection();
			
			
		for( NamedBean<StackProvider> ni : listStackProvider ) {
			
			try {
				ni.getValue().initRecursive(so, logger );
			} catch (InitException e) {
				// NB if we cannot create a particular channel provider, we simply skip.  We use this as a means to provide for channels
				//  that might not always be present
				logger.getErrorReporter().recordError(ExportObjectsAsCroppedImagesTask.class,e);
				continue;
			}
			
			
			Stack stack;
			try {
				stack = ni.getValue().create();
			} catch (CreateException e) {
				throw new JobExecutionException(e);
			}
			
			if (dim==null) {
				dim = stack.getDimensions();
			} else {
				if (!stack.getDimensions().equals(dim)) {
					throw new JobExecutionException(
						String.format("Channel dimensions are not uniform across the channels (%s vs %s)", stack.getDimensions(), dim )
					);
				}
			}
			
			try {
				stackCollection.add(ni.getName(), new IdentityOperation<>(stack) );
			} catch (OperationFailedException e) {
				throw new JobExecutionException(e);
			}
		}
			
		
		return stackCollection;			
	}
	
	
	
	
	private NamedImgStackCollection createStackCollectionMIP( ImageInitParams so, LogErrorReporter logger ) throws JobExecutionException {
		// Get named image stack collection
		ImageDim dim = null;
		NamedImgStackCollection stackCollection = new NamedImgStackCollection();
			
			
		for( NamedBean<StackProvider> ni : listStackProviderMIP ) {
			
			try {
				ni.getValue().initRecursive(so, logger);
			} catch (InitException e) {
				// NB if we cannot create a particular channel provider, we simply skip.  We use this as a means to provide for channels
				//  that might not always be present
				continue;
			}
			
			
			Stack stack;
			try {
				stack = ni.getValue().create();
			} catch (CreateException e) {
				throw new JobExecutionException(e);
			}
			
			if (dim==null) {
				dim = stack.getDimensions();
			} else {
				if (!stack.getDimensions().equals(dim)) {
					throw new JobExecutionException("Stack dimensions do not match");
				}
			}
			
			try {
				stackCollection.add(ni.getName(), new IdentityOperation<>(stack) );
			} catch (OperationFailedException e) {
				throw new JobExecutionException(e);
			}
		}
			
		
		return stackCollection;			
	}
	
	
	
	private IterableGenerator<ObjMask> wrapBBoxGenerator( IterableGenerator<BoundingBox> generator, final boolean mip ) {
		return new IterableGeneratorBridge<>(
			generator,
			new IObjectBridge<ObjMask, BoundingBox>() {

				@Override
				public BoundingBox bridgeElement(ObjMask sourceObject)
						throws GetOperationFailedException {
					if (mip) {
						BoundingBox bbox = new BoundingBox( sourceObject.getBoundingBox() );
						bbox.flattenZ();
						return bbox;
					} else {
						return sourceObject.getBoundingBox();
					}
				}
			}
		);
	}
	
	private IterableGenerator<ObjMask> createRGBObjMaskGenerator(
		ExtractedBBoxGenerator generator,
		ColorIndex colorIndex,
		boolean mip
	) throws CreateException {
		RGBObjMaskGenerator rgbObjMaskGenerator = new RGBObjMaskGenerator( new RGBOutlineWriter(outlineWidth), colorIndex);
		return new ExtractedBBoxOnRGBObjMaskGenerator(rgbObjMaskGenerator, generator, "rgbOutline", mip);
	}
	
	private IterableGenerator<ObjMask> createGenerator(
		final ImageDim dim,
		NamedImgStackCollection stackCollection,
		NamedImgStackCollection stackCollectionMIP
	) throws CreateException {
		
		String manifestFunction = "rasterExtract";
		
		IterableCombinedListGenerator<ObjMask> out = new IterableCombinedListGenerator<>();

		out.add( "mask", new ObjWithBoundingBoxGenerator(dim.getRes()) );
		
		try {
			for( String key : stackCollection.keys() ) {
				
				Stack stack = stackCollection.getException(key);
				
				ExtractedBBoxGenerator generatorBBox = createBBoxGeneratorForStack(stack, manifestFunction ); 
							
				out.add( key, wrapBBoxGenerator(generatorBBox,false) );
				
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
			
		} catch (GetOperationFailedException e) {
			throw new CreateException(e);
		}

		
		// Maybe we need to change the objectMask to a padded version
		IObjectBridge<ObjMask, ObjMask> bridgeToMaybePad = new IObjectBridge<ObjMask, ObjMask>() {

			@Override
			public ObjMask bridgeElement(ObjMask om)
					throws GetOperationFailedException {
				try {
					if (keepEntireImage) {
						return extractObjMaskKeepEntireImage(om, dim );
					} else {
						return maybePadObjMask(om, dim );
					}

				} catch (OutputWriteFailedException e) {
					throw new GetOperationFailedException(e);
				}
				
			}
		};

		return new IterableGeneratorBridge<>(out, bridgeToMaybePad);
	}
	
	
	private static ObjMask extractObjMaskKeepEntireImage( ObjMask om, ImageDim dim ) throws OutputWriteFailedException {
		return BBoxUtilities.createObjMaskForBBox(
			om,
			new BoundingBox(dim.getExtnt())
		);
	}
		
	private GeneratorSequenceIncrementalRerouteErrors<ObjMask> createGeneratorSequence( IterableGenerator<ObjMask> generator, BoundOutputManagerRouteErrors outputManager, SharedObjects psoImage, ErrorReporter errorReporter ) {
		IndexableOutputNameStyle outputNameStyle = new IntegerPrefixOutputNameStyle("extractedObjs", 6);
		
		GeneratorSequenceIncrementalRerouteErrors<ObjMask> writer = new GeneratorSequenceIncrementalRerouteErrors<>(
			new GeneratorSequenceIncrementalWriter<>(
				outputManager.getDelegate(),
				outputNameStyle.getOutputName(),
				outputNameStyle,
				generator,
				0,
				true
			),
			errorReporter
		);
		return writer;
	}
	
	private static ObjMaskCollection extendObjsInZ( ObjMaskCollection objs, int sz ) {
		ObjMaskCollection out = new ObjMaskCollection();
		for( ObjMask om : objs ) {
			out.add( om.flattenZ().growToZ(sz) );
		}
		return out;
	}
	
	


	public boolean isSuppressSubfolders() {
		return suppressSubfolders;
	}


	public void setSuppressSubfolders(boolean suppressSubfolders) {
		this.suppressSubfolders = suppressSubfolders;
	}


	public boolean isSuppressOutputExceptions() {
		return suppressOutputExceptions;
	}


	public void setSuppressOutputExceptions(boolean suppressOutputExceptions) {
		this.suppressOutputExceptions = suppressOutputExceptions;
	}

	public StringSet getOutputRGBOutline() {
		return outputRGBOutline;
	}


	public void setOutputRGBOutline(StringSet outputRGBOutline) {
		this.outputRGBOutline = outputRGBOutline;
	}


	public StringSet getOutputRGBOutlineMIP() {
		return outputRGBOutlineMIP;
	}


	public void setOutputRGBOutlineMIP(StringSet outputRGBOutlineMIP) {
		this.outputRGBOutlineMIP = outputRGBOutlineMIP;
	}


	public boolean isExtendInZ() {
		return extendInZ;
	}


	public void setExtendInZ(boolean extendInZ) {
		this.extendInZ = extendInZ;
	}


	public List<NamedBean<StackProvider>> getListStackProviderMIP() {
		return listStackProviderMIP;
	}


	public void setListStackProviderMIP(
			List<NamedBean<StackProvider>> listStackProviderMIP) {
		this.listStackProviderMIP = listStackProviderMIP;
	}


	public List<NamedBean<StackProvider>> getListStackProvider() {
		return listStackProvider;
	}


	public void setListStackProvider(
			List<NamedBean<StackProvider>> listStackProvider) {
		this.listStackProvider = listStackProvider;
	}

	public boolean isKeepEntireImage() {
		return keepEntireImage;
	}

	public void setKeepEntireImage(boolean keepEntireImage) {
		this.keepEntireImage = keepEntireImage;
	}
	
	
	public Define getNamedDefinitions() {
		return namedDefinitions;
	}


	public void setNamedDefinitions(Define namedDefinitions) {
		this.namedDefinitions = namedDefinitions;
	}

	public RandomNumberGeneratorBean getRandomNumberGenerator() {
		return randomNumberGenerator;
	}


	public void setRandomNumberGenerator(RandomNumberGeneratorBean randomNumberGenerator) {
		this.randomNumberGenerator = randomNumberGenerator;
	}
}
