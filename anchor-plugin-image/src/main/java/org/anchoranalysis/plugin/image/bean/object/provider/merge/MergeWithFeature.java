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

package org.anchoranalysis.plugin.image.bean.object.provider.merge;

import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.exception.CreateException;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.core.graph.TypedEdge;
import org.anchoranalysis.core.log.MessageLogger;
import org.anchoranalysis.image.core.dimensions.UnitConverter;
import org.anchoranalysis.image.voxel.object.ObjectCollection;
import org.anchoranalysis.plugin.image.object.merge.MergeGraph;
import org.anchoranalysis.plugin.image.object.merge.ObjectVertex;
import org.anchoranalysis.plugin.image.object.merge.PayloadCalculator;
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

    @Override
    public ObjectCollection createFromObjects(ObjectCollection objectsSource)
            throws CreateException {

        getLogger()
                .messageLogger()
                .logFormatted("There are %d input objects", objectsSource.size());

        try {
            ObjectCollection merged =
                    mergeMultiplex(objectsSource, this::mergeConnectedComponents);

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

    /** Is the payload considered in making decisions? (iff false, payload of nodes is irrelvant) */
    protected abstract boolean isPlayloadUsed();

    /**
     * Tries to merge objects
     *
     * @param objects objects to be merged
     * @return
     * @throws OperationFailedException
     */
    private ObjectCollection mergeConnectedComponents(ObjectCollection objects) throws OperationFailedException {

        MessageLogger logger = getLogger().messageLogger();

        MergeGraph graph;
        try {
            graph = createGraph(objects, unitConvertOptional());
        } catch (CreateException e) {
            throw new OperationFailedException(e);
        }

        logger.log("\nBefore");
        graph.logGraphDescription();

        while (tryMerge(graph)) {
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
     * @return
     * @throws OperationFailedException
     */
    private boolean tryMerge(MergeGraph graph)
            throws OperationFailedException {

        // Find the edge with the best improvement
        TypedEdge<ObjectVertex, PrioritisedVertex> edgeToMerge = graph.findMaxPriority();

        if (edgeToMerge == null) {
            return false;
        }

        graph.merge(edgeToMerge);

        return true;
    }

    private MergeGraph createGraph(ObjectCollection objects, Optional<UnitConverter> unitConverter)
            throws CreateException {

        try {
            MergeGraph graph =
                    new MergeGraph(
                            createPayloadCalculator(),
                            beforeConditions(),
                            unitConverter,
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
