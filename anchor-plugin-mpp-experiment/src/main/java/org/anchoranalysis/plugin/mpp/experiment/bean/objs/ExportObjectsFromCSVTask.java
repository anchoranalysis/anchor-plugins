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

import java.nio.file.Path;
import java.util.Collection;
import java.util.Set;

import org.anchoranalysis.anchor.mpp.bean.init.MPPInitParams;
import org.anchoranalysis.anchor.overlay.bean.objmask.writer.ObjMaskWriter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.Optional;
import org.anchoranalysis.core.cache.ExecuteException;
import org.anchoranalysis.core.color.ColorList;
import org.anchoranalysis.core.color.RGBColor;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.index.GetOperationFailedException;
import org.anchoranalysis.core.index.SetOperationFailedException;
import org.anchoranalysis.core.log.LogErrorReporter;
import org.anchoranalysis.core.log.LogReporter;
import org.anchoranalysis.core.name.store.SharedObjects;
import org.anchoranalysis.experiment.ExperimentExecutionException;
import org.anchoranalysis.experiment.JobExecutionException;
import org.anchoranalysis.experiment.task.InputTypesExpected;
import org.anchoranalysis.experiment.task.ParametersBound;
import org.anchoranalysis.experiment.task.ParametersExperiment;
import org.anchoranalysis.image.bean.provider.stack.StackProvider;
import org.anchoranalysis.image.index.rtree.ObjMaskCollectionRTree;
import org.anchoranalysis.image.init.ImageInitParams;
import org.anchoranalysis.image.io.generator.raster.RasterGenerator;
import org.anchoranalysis.image.io.generator.raster.obj.rgb.RGBObjMaskGeneratorCropped;
import org.anchoranalysis.image.objmask.properties.ObjMaskWithPropertiesCollection;
import org.anchoranalysis.image.stack.DisplayStack;
import org.anchoranalysis.image.stack.Stack;
import org.anchoranalysis.io.bean.filepath.generator.FilePathGenerator;
import org.anchoranalysis.io.bean.objmask.writer.RGBOutlineWriter;
import org.anchoranalysis.io.error.AnchorIOException;
import org.anchoranalysis.io.generator.Generator;
import org.anchoranalysis.io.generator.IterableGenerator;
import org.anchoranalysis.io.generator.collection.SubfolderGenerator;
import org.anchoranalysis.io.generator.combined.IterableCombinedListGenerator;
import org.anchoranalysis.io.manifest.ManifestDescription;
import org.anchoranalysis.io.output.bound.BoundOutputManagerRouteErrors;
import org.anchoranalysis.io.output.error.OutputWriteFailedException;
import org.anchoranalysis.plugin.mpp.experiment.bean.objs.columndefinition.ColumnDefinition;
import org.anchoranalysis.plugin.mpp.experiment.objs.FromCSVInputObject;
import org.anchoranalysis.plugin.mpp.experiment.objs.FromCSVSharedState;
import org.anchoranalysis.plugin.mpp.experiment.objs.csv.CSVRow;
import org.anchoranalysis.plugin.mpp.experiment.objs.csv.IndexedCSVRows;
import org.anchoranalysis.plugin.mpp.experiment.objs.csv.MapGroupToRow;

/**
 * 
 * Given a CSV with a point representing an object-mask, the object-masks are extracted from a segmentation
 * 
 * <p>The CSV filePath should be the same for all rasters passed as input. It gets cached between the threads.</p>
 * 
 * @author Owen Feehan
 *
 */
public class ExportObjectsFromCSVTask extends ExportObjectsBase<FromCSVInputObject,FromCSVSharedState> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// START BEAN PROPERTIES
	@BeanField @Optional
	private FilePathGenerator idGenerator;	// Translates an input file name to a unique ID
	
	/**
	 * The stack that is the background for our objects
	 */
	@BeanField
	private StackProvider stackProvider;
	
	/**
	 * Column definition for the CSVfiles
	 */
	@BeanField
	private ColumnDefinition columnDefinition;
	// END BEAN PROPERTIES
	
	private static RGBColor colorFirst = new RGBColor(255,0,0);
	private static RGBColor colorSecond = new RGBColor(0,255,0);
	
	@Override
	public InputTypesExpected inputTypesExpected() {
		return new InputTypesExpected(FromCSVInputObject.class);
	}
		
	@Override
	public FromCSVSharedState beforeAnyJobIsExecuted(
			BoundOutputManagerRouteErrors outputManager, ParametersExperiment params)
			throws ExperimentExecutionException {
		return new FromCSVSharedState();
	}

	@Override
	public void afterAllJobsAreExecuted(
			BoundOutputManagerRouteErrors outputManager, FromCSVSharedState sharedState, LogReporter logReporter)
			throws ExperimentExecutionException {
	
	}
	
	@Override
	public boolean hasVeryQuickPerInputExecution() {
		return false;
	}
	
	private DisplayStack createBackgroundStack( ImageInitParams so, LogErrorReporter logger ) throws CreateException, InitException {
		// Get our background-stack and objects. We duplicate to avoid threading issues
		StackProvider stackProviderDup = stackProvider.duplicateBean();
		stackProviderDup.initRecursive(so,logger);
		return DisplayStack.create(
			stackProviderDup.create()
		);
	}
	
	private Path idStringForPath( Path path, boolean debugMode ) throws AnchorIOException {
		return idGenerator!=null ? idGenerator.outFilePath(path, debugMode) : path;
	}
	
	@Override
	public void doJobOnInputObject(
		ParametersBound<FromCSVInputObject,FromCSVSharedState> params
	) throws JobExecutionException {
		
		LogErrorReporter logErrorReporter = params.getLogErrorReporter();
		FromCSVInputObject inputObject = params.getInputObject();
		BoundOutputManagerRouteErrors outputManager = params.getOutputManager();
		
		try {
			FromCSVSharedState ss = params.getSharedState();
			
			// TODO maybe move IndexedCSVRows shared-state inside input-object?
			IndexedCSVRows groupedRows = ss.getIndexedRowsOrCreate( inputObject.getCsvFilePath(), columnDefinition );

			
			// We look for rows that match our File ID
			String fileID = idStringForPath( inputObject.pathForBinding(), params.getExperimentArguments().isDebugEnabled() ).toString();
			MapGroupToRow mapGroup = groupedRows.get(fileID);
			
			if (mapGroup==null) {
				logErrorReporter.getLogReporter().logFormatted("No matching rows in CSV file for id '%s' from '%s'", fileID, inputObject.pathForBinding());
				return;
			}
			
			processFileWithMap(
				inputObject,
				mapGroup,
				groupedRows.groupNameSet(),
				outputManager,
				params.getExperimentArguments().getModelDirectory(),
				logErrorReporter
			);
			
		} catch (GetOperationFailedException | OperationFailedException | AnchorIOException | OutputWriteFailedException e) {
			throw new JobExecutionException(e);
		}
	}
	
	private void processFileWithMap(
		FromCSVInputObject inputObject,
		MapGroupToRow mapGroup,
		Set<String> groupNameSet,
		BoundOutputManagerRouteErrors outputManager,
		Path modelDir,
		LogErrorReporter logErrorReporter
	) throws OperationFailedException, OutputWriteFailedException {
		
		try {
			SharedObjects so = new SharedObjects( logErrorReporter );
			
			MPPInitParams soMPP = MPPInitParams.create(so, modelDir);
			ImageInitParams soImage = soMPP.getImage();
			
			inputObject.addToSharedObjects( soMPP, soImage );
			
			DisplayStack background = createBackgroundStack(soImage, logErrorReporter);
			
			ObjMaskCollectionRTree objs = new ObjMaskCollectionRTree(
				inputObjs(soImage,logErrorReporter)
			);
			
			// Loop through each group, and output a series of TIFFs, where set of objects is shown on a successive image, cropped appropriately
			for( String groupName : groupNameSet ) {
				logErrorReporter.getLogReporter().logFormatted("Processing group '%s'", groupName);
				
				Collection<CSVRow> rows = mapGroup.get(groupName);
				
				if (rows==null || rows.size()==0) {
					logErrorReporter.getLogReporter().logFormatted("No matching rows for group '%s'", groupName);
					continue;
				}
	
				//outputManager.resolveFolder(groupName, new FolderWritePhysical()) 
				outputGroup( String.format("group_%s",groupName), rows, objs, background, outputManager);
			}
		} catch (CreateException | InitException e) {
			throw new OperationFailedException(e);
		}
	}
	
	private void outputGroup(String label, Collection<CSVRow> rows, ObjMaskCollectionRTree objs, DisplayStack background, BoundOutputManagerRouteErrors outputManager ) throws OutputWriteFailedException {
		
		outputManager.getWriterAlwaysAllowed().write(
			label,
			() -> {
				try {
					return createGenerator(label, rows, objs, background);
				} catch (SetOperationFailedException e) {
					throw new ExecuteException(e);
				}
			}
		);
	}
	
	private SubfolderGenerator<CSVRow,Collection<CSVRow>> createGenerator( String label, Collection<CSVRow> rows, ObjMaskCollectionRTree objs, DisplayStack background) throws SetOperationFailedException {
		RGBOutlineWriter outlineWriter = new RGBOutlineWriter(1);
		outlineWriter.setForce2D(true);
				
		IterableCombinedListGenerator<CSVRow> listGenerator = new IterableCombinedListGenerator<CSVRow>();
		listGenerator.add(label, new CSVRowRGBOutlineGenerator( outlineWriter, objs, background, colorFirst, colorSecond ) );
		listGenerator.add("idXML", new CSVRowXMLGenerator() );
		
		// Output the group
		SubfolderGenerator<CSVRow,Collection<CSVRow>> subFolderGenerator = new SubfolderGenerator<CSVRow,Collection<CSVRow>>(
			listGenerator,
			"pair"
		);
		subFolderGenerator.setIterableElement(rows);
		return subFolderGenerator;
	}
	
	
	private class CSVRowRGBOutlineGenerator extends RasterGenerator implements IterableGenerator<CSVRow> {

		private RGBObjMaskGeneratorCropped delegate;
		private CSVRow element;
		
		/**
		 * All objects associated with the image.
		 */
		private ObjMaskCollectionRTree allObjs;
		
		public CSVRowRGBOutlineGenerator( ObjMaskWriter objMaskWriter, ObjMaskCollectionRTree allObjs, DisplayStack background, RGBColor colorFirst, RGBColor colorSecond ) {
			this.allObjs = allObjs;
			
			ColorList colorList = new ColorList();
			colorList.add(colorFirst);
			colorList.add(colorSecond);
			
			delegate = createRGBMaskGenerator(objMaskWriter, background, colorList);
		}
		
		@Override
		public boolean isRGB() {
			return delegate.isRGB();
		}

		@Override
		public Stack generate() throws OutputWriteFailedException {
			return delegate.generate();
		}

		@Override
		public ManifestDescription createManifestDescription() {
			return delegate.createManifestDescription();
		}

		@Override
		public CSVRow getIterableElement() {
			return element;
		}

		@Override
		public void setIterableElement(CSVRow element)
				throws SetOperationFailedException {
			this.element = element;
			
			try {
				ObjMaskWithPropertiesCollection objs = element.findObjsMatchingRow( allObjs );
				delegate.setIterableElement(objs);
				
			} catch (OperationFailedException e) {
				throw new SetOperationFailedException(e);
			}
			
		}

		@Override
		public void start() throws OutputWriteFailedException {
			delegate.start();
		}

		@Override
		public void end() throws OutputWriteFailedException {
			delegate.end();
		}

		@Override
		public Generator getGenerator() {
			return this;
		}
	}

	public FilePathGenerator getIdGenerator() {
		return idGenerator;
	}

	public void setIdGenerator(FilePathGenerator idGenerator) {
		this.idGenerator = idGenerator;
	}


	public StackProvider getStackProvider() {
		return stackProvider;
	}

	public void setStackProvider(StackProvider stackProvider) {
		this.stackProvider = stackProvider;
	}

	public ColumnDefinition getColumnDefinition() {
		return columnDefinition;
	}

	public void setColumnDefinition(
			ColumnDefinition columnDefinition) {
		this.columnDefinition = columnDefinition;
	}
}