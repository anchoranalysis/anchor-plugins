/*-
 * #%L
 * anchor-plugin-image-task
 * %%
 * Copyright (C) 2010 - 2020 Owen Feehan, ETH Zurich, University of Zurich, Hoffmann-La Roche
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

package org.anchoranalysis.plugin.image.task.bean.feature.source;

import java.io.File;
import java.nio.file.Path;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.bean.exception.BeanDuplicateException;
import org.anchoranalysis.bean.xml.exception.ProvisionFailedException;
import org.anchoranalysis.core.exception.InitializeException;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.core.format.NonImageFileFormat;
import org.anchoranalysis.core.log.Logger;
import org.anchoranalysis.experiment.task.InputTypesExpected;
import org.anchoranalysis.feature.bean.list.FeatureList;
import org.anchoranalysis.feature.calculate.NamedFeatureCalculateException;
import org.anchoranalysis.feature.calculate.bound.FeatureCalculatorMulti;
import org.anchoranalysis.feature.initialization.FeatureInitialization;
import org.anchoranalysis.feature.initialization.FeatureRelatedInitialization;
import org.anchoranalysis.feature.results.ResultsVector;
import org.anchoranalysis.feature.session.FeatureSession;
import org.anchoranalysis.image.bean.nonbean.init.ImageInitialization;
import org.anchoranalysis.image.bean.provider.HistogramProvider;
import org.anchoranalysis.image.feature.input.FeatureInputHistogram;
import org.anchoranalysis.image.io.ImageInitializationFactory;
import org.anchoranalysis.image.io.histogram.input.HistogramCSVReader;
import org.anchoranalysis.io.input.csv.CSVReaderException;
import org.anchoranalysis.io.input.file.FileInput;
import org.anchoranalysis.io.output.outputter.InputOutputContext;
import org.anchoranalysis.math.histogram.Histogram;
import org.anchoranalysis.plugin.image.task.feature.FeatureCalculationContext;
import org.anchoranalysis.plugin.image.task.feature.ResultsVectorWithThumbnail;

/**
 * Each input-file describes a histogram that produces one row of features.
 *
 * <p>Optionally, additionally extracted features are written in a XML model.
 *
 * @author Owen Feehan
 */
public class FromHistogram extends SingleRowPerInput<FileInput, FeatureInputHistogram> {

    /** The name of the input histogram made available to {@code histogram} */
    private static final String HISTOGRAM_INPUT_NAME_IN_PROVIDER = "input";

    // START BEAN PROPERTIES
    /**
     * If non-null, a histogram is extracted from this provider rather than the histogram from the
     * inputted CSV.
     *
     * <p>The histogram from the inputted CSV is available in the SharedObjects as "input".
     *
     * <p>In this way, {@code histogram} can approximate a function of the original histogram.
     */
    @BeanField @OptionalBean @Getter @Setter private HistogramProvider histogram;
    // END BEAN PROPERTIES

    public FromHistogram() {
        super("histogram");
    }

    @Override
    public InputTypesExpected inputTypesExpected() {
        return new InputTypesExpected(FileInput.class);
    }

    @Override
    public boolean includeGroupInExperiment(boolean groupGeneratorDefined) {
        return groupGeneratorDefined;
    }

    @Override
    protected ResultsVectorWithThumbnail calculateResultsForInput(
            FileInput input, FeatureCalculationContext<FeatureList<FeatureInputHistogram>> context)
            throws NamedFeatureCalculateException {

        return new ResultsVectorWithThumbnail(() -> calculateFeature(input, context));
    }

    private ResultsVector calculateFeature(
            FileInput input, FeatureCalculationContext<FeatureList<FeatureInputHistogram>> context)
            throws OperationFailedException {
        try {
            Histogram histogramRead = readHistogramFromCSV(input);

            if (histogram != null) {
                histogramRead = filterHistogramFromProvider(histogramRead, context.getContext());
            }

            return createCalculator(
                            context.getFeatureSource(),
                            context.getModelDirectory(),
                            context.getLogger())
                    .calculate(new FeatureInputHistogram(histogramRead, Optional.empty()));
        } catch (CSVReaderException
                | BeanDuplicateException
                | OperationFailedException
                | InitializeException
                | NamedFeatureCalculateException e) {
            throw new OperationFailedException(e);
        }
    }

    private Histogram filterHistogramFromProvider(
            Histogram inputtedHistogram, InputOutputContext context)
            throws OperationFailedException {

        HistogramProvider providerDuplicated = histogram.duplicateBean();

        try {
            providerDuplicated.initializeRecursive(
                    createImageInitialization(inputtedHistogram, context), context.getLogger());

            return providerDuplicated.get();
        } catch (ProvisionFailedException | InitializeException | OperationFailedException e) {
            throw new OperationFailedException("Cannot retrieve a histogram from the provider", e);
        }
    }

    private ImageInitialization createImageInitialization(
            Histogram inputtedHist, InputOutputContext context) throws OperationFailedException {
        // Create a shared-objects and initialise
        ImageInitialization initialization = ImageInitializationFactory.create(context);
        initialization.histograms().add(HISTOGRAM_INPUT_NAME_IN_PROVIDER, () -> inputtedHist);
        return initialization;
    }

    private static FeatureCalculatorMulti<FeatureInputHistogram> createCalculator(
            FeatureList<FeatureInputHistogram> features, Path modelDirectory, Logger logger)
            throws InitializeException {
        return FeatureSession.with(
                features,
                new FeatureInitialization(),
                FeatureRelatedInitialization.create(logger, modelDirectory).getSharedFeatures(),
                logger);
    }

    private static Histogram readHistogramFromCSV(FileInput input) throws CSVReaderException {
        File file = input.getFile();

        if (NonImageFileFormat.CSV.matches(file.getName())) {
            return HistogramCSVReader.readHistogramFromFile(file.toPath());
        } else {
            throw new CSVReaderException(
                    "This task expects a CSV file encoding a histogram as input. The file path must end with "
                            + NonImageFileFormat.CSV.extensionWithPeriod());
        }
    }

    @Override
    protected Optional<String[]> additionalLabelsFor(FileInput input) {
        return Optional.empty();
    }
}
