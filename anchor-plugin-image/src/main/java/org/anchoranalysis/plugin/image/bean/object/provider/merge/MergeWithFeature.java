/*-
 * #%L
 * anchor-plugin-image
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
/* (C)2020 */
package org.anchoranalysis.plugin.image.bean.object.provider.merge;

import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.OptionalFactory;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.graph.EdgeTypeWithVertices;
import org.anchoranalysis.core.log.MessageLogger;
import org.anchoranalysis.image.bean.provider.ObjectCollectionProvider;
import org.anchoranalysis.image.extent.ImageResolution;
import org.anchoranalysis.image.feature.evaluator.PayloadCalculator;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.plugin.image.object.merge.MergeGraph;
import org.anchoranalysis.plugin.image.object.merge.ObjectVertex;
import org.anchoranalysis.plugin.image.object.merge.condition.AndCondition;
import org.anchoranalysis.plugin.image.object.merge.condition.NeighborhoodCondition;
import org.anchoranalysis.plugin.image.object.merge.condition.UpdatableBeforeCondition;
import org.anchoranalysis.plugin.image.object.merge.condition.WrapAsUpdatable;
import org.anchoranalysis.plugin.image.object.merge.priority.AssignPriority;
import org.anchoranalysis.plugin.image.object.merge.priority.PrioritisedVertex;

/**
 * Base class for object-merging strategies that involve calculating a feature.
 *
 * @author Owen Feehan
 */
public abstract class MergeWithFeature extends MergeWithOptionalDistanceConstraint {

    // START BEAN PROPERTIES
    /**
     * Requires for any potential merge that the bounding-boxes of the two objects must intersect or
     * touch
     */
    @BeanField @Getter @Setter private boolean requireBBoxNeighbors = true;

    /**
     * Requires the object-masks to touch. More expensive to calculate than the {@code
     * requireBBoxNeighbors} condition.
     */
    @BeanField @Getter @Setter private boolean requireTouching = true;

    /**
     * Saves all objects that are inputs to the merge, outputs from the merge, or intermediate
     * merges along the way
     */
    @BeanField @OptionalBean @Getter @Setter private ObjectCollectionProvider objectsSave;
    // END BEAN PROPERTIES

    @Override
    public ObjectCollection createFromObjects(ObjectCollection objectsSource)
            throws CreateException {

        Optional<ObjectCollection> saveObjects = OptionalFactory.create(objectsSave);
        saveObjects.ifPresent(objects -> objects.addAll(objectsSource));

        getLogger()
                .messageLogger()
                .logFormatted("There are %d input objects", objectsSource.size());

        try {
            ObjectCollection merged =
                    mergeMultiplex(objectsSource, a -> mergeConnectedComponents(a, saveObjects));

            getLogger().messageLogger().logFormatted("There are %d final objects", merged.size());

            return merged;

        } catch (OperationFailedException e) {
            throw new CreateException(e);
        }
    }

    /** Determines the payload for any given or potential vertex */
    protected abstract PayloadCalculator createPayloadCalculator() throws OperationFailedException;

    /** Determines the priority (and selection criteria) used to allow merges between neighbors */
    protected abstract AssignPriority createPrioritizer() throws OperationFailedException;

    /** Is the payload considered in making decisions? (iff FALSE, payload of nodes is irrelvant) */
    protected abstract boolean isPlayloadUsed();

    /**
     * Tries to merge objects
     *
     * @param objects objects to be merged
     * @param saveObjects if defined, all merged objects are added to this collection
     * @return
     * @throws OperationFailedException
     */
    private ObjectCollection mergeConnectedComponents(
            ObjectCollection objects, Optional<ObjectCollection> saveObjects)
            throws OperationFailedException {

        MessageLogger logger = getLogger().messageLogger();

        MergeGraph graph;
        try {
            graph = createGraph(objects, calcResOptional());
        } catch (CreateException e) {
            throw new OperationFailedException(e);
        }

        logger.log("\nBefore");
        graph.logGraphDescription();

        while (tryMerge(graph, saveObjects)) {
            // NOTHING TO DO, we just keep merging until we cannot merge any moore
        }

        logger.log("After");
        graph.logGraphDescription();

        return graph.verticesAsObjects();
    }

    /**
     * Search for suitable merges in graph, and merges them
     *
     * @param graph the graph containing objects that can maybe be merged
     * @param saveObjects if defined, all merged objects are added to this collection
     * @return
     * @throws OperationFailedException
     */
    private boolean tryMerge(MergeGraph graph, Optional<ObjectCollection> saveObjects)
            throws OperationFailedException {

        // Find the edge with the best improvement
        EdgeTypeWithVertices<ObjectVertex, PrioritisedVertex> edgeToMerge = graph.findMaxPriority();

        if (edgeToMerge == null) {
            return false;
        }

        // When we decide to merge, we save the merged object
        saveObjects.ifPresent(so -> so.add(edgeToMerge.getEdge().getOmWithFeature().getObject()));

        graph.merge(edgeToMerge);

        return true;
    }

    private MergeGraph createGraph(ObjectCollection objects, Optional<ImageResolution> res)
            throws CreateException {

        try {
            MergeGraph graph =
                    new MergeGraph(
                            createPayloadCalculator(),
                            beforeConditions(),
                            res,
                            createPrioritizer(),
                            getLogger(),
                            isPlayloadUsed());

            graph.addObjectsToGraph(objects);

            return graph;

        } catch (OperationFailedException e) {
            throw new CreateException(e);
        }
    }

    private UpdatableBeforeCondition beforeConditions() {
        return new AndCondition(
                new WrapAsUpdatable(maybeDistanceCondition()),
                new NeighborhoodCondition(requireBBoxNeighbors, requireTouching));
    }
}
