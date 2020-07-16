/* (C)2020 */
package org.anchoranalysis.plugin.image.task.bean.feature.source;

import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import org.anchoranalysis.bean.NamedBean;
import org.anchoranalysis.bean.error.BeanDuplicateException;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.feature.bean.list.FeatureList;
import org.anchoranalysis.feature.bean.list.FeatureListProvider;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.feature.calc.results.ResultsVector;
import org.anchoranalysis.feature.input.FeatureInput;
import org.anchoranalysis.feature.io.csv.MetadataHeaders;
import org.anchoranalysis.feature.io.csv.StringLabelsForCsvRow;
import org.anchoranalysis.feature.io.csv.name.SimpleName;
import org.anchoranalysis.feature.list.NamedFeatureStore;
import org.anchoranalysis.feature.list.NamedFeatureStoreFactory;
import org.anchoranalysis.feature.name.FeatureNameList;
import org.anchoranalysis.io.error.AnchorIOException;
import org.anchoranalysis.io.input.InputFromManager;
import org.anchoranalysis.io.output.bound.BoundIOContext;
import org.anchoranalysis.plugin.image.task.feature.GenerateHeadersForCSV;
import org.anchoranalysis.plugin.image.task.feature.SharedStateExportFeatures;

/**
 * Base class for exporting features, where features are calculated per-image using a
 * NamedFeatureStore
 *
 * @author Owen Feehan
 * @param T input-manager type
 * @param S feature-input type
 */
public abstract class SingleRowPerInput<T extends InputFromManager, S extends FeatureInput>
        extends FeatureSource<T, FeatureList<S>, S> {

    private static final NamedFeatureStoreFactory STORE_FACTORY =
            NamedFeatureStoreFactory.factoryParamsOnly();

    private String firstResultHeader;

    /**
     * Default constructor
     *
     * @param firstResultHeader the first column-name in the CSV file that is outputted
     */
    public SingleRowPerInput(String firstResultHeader) {
        super();
        this.firstResultHeader = firstResultHeader;
    }

    @Override
    public SharedStateExportFeatures<FeatureList<S>> createSharedState(
            MetadataHeaders metadataHeaders,
            List<NamedBean<FeatureListProvider<S>>> features,
            BoundIOContext context)
            throws CreateException {
        try {
            NamedFeatureStore<S> store = STORE_FACTORY.createNamedFeatureList(features);
            return new SharedStateExportFeatures<>(
                    metadataHeaders,
                    store.createFeatureNames(),
                    () -> store.deepCopy().listFeatures(),
                    context);
        } catch (AnchorIOException e) {
            throw new CreateException(e);
        }
    }

    @Override
    public GenerateHeadersForCSV headers() {
        return new GenerateHeadersForCSV(new String[] {firstResultHeader}, Optional.empty());
    }

    @Override
    public void calcAllResultsForInput(
            T input,
            BiConsumer<StringLabelsForCsvRow, ResultsVector> addResultsFor,
            FeatureList<S> featureSourceSupplier,
            FeatureNameList featureNames,
            Optional<String> groupGeneratorName,
            BoundIOContext context)
            throws OperationFailedException {
        try {
            ResultsVector results =
                    calcResultsVectorForInputObject(
                            input, featureSourceSupplier, featureNames, context);

            addResultsFor.accept(
                    identifierFor(input.descriptiveName(), groupGeneratorName), results);

        } catch (BeanDuplicateException | FeatureCalcException e) {
            throw new OperationFailedException(e);
        }
    }

    protected abstract ResultsVector calcResultsVectorForInputObject(
            T inputObject,
            FeatureList<S> features,
            FeatureNameList featureNames,
            BoundIOContext context)
            throws FeatureCalcException;

    private static StringLabelsForCsvRow identifierFor(
            String descriptiveName, Optional<String> groupGeneratorName)
            throws OperationFailedException {
        return new StringLabelsForCsvRow(
                Optional.of(new String[] {descriptiveName}),
                groupGeneratorName.map(SimpleName::new));
    }
}
