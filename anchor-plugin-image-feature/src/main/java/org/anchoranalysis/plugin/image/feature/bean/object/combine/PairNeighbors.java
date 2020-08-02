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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.NamedBean;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.error.BeanDuplicateException;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.graph.EdgeTypeWithVertices;
import org.anchoranalysis.core.graph.GraphWithEdgeTypes;
import org.anchoranalysis.core.log.Logger;
import org.anchoranalysis.feature.bean.list.FeatureListProvider;
import org.anchoranalysis.feature.list.NamedFeatureStoreFactory;
import org.anchoranalysis.feature.nrg.NRGStackWithParams;
import org.anchoranalysis.image.feature.object.input.FeatureInputPairObjects;
import org.anchoranalysis.image.feature.object.input.FeatureInputSingleObject;
import org.anchoranalysis.image.feature.session.FeatureTableCalculator;
import org.anchoranalysis.image.feature.session.merged.PairsTableCalculator;
import org.anchoranalysis.image.feature.session.merged.MergedPairsFeatures;
import org.anchoranalysis.image.feature.session.merged.MergedPairsInclude;
import org.anchoranalysis.image.feature.stack.FeatureInputStack;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.image.stack.DisplayStack;
import org.anchoranalysis.image.voxel.neighborhood.CreateNeighborGraph;
import org.anchoranalysis.image.voxel.neighborhood.EdgeAdderParameters;

/**
 * Creates a set of features, that creates pairs of neighboring-objects and applies a mixture of
 * single-object features and pair features.
 *
 * <p>Specifically:
 *
 * <ul>
 *   <li>Creates a graph of neighboring-objects
 *   <li>Passes each pair of immediately-neighboring as params, together with their merged object
 * </ul>
 *
 * <p>Features are formed by duplicating the input-feature list (inputfeatures, single-object
 * features only):
 *
 * <pre>
 *   a) First.inputfeatures     applies the features to the first-object in the pair
 *   b) Second.inputfeatures    applies the features to the second-object in the pair
 *   c) Merged.inputfeatures    applies the features to the merged-object
 * </pre>
 *
 * <p>Features (that are not duplicated) are also possible:
 *
 * <pre>
 *   d) Image.					additional single-object features that don't depend on any individual-object, only the image
 *   e) Pair.					additional pair-features ({@link FeatureInputPairObjects})
 * </pre>
 *
 * <p>The column order output is: <code>Image, First, Second, Pair, Merged</code>
 *
 * <p>For <code>First</code> and <code>Second</code>, we use a cache, to avoid repeated
 * calculations.
 *
 * <p>TODO This latter caching-step, could also be avoided in {@link
 * org.anchoranalysis.plugin.image.task.bean.ExportFeaturesTask} due to knowledge of the topology of
 * the repeated features in the resulting output but for now, it's done by putting a cache on each
 * feature.
 *
 * @author Owen Feehan
 */
public class PairNeighbors extends CombineObjectsForFeatures<FeatureInputPairObjects> {

    // START BEAN PROPERTIES
    /**
     * Additional features that are processed on the pair of images (i.e. First+Second as a pair)
     */
    @BeanField @Getter @Setter
    private List<NamedBean<FeatureListProvider<FeatureInputPairObjects>>> featuresPair =
            new ArrayList<>();

    /**
     * Additional features that only depend on the image, so do not need to be replicated for every
     * object.
     */
    @BeanField @Getter @Setter
    private List<NamedBean<FeatureListProvider<FeatureInputStack>>> featuresImage =
            new ArrayList<>();

    /** Include features for the First-object of the pair */
    @BeanField @Getter @Setter private boolean includeFirst = true;

    /** Include features for the Second-object of the pair */
    @BeanField @Getter @Setter private boolean includeSecond = true;

    /** Include features for the Merged-object of the pair */
    @BeanField @Getter @Setter private boolean includeMerged = true;

    /** If true, no overlapping objects are treated as pairs */
    @BeanField @Getter @Setter private boolean avoidOverlappingObjects = false;

    @BeanField @Getter @Setter private boolean do3D = true;
    // END BEAN PROPERTIES

    @Override
    public FeatureTableCalculator<FeatureInputPairObjects> createFeatures(
            List<NamedBean<FeatureListProvider<FeatureInputSingleObject>>> list,
            NamedFeatureStoreFactory storeFactory,
            boolean suppressErrors)
            throws CreateException {

        try {
            FeatureListCustomNameHelper helper = new FeatureListCustomNameHelper(storeFactory);

            MergedPairsFeatures features =
                    new MergedPairsFeatures(
                            helper.copyFeaturesCreateCustomName(featuresImage),
                            helper.copyFeaturesCreateCustomName(list),
                            helper.copyFeaturesCreateCustomName(featuresPair));

            return new PairsTableCalculator(
                    features,
                    new MergedPairsInclude(includeFirst, includeSecond, includeMerged),
                    suppressErrors);

        } catch (BeanDuplicateException | OperationFailedException e) {
            throw new CreateException(e);
        }
    }

    @Override
    public List<FeatureInputPairObjects> startBatchDeriveInputs(
            ObjectCollection objects, NRGStackWithParams nrgStack, Logger logger)
            throws CreateException {

        List<FeatureInputPairObjects> out = new ArrayList<>();

        // We create a neighbor-graph of our input objects
        CreateNeighborGraph<ObjectMask> graphCreator =
                new CreateNeighborGraph<>(new EdgeAdderParameters(avoidOverlappingObjects));
        GraphWithEdgeTypes<ObjectMask, Integer> graphNeighbors =
                graphCreator.createGraph(
                        objects.asList(),
                        Function.identity(),
                        (vector1, vector2, numberVoxels) -> numberVoxels,
                        nrgStack.getNrgStack().getDimensions().getExtent(),
                        do3D);

        // We iterate through every edge in the graph, edges can exist in both directions
        for (EdgeTypeWithVertices<ObjectMask, Integer> edge : graphNeighbors.edgeSetUnique()) {
            out.add(new FeatureInputPairObjects(edge.getNode1(), edge.getNode2(), Optional.of(nrgStack)));
        }

        return out;
    }

    @Override
    public String uniqueIdentifierFor(FeatureInputPairObjects input) {
        return UniqueIdentifierUtilities.forObjectPair(input.getFirst(), input.getSecond());
    }

    @Override
    public Optional<DisplayStack> createThumbailFor(FeatureInputPairObjects input) {
        return Optional.empty();
    }
}
