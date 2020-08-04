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

package org.anchoranalysis.plugin.image.bean.chnl.level;

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
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.functional.FunctionalList;
import org.anchoranalysis.core.graph.GraphWithEdgeTypes;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.histogram.Histogram;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.image.voxel.box.VoxelBox;
import org.anchoranalysis.image.voxel.neighborhood.CreateNeighborGraph;
import org.anchoranalysis.image.voxel.neighborhood.EdgeAdderParameters;

/*
 *  Calculates a threshold-level for each object collectively based on other objects
 *
 *  <p>
 *  A neighborhood-graph is compiled of objects that touch each other. The threshold for each objects is determined by the object itself and neigbours e.g. neighborhoodDistance==1 are all the immediate neighbors
 *  </p>
 */
public class ChnlProviderObjectsLevelNeighbors extends ChnlProviderLevel {

    // START BEAN
    /**
     * How many neighbors to include by distance (distance==1 -> all directly touching neighbors,
     * distance==2 -> those touching the directly touching etc.)
     */
    @BeanField @Positive @Getter @Setter private int distance; // Determines the neighbor distance
    // END BEAN

    @Override
    protected Channel createFor(Channel chnlIntensity, ObjectCollection objects, Channel chnlOutput)
            throws CreateException {
        try {
            setAgainstNeighbor(chnlIntensity, chnlOutput, objects, distance);

            return chnlOutput;

        } catch (OperationFailedException e) {
            throw new CreateException(e);
        }
    }

    private static void visit(
            GraphWithEdgeTypes<ObjectMaskWithHistogram, Integer> graph,
            List<ObjectMaskWithHistogram> currentVisit,
            List<ObjectMaskWithHistogram> toVisit,
            Set<ObjectMaskWithHistogram> visited) {
        for (ObjectMaskWithHistogram omLocal : currentVisit) {

            Collection<ObjectMaskWithHistogram> adjacent = graph.adjacentVertices(omLocal);
            for (ObjectMaskWithHistogram omAdjacent : adjacent) {
                if (!visited.contains(omAdjacent)) {
                    toVisit.add(omAdjacent);
                }
            }

            visited.add(omLocal);
        }
    }

    private static Collection<ObjectMaskWithHistogram> verticesWithinDistance(
            GraphWithEdgeTypes<ObjectMaskWithHistogram, Integer> graph,
            ObjectMaskWithHistogram om,
            int neighborDistance) {
        if (neighborDistance == 1) {
            return graph.adjacentVertices(om);
        } else {

            Set<ObjectMaskWithHistogram> visited = new HashSet<>();

            List<ObjectMaskWithHistogram> toVisit = new ArrayList<>();
            toVisit.add(om);

            for (int i = 0; i < neighborDistance; i++) {
                List<ObjectMaskWithHistogram> currentVisit = toVisit;
                toVisit = new ArrayList<>();
                visit(graph, currentVisit, toVisit, visited);
            }
            return visited;
        }
    }

    /**
     * Sums hist and everything in others
     *
     * @throws OperationFailedException
     */
    private static Histogram createSumHistograms(Histogram hist, Collection<Histogram> others)
            throws OperationFailedException {
        Histogram out = hist.duplicate();
        for (Histogram h : others) {
            out.addHistogram(h);
        }
        return out;
    }

    private void setAgainstNeighbor(
            Channel chnlIntensity,
            Channel chnlOutput,
            ObjectCollection objects,
            int neighborDistance)
            throws OperationFailedException {

        try {
            CreateNeighborGraph<ObjectMaskWithHistogram> graphCreator =
                    new CreateNeighborGraph<>(new EdgeAdderParameters(false));

            GraphWithEdgeTypes<ObjectMaskWithHistogram, Integer> graph =
                    graphCreator.createGraph(
                            objectsWithHistograms(objects, chnlIntensity),
                            ObjectMaskWithHistogram::getObject,
                            (v1, v2, numberVoxels) -> numberVoxels,
                            chnlIntensity.getDimensions().getExtent(),
                            true);

            VoxelBox<?> vbOutput = chnlOutput.voxels().any();

            // We don't need this for the computation, used only for outputting debugging
            Map<ObjectMaskWithHistogram, Integer> mapLevel = new HashMap<>();

            for (ObjectMaskWithHistogram om : graph.vertexSet()) {

                getLogger()
                        .messageLogger()
                        .logFormatted(
                                "Setting for %s against neighborhood",
                                om.getObject().centerOfGravity());

                // Get the neighbors of the current object
                Collection<ObjectMaskWithHistogram> vertexNeighbors =
                        verticesWithinDistance(graph, om, neighborDistance);

                for (ObjectMaskWithHistogram neighbor : vertexNeighbors) {
                    getLogger()
                            .messageLogger()
                            .logFormatted(
                                    "Including neighbor %s",
                                    neighbor.getObject().centerOfGravity());
                }

                // Level calculated from combined histograms
                int level = calcLevelCombinedHist(om, vertexNeighbors);

                vbOutput.setPixelsCheckMask(om.getObject(), level);

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

    private static List<ObjectMaskWithHistogram> objectsWithHistograms(
            ObjectCollection objects, Channel chnlIntensity) {
        return objects.stream()
                .mapToList(objectMask -> new ObjectMaskWithHistogram(objectMask, chnlIntensity));
    }

    private int calcLevelCombinedHist(
            ObjectMaskWithHistogram objectMask, Collection<ObjectMaskWithHistogram> vertices)
            throws OperationFailedException {

        Collection<Histogram> histogramsFromVertices =
                FunctionalList.mapToList(vertices, ObjectMaskWithHistogram::getHistogram);

        return getCalculateLevel()
                .calculateLevel(
                        createSumHistograms(objectMask.getHistogram(), histogramsFromVertices));
    }
}
