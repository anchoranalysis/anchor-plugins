package org.anchoranalysis.plugin.image.task.bean.labeller;

/*-
 * #%L
 * anchor-plugin-image-task
 * %%
 * Copyright (C) 2010 - 2019 Owen Feehan, ETH Zurich, University of Zurich, Hoffmann la Roche
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
import java.util.ArrayList;
import java.util.List;
import org.anchoranalysis.bean.NamedBean;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.NonEmpty;
import org.anchoranalysis.bean.annotation.SkipInit;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.log.LogErrorReporter;
import org.anchoranalysis.feature.bean.list.FeatureListProvider;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.feature.calc.params.FeatureCalcParams;
import org.anchoranalysis.feature.list.NamedFeatureStore;
import org.anchoranalysis.feature.list.NamedFeatureStoreFactory;
import org.anchoranalysis.image.bean.provider.stack.StackProvider;
import org.anchoranalysis.image.io.input.ProvidesStackInput;
import org.anchoranalysis.plugin.image.task.imagefeature.calculator.FeatureCalculatorStackInputFromStore;

public class BinaryClassifierImageLabeller<T extends FeatureCalcParams> extends BinaryOutcomeImageLabeller<Object> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	// START BEAN PROPERTIES
	@BeanField @SkipInit
	private FeatureListProvider<T> classifierProvider;
	
	@BeanField @NonEmpty
	private List<NamedBean<FeatureListProvider<T>>> listFeatures = new ArrayList<>();
	
	@BeanField
	private StackProvider nrgStackProvider;
	// END BEAN PROPERTIES

	@Override
	public Object init( Path pathForBinding ) throws InitException {
		return null;
	}
	
	@Override
	public String labelFor(
		Object initParams,
		ProvidesStackInput input,
		Path modelDir,
		LogErrorReporter logErrorReporter
	) throws OperationFailedException {
		
		try {
			NamedFeatureStore<T> featureStore = NamedFeatureStoreFactory.createNamedFeatureList(
				listFeatures
			);
			
			FeatureCalculatorStackInputFromStore<T> featureCalculator = new FeatureCalculatorStackInputFromStore<>(
				input,
				getNrgStackProvider(),
				featureStore,
				modelDir,
				logErrorReporter
			);
			
			double classificationVal = featureCalculator.calcSingleFromProvider(classifierProvider,"classifierProvider");
	
			logErrorReporter.getLogReporter().logFormatted("Classification value = %f", classificationVal);
					
			// If classification val is >= 0, then it is POSITIVE
			// If classification val is < 0, then it is NEGATIVE
			boolean classificationPositive = classificationVal>=0;
			
			return classificationString(classificationPositive);
			
		} catch (FeatureCalcException | CreateException e) {
			throw new OperationFailedException(e);
		}
	}
	
	public FeatureListProvider<T> getClassifierProvider() {
		return classifierProvider;
	}

	public void setClassifierProvider(FeatureListProvider<T> classifierProvider) {
		this.classifierProvider = classifierProvider;
	}
	
	public List<NamedBean<FeatureListProvider<T>>> getListFeatures() {
		return listFeatures;
	}

	public void setListFeatures(
			List<NamedBean<FeatureListProvider<T>>> listFeatures) {
		this.listFeatures = listFeatures;
	}

	public StackProvider getNrgStackProvider() {
		return nrgStackProvider;
	}

	public void setNrgStackProvider(StackProvider nrgStackProvider) {
		this.nrgStackProvider = nrgStackProvider;
	}

}
