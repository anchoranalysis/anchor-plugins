/*-
 * #%L
 * anchor-plugin-mpp-experiment
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

package org.anchoranalysis.plugin.mpp.experiment.bean.feature.source;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.NamedBean;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.exception.CreateException;
import org.anchoranalysis.core.exception.InitializeException;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.experiment.io.InitializationContext;
import org.anchoranalysis.experiment.task.InputTypesExpected;
import org.anchoranalysis.feature.bean.list.FeatureListProvider;
import org.anchoranalysis.feature.energy.EnergyStack;
import org.anchoranalysis.feature.input.FeatureInput;
import org.anchoranalysis.feature.io.csv.metadata.LabelHeaders;
import org.anchoranalysis.feature.io.results.FeatureOutputNames;
import org.anchoranalysis.feature.store.NamedFeatureStoreFactory;
import org.anchoranalysis.image.bean.nonbean.init.ImageInitialization;
import org.anchoranalysis.image.bean.provider.ObjectCollectionProvider;
import org.anchoranalysis.image.feature.calculator.FeatureTableCalculator;
import org.anchoranalysis.image.feature.input.FeatureInputSingleObject;
import org.anchoranalysis.image.voxel.object.ObjectCollection;
import org.anchoranalysis.io.input.grouper.InputGrouper;
import org.anchoranalysis.mpp.io.input.MultiInput;
import org.anchoranalysis.plugin.image.feature.bean.object.combine.CombineObjectsForFeatures;
import org.anchoranalysis.plugin.image.task.bean.feature.source.FeatureSource;
import org.anchoranalysis.plugin.image.task.feature.FeatureCalculationContext;
import org.anchoranalysis.plugin.image.task.feature.FeatureExporter;
import org.anchoranalysis.plugin.image.task.feature.FeatureExporterContext;
import org.anchoranalysis.plugin.image.task.feature.InitializationWithEnergyStack;
import org.anchoranalysis.plugin.image.task.feature.LabelHeadersForCSV;
import org.anchoranalysis.plugin.image.task.feature.calculator.CalculateFeaturesForObjects;
import org.anchoranalysis.plugin.mpp.bean.define.DefineOutputterWithEnergy;

/**
 * Extracts features for each object in a collection.
 *
 * <p>
 *
 * <ol>
 *   <li>All input are aggregated into groups (with the name of the {@link ObjectCollectionProvider}
 *       added to the end).
 *   <li>For each input, the <code>define</code> is applied and one or more {@link ObjectCollection}
 *       are extracted.
 *   <li>These objects are added to the appropriate {@link ObjectCollection} associated with each
 *       group.
 * </ol>
 *
 * <p>Note unlike other feature-sources, the group here is not only what is returned by the <code>
 * group</code> generator in the super-class, but also includes the name of the {@link
 * ObjectCollectionProvider} if there is more than one.
 *
 * <p>*
 *
 * <p>The following outputs are produced:
 *
 * <table>
 * <caption></caption>
 * <thead>
 * <tr><th>Output Name</th><th>Default?</th><th>Description</th></tr>
 * </thead>
 * <tbody>
 * <tr><td rowspan="3"><i>outputs from a sub-class of {@link DefineOutputterWithEnergy} as used in {@code define}.</i></td></tr>
 * </tbody>
 * </table>
 *
 * <p>TODO does this need to be a MultiInput and dependent on MPP? Can it be moved to
 * anchor-plugin-image-task??
 *
 * @param <T> the feature input-type supported by the {@link FeatureTableCalculator}
 */
public class FromObjects<T extends FeatureInput>
        extends FeatureSource<MultiInput, FeatureTableCalculator<T>, FeatureInputSingleObject> {

    private static final NamedFeatureStoreFactory STORE_FACTORY =
            NamedFeatureStoreFactory.bothNameAndParameters();

    private static final String[] NON_GROUP_HEADERS =
            new String[] {"image", "unique_pixel_in_object"};

    private static final String ADDITONAL_GROUP_HEADER = "object_collection";

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter
    private DefineOutputterWithEnergy define = new DefineOutputterWithEnergy();

    @BeanField @Getter @Setter
    private List<NamedBean<ObjectCollectionProvider>> objects = new ArrayList<>();

    @BeanField @Getter @Setter private CombineObjectsForFeatures<T> combine;

    /**
     * When true, exceptions aren't thrown when feature-calculations fail, but rather a log error
     * message is written.
     */
    @BeanField @Getter @Setter private boolean suppressErrors = false;
    // END BEAN PROPERTIES

    @Override
    public InputTypesExpected inputTypesExpected() {
        return new InputTypesExpected(MultiInput.class);
    }

    @Override
    public FeatureExporter<FeatureTableCalculator<T>> createExporter(
            LabelHeaders metadataHeaders,
            List<NamedBean<FeatureListProvider<FeatureInputSingleObject>>> features,
            FeatureOutputNames outputNames,
            Optional<InputGrouper> grouper,
            FeatureExporterContext context)
            throws CreateException {
        try {
            FeatureTableCalculator<T> tableCalculator =
                    combine.createFeatures(features, STORE_FACTORY, suppressErrors);
            return FeatureExporter.create(
                    outputNames, tableCalculator, metadataHeaders, grouper, context);
        } catch (InitializeException e) {
            throw new CreateException(e);
        }
    }

    @Override
    public void calculateAndOutput(
            MultiInput input, FeatureCalculationContext<FeatureTableCalculator<T>> context)
            throws OperationFailedException {
        define.processInput(
                input,
                new InitializationContext(context.getContext()),
                (initialization, energyStack) ->
                        calculateFeaturesForImage(
                                input.identifier(), initialization, energyStack, context));
    }

    @Override
    public LabelHeaders headers(boolean groupsEnabled) {
        Optional<String> additionalHeader =
                moreThanOneProvider() ? Optional.of(ADDITONAL_GROUP_HEADER) : Optional.empty();
        return LabelHeadersForCSV.createHeaders(NON_GROUP_HEADERS, additionalHeader, groupsEnabled);
    }

    /**
     * If either a group-generator is defined or there's more than one provider, then groups should
     * be included.
     */
    @Override
    public boolean includeGroupInExperiment(boolean groupGeneratorDefined) {
        return groupGeneratorDefined || moreThanOneProvider();
    }

    private boolean moreThanOneProvider() {
        return objects.size() > 1;
    }

    private Object calculateFeaturesForImage(
            String inputName,
            ImageInitialization initialization,
            EnergyStack energyStack,
            FeatureCalculationContext<FeatureTableCalculator<T>> context)
            throws OperationFailedException {

        InitializationWithEnergyStack initializationEnergy =
                new InitializationWithEnergyStack(initialization, energyStack);

        CalculateFeaturesForObjects<T> objectsCalculator =
                new CalculateFeaturesForObjects<>(
                        combine, initializationEnergy, suppressErrors, context);

        processAllProviders(inputName, objectsCalculator);

        // Arbitrary, we need a return-type
        return null;
    }

    private void processAllProviders(
            String imageIdentifier, CalculateFeaturesForObjects<T> calculator)
            throws OperationFailedException {

        // For every object-collection-provider
        for (NamedBean<ObjectCollectionProvider> namedBean : objects) {
            calculator.calculateForObjects(
                    namedBean.getValue(),
                    (objectIdentifier, groupGeneratorName, index) ->
                            IdentifierHelper.identifierFor(
                                    imageIdentifier,
                                    objectIdentifier,
                                    groupGeneratorName,
                                    namedBean.getName(),
                                    moreThanOneProvider()));
        }
    }
}
