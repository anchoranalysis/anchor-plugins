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
import org.anchoranalysis.core.arithmetic.DoubleUtilities;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.geometry.Comparator3i;
import org.anchoranalysis.core.geometry.Point3i;
import org.anchoranalysis.core.graph.TypedEdge;
import org.anchoranalysis.core.log.Logger;
import org.anchoranalysis.feature.calculate.FeatureCalculationException;
import org.anchoranalysis.image.extent.UnitConverter;
import org.anchoranalysis.image.feature.evaluator.PayloadCalculator;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.plugin.image.object.merge.condition.UpdatableBeforeCondition;
import org.anchoranalysis.plugin.image.object.merge.priority.AssignPriority;
import org.anchoranalysis.plugin.image.object.merge.priority.PrioritisedVertex;

/**
 * A graph that stores each object as a vertex, where edges represent a neighborhood relation.
 *
 * <p>It potentially allows merges between neighboring vertices.
 *
 * <p>Each vertex has a payload (double) value associated with it, that is a function of the
 * object-mask.
 *
 * @author Owen Feehan
 */
public class MergeGraph {

    // START: Set from constructor
    private PayloadCalculator payloadCalculator;
    // END: Set from constructor

    private NeighborGraph graph;
    private GraphLogger logger;
    private AssignPriority prioritizer;

    /**
     * Constructor
     *
     * @param payloadCalculator means to calculate a payload for any object
     * @param beforeCondition
     * @param unitConverter converts units from voxels to physical measurements and vice-versa
     * @param prioritizer means to assign priority to the merge of any two objects
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

    public TypedEdge<ObjectVertex, PrioritisedVertex> findMaxPriority() {

        TypedEdge<ObjectVertex, PrioritisedVertex> max = null;

        Comparator3i<Point3i> comparator = new Comparator3i<>();

        for (TypedEdge<ObjectVertex, PrioritisedVertex> entry : graph.edgeSetUnique()) {

            PrioritisedVertex edge = entry.getPayload();

            // We skip any edges that don't off an improvement
            if (!edge.isConsiderForMerge()) {
                continue;
            }

            if (max == null || edge.getPriority() > max.getPayload().getPriority()) {
                max = entry;
            } else if (DoubleUtilities.areEqual(
                    edge.getPriority(), max.getPayload().getPriority())) {

                // We can safely assume a point exists on the object-mask and call .get(), as none
                // of the
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

    public void logGraphDescription() {
        logger.logDescription();
    }

    public ObjectCollection verticesAsObjects() {
        return graph.verticesAsObjects();
    }

    private ObjectVertex createVertex(ObjectMask obj) throws OperationFailedException {
        try {
            return new ObjectVertex(obj, payloadCalculator.calc(obj));
        } catch (FeatureCalculationException e) {
            throw new OperationFailedException(e);
        }
    }
}
