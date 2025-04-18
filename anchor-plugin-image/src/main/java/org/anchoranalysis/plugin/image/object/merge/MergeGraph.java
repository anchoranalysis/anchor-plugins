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

package org.anchoranalysis.plugin.image.object.merge;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.core.graph.TypedEdge;
import org.anchoranalysis.core.log.Logger;
import org.anchoranalysis.feature.calculate.FeatureCalculationException;
import org.anchoranalysis.image.core.dimensions.UnitConverter;
import org.anchoranalysis.image.voxel.object.ObjectCollection;
import org.anchoranalysis.image.voxel.object.ObjectMask;
import org.anchoranalysis.math.arithmetic.DoubleUtilities;
import org.anchoranalysis.plugin.image.object.merge.condition.UpdatableBeforeCondition;
import org.anchoranalysis.plugin.image.object.merge.priority.AssignPriority;
import org.anchoranalysis.plugin.image.object.merge.priority.PrioritisedVertex;
import org.anchoranalysis.spatial.point.Comparator3i;
import org.anchoranalysis.spatial.point.Point3i;

/**
 * A graph that stores each object as a vertex, where edges represent a neighborhood relation.
 *
 * <p>It potentially allows merges between neighboring vertices.
 *
 * <p>Each vertex has a payload (double) value associated with it, that is a function of the
 * object-mask.
 */
public class MergeGraph {

    /** Calculates a payload value for any {@link ObjectMask}. */
    private PayloadCalculator payloadCalculator;

    /** The underlying graph structure. */
    private NeighborGraph graph;

    /** Logger for graph operations. */
    private GraphLogger logger;

    /** Assigns priority to potential merges. */
    private AssignPriority prioritizer;

    /**
     * Constructor.
     *
     * @param payloadCalculator means to calculate a payload for any object
     * @param beforeCondition condition to check before merging
     * @param unitConverter converts units from voxels to physical measurements and vice-versa
     * @param prioritizer means to assign priority to the merge of any two objects
     * @param logger logger for outputting messages
     * @param logPayload whether to include the payload in logging messages
     */
    public MergeGraph(
            PayloadCalculator payloadCalculator,
            UpdatableBeforeCondition beforeCondition,
            Optional<UnitConverter> unitConverter,
            AssignPriority prioritizer,
            Logger logger,
            boolean logPayload) {
        super();
        this.payloadCalculator = payloadCalculator;
        this.prioritizer = prioritizer;

        graph = new NeighborGraph(beforeCondition, unitConverter);
        this.logger = new GraphLogger(new DescribeGraph(graph, logPayload), logger);
    }

    /**
     * Adds objects to the graph.
     *
     * @param objects the {@link ObjectCollection} to add
     * @return a list of {@link ObjectVertex} objects created from the input objects
     * @throws OperationFailedException if the operation fails
     */
    public List<ObjectVertex> addObjectsToGraph(ObjectCollection objects)
            throws OperationFailedException {

        List<ObjectVertex> listAdded = new ArrayList<>();

        for (int i = 0; i < objects.size(); i++) {

            ObjectVertex vertex = createVertex(objects.get(i));
            graph.addVertex(vertex, listAdded, prioritizer, logger);
            listAdded.add(vertex);
        }

        return listAdded;
    }

    /**
     * Merges two vertices in the graph.
     *
     * @param bestImprovement the edge representing the best merge improvement
     * @return the newly created merged {@link ObjectVertex}
     * @throws OperationFailedException if the merge operation fails
     */
    public ObjectVertex merge(TypedEdge<ObjectVertex, PrioritisedVertex> bestImprovement)
            throws OperationFailedException {

        Set<ObjectVertex> setPossibleNeighbors = graph.neighborNodesFor(bestImprovement);
        graph.removeVertex(bestImprovement.getFrom());
        graph.removeVertex(bestImprovement.getTo());

        ObjectVertex omMerged = bestImprovement.getPayload().getVertex();
        graph.addVertex(omMerged, setPossibleNeighbors, prioritizer, logger);

        logger.describeMerge(omMerged, bestImprovement);

        return omMerged;
    }

    /**
     * Finds the edge with the maximum priority for merging.
     *
     * @return the {@link TypedEdge} with the highest priority, or null if no valid edges exist
     */
    public TypedEdge<ObjectVertex, PrioritisedVertex> findMaxPriority() {

        TypedEdge<ObjectVertex, PrioritisedVertex> max = null;

        Comparator3i<Point3i> comparator = new Comparator3i<>();

        for (TypedEdge<ObjectVertex, PrioritisedVertex> entry : graph.edgeSetUnique()) {

            PrioritisedVertex edge = entry.getPayload();

            // We skip any edges that don't offer an improvement
            if (!edge.isConsiderForMerge()) {
                continue;
            }

            if (max == null || edge.getPriority() > max.getPayload().getPriority()) {
                max = entry;
            } else if (DoubleUtilities.areEqual(
                    edge.getPriority(), max.getPayload().getPriority())) {

                // We can safely assume a point exists on the object-mask and call .get(), as none of the
                // object-masks are empty

                // If we have equal values, we impose an arbitrary ordering
                // so as to keep the output of the algorithm as deterministic as possible
                int cmp =
                        comparator.compare(
                                max.getPayload() // NOSONAR
                                        .getVertex()
                                        .getObject()
                                        .findArbitraryOnVoxel()
                                        .get(),
                                edge.getVertex() // NOSONAR
                                        .getObject()
                                        .findArbitraryOnVoxel()
                                        .get());
                if (cmp > 0) {
                    max = entry;
                }

                // It should only be possible to ever have cmp==0 unless objects overlap. If we get
                //  this far, we resign ourselves to non-determinism
            }
        }

        return max;
    }

    /**
     * Logs a description of the current graph state.
     */
    public void logGraphDescription() {
        logger.logDescription();
    }

    /**
     * Retrieves all vertices in the graph as an {@link ObjectCollection}.
     *
     * @return an {@link ObjectCollection} containing all objects in the graph
     */
    public ObjectCollection verticesAsObjects() {
        return graph.verticesAsObjects();
    }

    /**
     * Creates a new {@link ObjectVertex} from an {@link ObjectMask}.
     *
     * @param mask the {@link ObjectMask} to create a vertex from
     * @return a new {@link ObjectVertex}
     * @throws OperationFailedException if the vertex creation fails
     */
    private ObjectVertex createVertex(ObjectMask mask) throws OperationFailedException {
        try {
            return new ObjectVertex(mask, payloadCalculator.calculate(mask));
        } catch (FeatureCalculationException e) {
            throw new OperationFailedException(e);
        }
    }
}