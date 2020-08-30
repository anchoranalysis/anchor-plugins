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
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.experiment.task.InputTypesExpected;
import org.anchoranalysis.feature.bean.list.FeatureListProvider;
import org.anchoranalysis.feature.input.FeatureInput;
import org.anchoranalysis.feature.io.csv.LabelHeaders;
import org.anchoranalysis.feature.io.csv.StringLabelsForCsvRow;
import org.anchoranalysis.feature.io.csv.name.CombinedName;
import org.anchoranalysis.feature.io.csv.name.MultiName;
import org.anchoranalysis.feature.io.csv.name.SimpleName;
import org.anchoranalysis.feature.list.NamedFeatureStoreFactory;
import org.anchoranalysis.image.bean.provider.ObjectCollectionProvider;
import org.anchoranalysis.image.feature.object.input.FeatureInputSingleObject;
import org.anchoranalysis.image.feature.session.FeatureTableCalculator;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.io.output.bound.BoundIOContext;
import org.anchoranalysis.mpp.io.input.MultiInput;
import org.anchoranalysis.plugin.image.feature.bean.object.combine.CombineObjectsForFeatures;
import org.anchoranalysis.plugin.image.task.bean.feature.source.FeatureSource;
import org.anchoranalysis.plugin.image.task.feature.CalculateFeaturesForObjects;
import org.anchoranalysis.plugin.image.task.feature.GenerateLabelHeadersForCSV;
import org.anchoranalysis.plugin.image.task.feature.InitParamsWithEnergyStack;
import org.anchoranalysis.plugin.image.task.feature.InputProcessContext;
import org.anchoranalysis.plugin.image.task.feature.SharedStateExportFeatures;
import org.anchoranalysis.plugin.mpp.bean.define.DefineOutputterMPPWithEnergy;

/**
 * Extracts features for each object in a collection.
 *
 * <p>
 *
 * <ol>
 *   <li>All input are aggregated into groups (with the name of the {@link ObjectCollectionProvider}
 *       added to the end)
 *   <li>For each input, the <code>define</code> is applied and one or more {@link ObjectCollection}
 *       are extracted
 *   <li>These objects are added to the appropriate {@link ObjectCollection} associated with each
 *       group
 * </ol>
 *
 * <p>Note unlike other feature-sources, the group here is not only what is returned by the <code>
 * group</code> generator in the super-class, but also includes the name of the {@link
 * ObjectCollectionProvider} if there is more than one.
 *
 * <p>TODO does this need to be a MultiInput and dependent on MPP? Can it be moved to
 * anchor-plugin-image-task??
 *
 * @param <T> the feature input-type supported by the {@link FeatureTableCalculator}
 */
public class FromObjects<T extends FeatureInput>
        extends FeatureSource<MultiInput, FeatureTableCalculator<T>, FeatureInputSingleObject> {

    private static final NamedFeatureStoreFactory STORE_FACTORY =
            NamedFeatureStoreFactory.bothNameAndParams();

    private static final String[] NON_GROUP_HEADERS =
            new String[] {"image", "unique_pixel_in_object"};

    private static final String ADDITONAL_GROUP_HEADER = "object_collection";

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter
    private DefineOutputterMPPWithEnergy define = new DefineOutputterMPPWithEnergy();

    @BeanField @Getter @Setter
    private List<NamedBean<ObjectCollectionProvider>> objects = new ArrayList<>();

    @BeanField @Getter @Setter private CombineObjectsForFeatures<T> combine;

    @BeanField @Getter @Setter private boolean suppressErrors = false;
    // END BEAN PROPERTIES

    @Override
    public InputTypesExpected inputTypesExpected() {
        return new InputTypesExpected(MultiInput.class);
    }

    @Override
    public SharedStateExportFeatures<FeatureTableCalculator<T>> createSharedState(
            LabelHeaders metadataHeaders,
            List<NamedBean<FeatureListProvider<FeatureInputSingleObject>>> features,
            BoundIOContext context)
            throws CreateException {
        try {
            FeatureTableCalculator<T> tableCalculator =
                    combine.createFeatures(features, STORE_FACTORY, suppressErrors);
            return SharedStateExportFeatures.createForFeatures(tableCalculator,  metadataHeaders, context);
        } catch (InitException e) {
            throw new CreateException(e);
        }
    }

    @Override
    public void processInput(
            MultiInput input, InputProcessContext<FeatureTableCalculator<T>> context)
            throws OperationFailedException {
        define.processInput(
                input,
                context.getContext(),
                (initParams, energyStack) ->
                        calculateFeaturesForImage(
                                input.descriptiveName(),
                                new InitParamsWithEnergyStack(initParams, energyStack),
                                context));
    }

    @Override
    public GenerateLabelHeadersForCSV headers() {
        return new GenerateLabelHeadersForCSV(
                NON_GROUP_HEADERS,
                moreThanOneProvider() ? Optional.of(ADDITONAL_GROUP_HEADER) : Optional.empty());
    }

    /**
     * If either a group-generator is defined or there's more than one provider, then groups should
     * be included
     */
    @Override
    public boolean includeGroupInExperiment(boolean groupGeneratorDefined) {
        return groupGeneratorDefined || moreThanOneProvider();
    }

    private boolean moreThanOneProvider() {
        return objects.size() > 1;
    }

    private int calculateFeaturesForImage(
            String descriptiveName,
            InitParamsWithEnergyStack initParams,
            InputProcessContext<FeatureTableCalculator<T>> context)
            throws OperationFailedException {

        CalculateFeaturesForObjects<T> objectsCalculator = new CalculateFeaturesForObjects<>(
                combine,
                initParams,
                suppressErrors,
                context
        );
        
        CalculateFeaturesFromProvider<T> fromProviderCalculator = new CalculateFeaturesFromProvider<>(objectsCalculator, initParams);
        processAllProviders(
                descriptiveName, context.getGroupGeneratorName(), fromProviderCalculator);

        // Arbitrary, we need a return-type
        return 0;
    }

    private void processAllProviders(
            String imageIdentifier,
            Optional<String> groupGeneratorName,
            CalculateFeaturesFromProvider<T> calculator)
            throws OperationFailedException {

        // For every object-collection-provider
        for (NamedBean<ObjectCollectionProvider> namedBean : objects) {
            calculator.processProvider(
                    namedBean.getValue(),
                    input ->
                            identifierFor(
                                    imageIdentifier,
                                    combine.uniqueIdentifierFor(input),
                                    groupGeneratorName,
                                    namedBean.getName()));
        }
    }

    private StringLabelsForCsvRow identifierFor(
            String imageIdentifier,
            String objectIdentifier,
            Optional<String> groupGeneratorName,
            String providerName) {
        return new StringLabelsForCsvRow(
                Optional.of(new String[] {imageIdentifier, objectIdentifier}),
                createGroupName(groupGeneratorName, providerName));
    }

    private Optional<MultiName> createGroupName(
            Optional<String> groupGeneratorName, String providerName) {
        if (moreThanOneProvider()) {
            if (groupGeneratorName.isPresent()) {
                return Optional.of(new CombinedName(groupGeneratorName.get(), providerName));
            } else {
                return Optional.of(new SimpleName(providerName));
            }
        } else {
            return groupGeneratorName.map(SimpleName::new);
        }
    }
}
