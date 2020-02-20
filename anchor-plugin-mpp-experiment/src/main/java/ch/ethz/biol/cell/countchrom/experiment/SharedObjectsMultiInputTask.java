package ch.ethz.biol.cell.countchrom.experiment;

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


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.anchoranalysis.anchor.mpp.bean.init.MPPInitParams;
import org.anchoranalysis.bean.annotation.AllowEmpty;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.define.Define;
import org.anchoranalysis.bean.shared.random.RandomNumberGeneratorBean;
import org.anchoranalysis.bean.shared.random.RandomNumberGeneratorMersenneConstantBean;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.log.LogErrorReporter;
import org.anchoranalysis.core.name.store.SharedObjects;
import org.anchoranalysis.experiment.JobExecutionException;
import org.anchoranalysis.experiment.bean.task.TaskWithoutSharedState;
import org.anchoranalysis.experiment.task.ParametersBound;
import org.anchoranalysis.image.init.ImageInitParams;
import org.anchoranalysis.image.io.bean.output.feature.table.OutputFeatureTable;
import org.anchoranalysis.image.stack.NamedImgStackCollection;
import org.anchoranalysis.io.output.bound.BoundOutputManagerRouteErrors;
import org.anchoranalysis.io.output.error.OutputWriteFailedException;
import org.anchoranalysis.mpp.io.input.MultiInput;

public class SharedObjectsMultiInputTask extends TaskWithoutSharedState<MultiInput> {

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
	
	// Allows feature tables to be also outputted
	@BeanField
	private List<OutputFeatureTable> listOutputFeatureTable = new ArrayList<>();
	
	/**
	 * If non-empty, A keyValueParams is treated as part of the nrgStack 
	 */
	@BeanField @AllowEmpty
	private String nrgParamsName = "";
	// END BEAN PROPERTIES

	@Override
	protected void doJobOnInputObject(	ParametersBound<MultiInput,Object> params)	throws JobExecutionException {
		
		LogErrorReporter logErrorReporter = params.getLogErrorReporter();
		MultiInput inputObject = params.getInputObject();
		BoundOutputManagerRouteErrors outputManager = params.getOutputManager();
		
		try {
			SharedObjects so = new SharedObjects( logErrorReporter	);
			MPPInitParams soMPP = MPPInitParams.create(
				so,
				namedDefinitions,
				logErrorReporter,
				randomNumberGenerator.create()
			);
			ImageInitParams soImage = soMPP.getImage();
			
			inputObject.addToSharedObjects( soMPP, soImage );
			
			if (suppressOutputExceptions) {
				SharedObjectsUtilities.output(soMPP, outputManager, logErrorReporter, suppressSubfolders);
				outputFeatureTables(soImage, outputManager, logErrorReporter );
			} else {
				SharedObjectsUtilities.outputWithException(soMPP, outputManager, suppressSubfolders);
				outputFeatureTablesWithException(soImage, outputManager, logErrorReporter);
			}
			
			SharedObjectsUtilities.writeNRGStackParams( soImage, nrgParamsName, outputManager, logErrorReporter );

			
		} catch (OperationFailedException | OutputWriteFailedException | IOException | CreateException e) {
			throw new JobExecutionException(e);
		}
	}

	@Override
	public boolean hasVeryQuickPerInputExecution() {
		return false;
	}

	private void outputFeatureTables( ImageInitParams so, BoundOutputManagerRouteErrors outputManager, LogErrorReporter logErrorReporter ) {
		for (OutputFeatureTable oft : listOutputFeatureTable) {
			try {
				oft.initRecursive(so, logErrorReporter);
				oft.output(outputManager, logErrorReporter);
			} catch (IOException | InitException e) {
				logErrorReporter.getErrorReporter().recordError(NamedImgStackCollection.class, e);
			}
		}
	}
	
	private void outputFeatureTablesWithException( ImageInitParams so, BoundOutputManagerRouteErrors outputManager, LogErrorReporter logErrorReporter ) throws IOException {
		for (OutputFeatureTable oft : listOutputFeatureTable) {
			
			try {
				oft.initRecursive(so, logErrorReporter);
			} catch (InitException e) {
				throw new IOException(e);
			}
			oft.output(outputManager, logErrorReporter);
		}
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


	public List<OutputFeatureTable> getListOutputFeatureTable() {
		return listOutputFeatureTable;
	}


	public void setListOutputFeatureTable(
			List<OutputFeatureTable> listOutputFeatureTable) {
		this.listOutputFeatureTable = listOutputFeatureTable;
	}

	public String getNrgParamsName() {
		return nrgParamsName;
	}

	public void setNrgParamsName(String nrgParamsName) {
		this.nrgParamsName = nrgParamsName;
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
