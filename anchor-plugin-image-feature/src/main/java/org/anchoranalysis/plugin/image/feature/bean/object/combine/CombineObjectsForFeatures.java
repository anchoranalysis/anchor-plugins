/*-
 * #%L
 * anchor-plugin-image-feature
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

package org.anchoranalysis.plugin.image.feature.bean.object.combine;

import java.util.List;
import java.util.Optional;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.anchoranalysis.bean.AnchorBean;
import org.anchoranalysis.bean.NamedBean;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.DefaultInstance;
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.core.exception.CreateException;
import org.anchoranalysis.core.exception.InitializeException;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.core.functional.StreamableCollection;
import org.anchoranalysis.core.log.Logger;
import org.anchoranalysis.feature.bean.list.FeatureListProvider;
import org.anchoranalysis.feature.energy.EnergyStack;
import org.anchoranalysis.feature.input.FeatureInput;
import org.anchoranalysis.feature.store.NamedFeatureStore;
import org.anchoranalysis.feature.store.NamedFeatureStoreFactory;
import org.anchoranalysis.image.bean.interpolator.Interpolator;
import org.anchoranalysis.image.feature.calculator.FeatureTableCalculator;
import org.anchoranalysis.image.feature.input.FeatureInputSingleObject;
import org.anchoranalysis.image.voxel.object.ObjectCollection;
import org.anchoranalysis.plugin.image.bean.thumbnail.object.OutlinePreserveRelativeSize;
import org.anchoranalysis.plugin.image.bean.thumbnail.object.ThumbnailFromObjects;
import org.anchoranalysis.plugin.image.feature.object.ListWithThumbnails;
import org.anchoranalysis.spatial.box.BoundingBox;

/**
 * A way to combine (or not combine) objects so that they provide a feature-table.
 *
 * <p>Columns in the feature-table always represent features.
 *
 * <p>A row may represent a single object, or a pair of objects, or any other derived inputs from an
 * object-collection, depending on the implementation of the sub-class.
 *
 * @author Owen Feehan
 * @param <T> type of feature used in the table
 */
@NoArgsConstructor
public abstract class CombineObjectsForFeatures<T extends FeatureInput>
        extends AnchorBean<CombineObjectsForFeatures<T>> {

    // START BEAN PROPERTIES
    /**
     * Generates a thumbnail representation of one or more combined objects, as form a single input.
     *
     * <p>If not set, a thumbnail will be created with an outline around selected and unselected
     * objects.
     */
    @BeanField @Getter @Setter @OptionalBean private ThumbnailFromObjects thumbnail;

    /** Interpolator used to resize images in thumbnail generation. */
    @BeanField @Getter @Setter @DefaultInstance private Interpolator interpolator;
    // END BEAN PROPERTIES

    /**
     * Create with a specific interpolator.
     *
     * @param interpolator interpolator used to resize images in thumbnail generation.
     */
    protected CombineObjectsForFeatures(Interpolator interpolator) {
        this.interpolator = interpolator;
    }

    /**
     * Creates features that will be applied on the objects. Features should always be duplicated
     * from the input list.
     *
     * @param featuresSingleObject beans defining features to be applied to single-objects
     * @param storeFactory creates as new {@link NamedFeatureStore} as needed
     * @param suppressErrors when true, exceptions aren't thrown when feature-calculations fail, but
     *     rather a log error message is written.
     * @return a calculator for feature tables that may apply various features derived from {@code
     *     featuresSingleObject}
     * @throws CreateException
     */
    public abstract FeatureTableCalculator<T> createFeatures(
            List<NamedBean<FeatureListProvider<FeatureInputSingleObject>>> featuresSingleObject,
            NamedFeatureStoreFactory storeFactory,
            boolean suppressErrors)
            throws CreateException, InitializeException;

    /** Generates a unique identifier for a particular input */
    public abstract String uniqueIdentifierFor(T input);

    /**
     * Derives a list of inputs (i.e. rows in a feature table) and starts a batch of related
     * thumbnail generation.
     *
     * @param objects the objects from which inputs are derived
     * @param energyStack energy-stack used during feature calculation
     * @param thumbnailsEnabled whether thumbnail-generation is enabled
     * @param logger logger
     * @return the list of inputs
     * @throws CreateException
     */
    public ListWithThumbnails<T, ObjectCollection> deriveInputsStartBatch(
            ObjectCollection objects,
            EnergyStack energyStack,
            boolean thumbnailsEnabled,
            Logger logger)
            throws CreateException {

        List<T> inputs = startBatchDeriveInputs(objects, energyStack, logger);

        if (inputs.isEmpty()) {
            return new ListWithThumbnails<>(inputs);
        }

        if (thumbnailsEnabled) {

            if (thumbnail == null) {
                thumbnail =
                        OutlinePreserveRelativeSize.createToColorUnselectedObjects(interpolator);
            }

            try {
                return new ListWithThumbnails<>(
                        inputs,
                        thumbnail.start(
                                objects,
                                scaledBoundingBoxes(inputs),
                                Optional.of(energyStack.asStack())));
            } catch (OperationFailedException e) {
                throw new CreateException(e);
            }
        } else {
            return new ListWithThumbnails<>(inputs);
        }
    }

    /**
     * Selects objects from an input that will be used for thumbnail generation
     *
     * @param input the input
     * @return the thumbnail
     */
    public abstract ObjectCollection objectsForThumbnail(T input) throws CreateException;

    /**
     * Derives a list of inputs from an object-collection
     *
     * @param objects the object-collection
     * @param energyStack energy-stack used during feature calculation
     * @param logger logger
     * @return the list of inputs
     * @throws CreateException
     */
    protected abstract List<T> startBatchDeriveInputs(
            ObjectCollection objects, EnergyStack energyStack, Logger logger)
            throws CreateException;

    /**
     * Creates a bounding-box that tightly fits the input to a particular table row (could be for
     * one or more objects)
     *
     * @param input the input
     * @return a bounding-box that fully fits around all objects used in input
     */
    protected abstract BoundingBox boundingBoxThatSpansInput(T input);

    private StreamableCollection<BoundingBox> scaledBoundingBoxes(List<T> inputs) {
        return new StreamableCollection<>(
                () -> inputs.stream().map(this::boundingBoxThatSpansInput));
    }
}
