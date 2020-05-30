package org.anchoranalysis.plugin.image.task.bean.feature;

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

import java.io.File;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.bean.error.BeanDuplicateException;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.functional.IdentityOperation;
import org.anchoranalysis.core.log.LogErrorReporter;
import org.anchoranalysis.experiment.task.InputTypesExpected;
import org.anchoranalysis.feature.bean.list.FeatureList;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.feature.calc.FeatureInitParams;
import org.anchoranalysis.feature.calc.results.ResultsVector;
import org.anchoranalysis.feature.list.NamedFeatureStore;
import org.anchoranalysis.feature.session.FeatureSession;
import org.anchoranalysis.feature.session.calculator.FeatureCalculatorMulti;
import org.anchoranalysis.feature.shared.SharedFeaturesInitParams;
import org.anchoranalysis.image.bean.provider.HistogramProvider;
import org.anchoranalysis.image.feature.histogram.FeatureInputHistogram;
import org.anchoranalysis.image.histogram.Histogram;
import org.anchoranalysis.image.init.ImageInitParams;
import org.anchoranalysis.image.io.histogram.HistogramCSVReader;
import org.anchoranalysis.image.io.input.ImageInitParamsFactory;
import org.anchoranalysis.io.csv.reader.CSVReaderException;
import org.anchoranalysis.io.input.FileInput;
import org.anchoranalysis.io.output.bound.BoundIOContext;

/**
 * 1. Reads a histogram from a CSV file
 * 2. Extracts some features (FeatureHistogram)
 * 3. Writes each feature in a KeyValueParams XML model
 * 
 * @author FEEHANO
 *
 */
public class ExportFeaturesHistogramTask extends ExportFeaturesStoreTask<FileInput,FeatureInputHistogram> {

	/** The name of the input histogram made available to histogramProvider */
	private static final String HISTOGRAM_INPUT_NAME_IN_PROVIDER = "input";
	
	// START BEAN PROPERTIES
	/**
	 * If non-NULL, a histogram is extracted from this provider rather than the histogram from the inputted CSV.
	 * 
	 * The histogram from the inputted CSV is available in the SharedObjects as "input".
	 * 
	 * In this way histogramProvider can be used as a type of function around the original histogram.
	 */
	@BeanField @OptionalBean
	private HistogramProvider histogramProvider;
	// END BEAN PROPERTIES

	public ExportFeaturesHistogramTask() {
		super("histogram");
	}
	
	@Override
	public InputTypesExpected inputTypesExpected() {
		return new InputTypesExpected(FileInput.class);
	}
		
	@Override
	protected ResultsVector calcResultsVectorForInputObject(
		FileInput inputObject,
		NamedFeatureStore<FeatureInputHistogram> featureStore,
		BoundIOContext context
	) throws FeatureCalcException {

		// Reads histogram from file-system
		try {
			Histogram hist = readHistogramFromCsv(inputObject);

			if (histogramProvider!=null) {
				hist = filterHistogramFromProvider(hist, context);
			}
			
			ResultsVector rv = calcFeatures(
				hist,
				featureStore.deepCopy().listFeatures(),
				context.getLogger()
			);
			
			// Exports results as a KeyValueParams
			KeyValueParamsExporter.export(
				featureStore.createFeatureNames(),
				rv,
				context
			);
		
			return rv;
			
		} catch (CSVReaderException | BeanDuplicateException | InitException | OperationFailedException e) {
			throw new FeatureCalcException(e);
		}
	}
	
	private Histogram filterHistogramFromProvider( Histogram inputtedHist, BoundIOContext context ) throws OperationFailedException {
		
		HistogramProvider histProviderDup = histogramProvider.duplicateBean();
		
		try {
			histProviderDup.initRecursive(
				createImageInitParmas(inputtedHist, context),
				context.getLogger()
			);
			
			return histProviderDup.create();
		} catch (CreateException | InitException | OperationFailedException e) {
			throw new OperationFailedException("Cannot retrieve a histogram from histogramProvider", e);
		}
	}
	
	private ImageInitParams createImageInitParmas( Histogram inputtedHist, BoundIOContext context ) throws OperationFailedException {
		// Create a shared-objects and initialise
		ImageInitParams paramsInit = ImageInitParamsFactory.create(context);
		paramsInit.getHistogramCollection().add(HISTOGRAM_INPUT_NAME_IN_PROVIDER, new IdentityOperation<>(inputtedHist) );
		return paramsInit;
	}

	private ResultsVector calcFeatures( Histogram hist, FeatureList<FeatureInputHistogram> features, LogErrorReporter logErrorReporter ) throws InitException, FeatureCalcException {
		SharedFeaturesInitParams initParams = SharedFeaturesInitParams.create( logErrorReporter );
		
		 FeatureCalculatorMulti<FeatureInputHistogram> session = FeatureSession.with(
			features,
			new FeatureInitParams(),
			initParams.getSharedFeatureSet(),
			logErrorReporter
		);
		
		return session.calc(
			new FeatureInputHistogram(hist, null)
		);
	}

	private static Histogram readHistogramFromCsv( FileInput input ) throws CSVReaderException {
		File file = input.getFile();
		
		if (!file.getName().endsWith(".csv") && !file.getName().endsWith(".CSV")) {
			throw new CSVReaderException("This task expects a .CSV file encoding a histogram as input. The file path must end with .csv or .CSV");
		}
		
		return HistogramCSVReader.readHistogramFromFile( file.toPath() );
	}

	public HistogramProvider getHistogramProvider() {
		return histogramProvider;
	}

	public void setHistogramProvider(HistogramProvider histogramProvider) {
		this.histogramProvider = histogramProvider;
	}
}
