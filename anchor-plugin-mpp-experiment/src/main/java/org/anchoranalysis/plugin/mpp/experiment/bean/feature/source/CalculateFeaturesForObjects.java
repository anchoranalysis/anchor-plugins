package org.anchoranalysis.plugin.mpp.experiment.bean.feature.source;

import java.util.Optional;
import java.util.function.Function;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.log.Logger;
import org.anchoranalysis.feature.calculate.NamedFeatureCalculateException;
import org.anchoranalysis.feature.energy.EnergyStack;
import org.anchoranalysis.feature.input.FeatureInput;
import org.anchoranalysis.feature.io.csv.StringLabelsForCsvRow;
import org.anchoranalysis.feature.session.calculator.FeatureCalculatorMulti;
import org.anchoranalysis.image.feature.session.FeatureTableCalculator;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.image.stack.DisplayStack;
import org.anchoranalysis.plugin.image.feature.bean.object.combine.CombineObjectsForFeatures;
import org.anchoranalysis.plugin.image.feature.object.ListWithThumbnails;
import org.anchoranalysis.plugin.image.task.feature.InputProcessContext;
import org.anchoranalysis.plugin.image.task.feature.ResultsVectorWithThumbnail;
import org.anchoranalysis.plugin.image.thumbnail.ThumbnailBatch;
import org.anchoranalysis.plugin.mpp.experiment.feature.source.InitParamsWithEnergyStack;

public class CalculateFeaturesForObjects<T extends FeatureInput> {

    private final CombineObjectsForFeatures<T> table;
    private final FeatureCalculatorMulti<T> calculator;
    
    /**
     * iff TRUE no exceptions are thrown when an error occurs, but rather a message is written to
     * the log
     */
    private final boolean suppressErrors;

    private final InputProcessContext<FeatureTableCalculator<T>> context;
    
    public CalculateFeaturesForObjects(CombineObjectsForFeatures<T> table, InitParamsWithEnergyStack initParams, boolean suppressErrors, InputProcessContext<FeatureTableCalculator<T>> context) throws OperationFailedException {
        this.table = table;
        this.calculator = startCalculator(context.getRowSource(), initParams, context.getLogger());
        this.suppressErrors = suppressErrors;
        this.context = context;
    }
    
    public void calculateFeaturesForObjects(
            ObjectCollection objects,
            EnergyStack energyStack,
            Function<T, StringLabelsForCsvRow> identifierFromInput)
            throws OperationFailedException {
        try {
                ListWithThumbnails<T,ObjectCollection> inputs =
                        table.deriveInputsStartBatch(
                                objects,
                                energyStack,
                                context.isThumbnailsEnabled(),
                                context.getLogger());
    
                calculateManyFeaturesInto(inputs, identifierFromInput);


        } catch (CreateException | OperationFailedException e) {
            throw new OperationFailedException(e);
        }
    }
    
    /**
     * Calculates a bunch of features with an objectID (unique) and a groupID and adds them to the
     * stored-results
     *
     * <p>The stored-results also have an additional first-column with the ID.
     *
     * @param listInputs a list of parameters. Each parameters creates a new result (e.g. a new row
     *     in a feature-table)
     * @throws OperationFailedException
     */
    private void calculateManyFeaturesInto(
            ListWithThumbnails<T,ObjectCollection> listInputs, Function<T, StringLabelsForCsvRow> labelsForInput)
            throws OperationFailedException {

        try {
            for (int i = 0; i < listInputs.size(); i++) {

                T input = listInputs.get(i);

                context.getLogger()
                        .messageLogger()
                        .logFormatted(
                                "Calculating input %d of %d: %s",
                                i + 1, listInputs.size(), input.toString());

                context.addResultsFor(
                        labelsForInput.apply(input),
                        new ResultsVectorWithThumbnail(
                                calculator.calculate(
                                        input, context.getLogger().errorReporter(), suppressErrors),
                                thumbnailForInput(input, listInputs.getThumbnailBatch(), context.isThumbnailsEnabled() )));
            }

        } catch (NamedFeatureCalculateException | CreateException e) {
            throw new OperationFailedException(e);
        }
    }
    
    private Optional<DisplayStack> thumbnailForInput(T input, Optional<ThumbnailBatch<ObjectCollection>> thumbnailBatch, boolean thumbnailsEnabled) throws CreateException {
        if (thumbnailsEnabled && thumbnailBatch.isPresent()) {
            return Optional.of( thumbnailBatch.get().thumbnailFor( table.objectsForThumbnail(input) ) );
        } else {
            return Optional.empty();
        }
    }
    
    public String uniqueIdentifierFor(T input) {
        return table.uniqueIdentifierFor(input);
    }

    public Logger getLogger() {
        return context.getLogger();
    }
    
    private static <T extends FeatureInput> FeatureCalculatorMulti<T> startCalculator(
            FeatureTableCalculator<T> calculator,
            InitParamsWithEnergyStack initParams,
            Logger logger)
            throws OperationFailedException {

        try {
            calculator.start(
                    initParams.getImageInit(), Optional.of(initParams.getEnergyStack()), logger);
        } catch (InitException e) {
            throw new OperationFailedException(e);
        }

        return calculator;
    }
}
