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

package org.anchoranalysis.plugin.image.bean.channel.provider.object;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.Positive;
import org.anchoranalysis.core.exception.CreateException;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.core.functional.FunctionalList;
import org.anchoranalysis.core.graph.GraphWithPayload;
import org.anchoranalysis.image.core.channel.Channel;
import org.anchoranalysis.image.voxel.Voxels;
import org.anchoranalysis.image.voxel.neighborhood.NeighborGraph;
import org.anchoranalysis.image.voxel.object.ObjectCollection;
import org.anchoranalysis.math.histogram.Histogram;

/**
 * Calculates a threshold-level for each object collectively based on other objects
 *
 * <p>A neighborhood-graph is compiled of objects that touch each other. The threshold for each
 * objects is determined by the object itself and neigbours e.g. neighborhoodDistance==1 are all the
 * immediate neighbors
 */
public class LevelPerObjectNeighbors extends LevelPerObjectBase {

    // START BEAN
    /**
     * How many neighbors to include by distance (distance==1 implies all directly touching
     * neighbors, distance==2 implies those touching the directly touching etc.)
     */
    @BeanField @Positive @Getter @Setter private int distance; // Determines the neighbor distance
    // END BEAN

    @Override
    protected void writeLevelsForObjects(
            Channel channelIntensity, ObjectCollection objects, Channel output)
            throws CreateException {
        try {
            setAgainstNeighbor(channelIntensity, output, objects, distance);

        } catch (OperationFailedException e) {
            throw new CreateException(e);
        }
    }

    private static void visit(
            GraphWithPayload<ObjectWithHistogram, Integer> graph,
            List<ObjectWithHistogram> currentVisit,
            List<ObjectWithHistogram> toVisit,
            Set<ObjectWithHistogram> visited) {
        for (ObjectWithHistogram omLocal : currentVisit) {

            Collection<ObjectWithHistogram> adjacentObjects =
                    graph.adjacentVerticesOutgoing(omLocal);

            for (ObjectWithHistogram object : adjacentObjects) {
                if (!visited.contains(object)) {
                    toVisit.add(object);
                }
            }

            visited.add(omLocal);
        }
    }

    private static Collection<ObjectWithHistogram> verticesWithinDistance(
            GraphWithPayload<ObjectWithHistogram, Integer> graph,
            ObjectWithHistogram om,
            int neighborDistance) {
        if (neighborDistance == 1) {
            return graph.adjacentVerticesOutgoing(om);
        } else {

            Set<ObjectWithHistogram> visited = new HashSet<>();

            List<ObjectWithHistogram> toVisit = new ArrayList<>();
            toVisit.add(om);

            for (int i = 0; i < neighborDistance; i++) {
                List<ObjectWithHistogram> currentVisit = toVisit;
                toVisit = new ArrayList<>();
                visit(graph, currentVisit, toVisit, visited);
            }
            return visited;
        }
    }

    /**
     * Sums histogram and everything in others
     *
     * @throws OperationFailedException
     */
    private static Histogram createSumHistograms(Histogram histogram, Collection<Histogram> others)
            throws OperationFailedException {
        Histogram out = histogram.duplicate();
        for (Histogram other : others) {
            out.addHistogram(other);
        }
        return out;
    }

    private void setAgainstNeighbor(
            Channel channelIntensity,
            Channel channelOutput,
            ObjectCollection objects,
            int neighborDistance)
            throws OperationFailedException {

        try {
            GraphWithPayload<ObjectWithHistogram, Integer> graph =
                    NeighborGraph.create(
                            objectsWithHistograms(objects, channelIntensity),
                            ObjectWithHistogram::getObject,
                            channelIntensity.extent(),
                            false,
                            true);

            Voxels<?> voxelsOutput = channelOutput.voxels().any();

            // We don't need this for the computation, used only for outputting debugging
            Map<ObjectWithHistogram, Integer> mapLevel = new HashMap<>();

            for (ObjectWithHistogram om : graph.vertices()) {

                getLogger()
                        .messageLogger()
                        .logFormatted(
                                "Setting for %s against neighborhood",
                                om.getObject().centerOfGravity());

                // Get the neighbors of the current object
                Collection<ObjectWithHistogram> vertexNeighbors =
                        verticesWithinDistance(graph, om, neighborDistance);

                for (ObjectWithHistogram neighbor : vertexNeighbors) {
                    getLogger()
                            .messageLogger()
                            .logFormatted(
                                    "Including neighbor %s",
                                    neighbor.getObject().centerOfGravity());
                }

                // Level calculated from combined histograms
                int level = calculateLevelCombinedHist(om, vertexNeighbors);

                voxelsOutput.assignValue(level).toObject(om.getObject());

                getLogger()
                        .messageLogger()
                        .logFormatted(
                                "Setting threshold %d at %s",
                                level, om.getObject().centerOfGravity());

                mapLevel.put(om, level);
            }

        } catch (CreateException e) {
            throw new OperationFailedException(e);
        }
    }

    private static List<ObjectWithHistogram> objectsWithHistograms(
            ObjectCollection objects, Channel channelIntensity) {
        return objects.stream()
                .mapToList(objectMask -> new ObjectWithHistogram(objectMask, channelIntensity));
    }

    private int calculateLevelCombinedHist(
            ObjectWithHistogram objectMask, Collection<ObjectWithHistogram> vertices)
            throws OperationFailedException {

        Collection<Histogram> histogramsFromVertices =
                FunctionalList.mapToList(vertices, ObjectWithHistogram::getHistogram);

        return getCalculateLevel()
                .calculateLevel(
                        createSumHistograms(objectMask.getHistogram(), histogramsFromVertices));
    }
}
