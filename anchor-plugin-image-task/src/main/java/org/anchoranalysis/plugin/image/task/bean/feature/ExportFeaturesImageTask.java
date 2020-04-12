package org.anchoranalysis.plugin.image.task.bean.feature;

import java.nio.file.Path;

/*
 * #%L
 * anchor-plugin-image-task
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


import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.log.LogErrorReporter;
import org.anchoranalysis.experiment.task.InputTypesExpected;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.feature.calc.ResultsVector;
import org.anchoranalysis.feature.list.NamedFeatureStore;
import org.anchoranalysis.image.bean.provider.stack.StackProvider;
import org.anchoranalysis.image.feature.stack.FeatureStackParams;
import org.anchoranalysis.image.io.input.ProvidesStackInput;
import org.anchoranalysis.io.output.bound.BoundOutputManagerRouteErrors;
import org.anchoranalysis.plugin.image.task.imagefeature.calculator.FeatureCalculatorStackInputFromStore;


/** Calculates a feature on each image **/
public class ExportFeaturesImageTask extends ExportFeaturesStoreTask<ProvidesStackInput,FeatureStackParams> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5961126655531145104L;
	
	// START BEAN PROPERTIES
	@BeanField
	private StackProvider nrgStackProvider;
	// END BEAN PROPERTIES
	
	public ExportFeaturesImageTask() {
		super("image");
	}
	
	@Override
	public InputTypesExpected inputTypesExpected() {
		return new InputTypesExpected(ProvidesStackInput.class);
	}
		
	@Override
	protected ResultsVector calcResultsVectorForInputObject(
		ProvidesStackInput inputObject,
		NamedFeatureStore<FeatureStackParams> featureStore,
		BoundOutputManagerRouteErrors outputManager,
		Path modelDir,
		LogErrorReporter logErrorReporter
	) throws FeatureCalcException {

		try {
			FeatureCalculatorStackInputFromStore featCalc = new FeatureCalculatorStackInputFromStore(
				inputObject,
				getNrgStackProvider(),
				featureStore,
				modelDir,
				logErrorReporter
			);
			
			return featCalc.calcAllInStore();
			
		} catch (OperationFailedException e) {
			throw new FeatureCalcException(e);
		}
	}
	
	public StackProvider getNrgStackProvider() {
		return nrgStackProvider;
	}

	public void setNrgStackProvider(StackProvider nrgStackProvider) {
		this.nrgStackProvider = nrgStackProvider;
	}
}
